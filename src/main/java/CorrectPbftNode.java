import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorrectPbftNode extends Node {
    private int cycle = 0;
    private Map<Integer, CorrectPbftNode.CycleState> cycleStates = new HashMap<>();
    private CorrectPbftNode.ProtocolState protocolState;
    private double timeout;
    private double nextTimer;
    private CopyOnWriteArrayList<Block> blockChain = new CopyOnWriteArrayList<>();

    CorrectPbftNode(EarthPosition position, double initialTimeout) {
        super(position);
        this.timeout = initialTimeout;
    }
    @Override public void onStart(Simulation simulation) {
        beginProposal(simulation, 0);
    }

    @Override public void onTimerEvent(TimerEvent timerEvent, Simulation simulation) {
        if (hasTerminated()) {
            return;
        }

        double time = timerEvent.getTime();
        if (time != nextTimer) {
            // It's a stale timer; we must have made the relevant state transition based on observed
            // messages rather than a timer. Ignore it.
            return;
        }

        switch (protocolState) {
            case PROPOSAL:
                beginPrePrepare(simulation, time);
                break;
            case PRE_PREPARE:
                beginPrepare(simulation, time);
                break;
            case PREPARE:
                beginCommit(simulation, time);
            case COMMIT:
                ++cycle;
                // Exponential backoff.指数退避/补偿
                //TODO:这里的Exponential backoff不知道对PBFT需不需要
                timeout *= 2;
                beginProposal(simulation, time);
                break;
            default:
                throw new AssertionError("Unexpected protocol state");
        }
    }

    @Override public void onMessageEvent(MessageEvent messageEvent, Simulation simulation) {
        if (hasTerminated()) {
            return;
        }

        Message message = messageEvent.getMessage();
        double time = messageEvent.getTime();
        cycleStates.putIfAbsent(message.getCycle(), new CorrectPbftNode.CycleState());
        CorrectPbftNode.CycleState cycleState = cycleStates.get(message.getCycle());

        if (message instanceof ProposalMessage) {
            cycleState.proposals.add(message.getProposal());
        } else if (message instanceof PrePreareMessage) {
            cycleState.prePrepareCounts.merge(message.getProposal(), 1, Integer::sum);
        } else if (message instanceof PrepareMessage) {
            cycleState.prepareCounts.merge(message.getProposal(), 1, Integer::sum);
        } else if (message instanceof CommitMessage) {
            cycleState.commitCounts.merge(message.getProposal(),1,Integer::sum);
            Set<Proposal> committedProposals = cycleState.getCommittedProposals(simulation);
            if (!committedProposals.isEmpty()) {
                Proposal committedProposal = committedProposals.iterator().next();
                if (committedProposal != null) {
                    addBlock(message.getProposal().getBlock());
                    terminate(committedProposal, time);
//                    System.out.printf("terminatedTime = %.10f\n",time);

                }
            }
        } else {
            throw new AssertionError("Unexpected message: " + message);
        }
    }

    //add block
    public boolean addBlock(Block blcok){
        if (isValidNewBlock(blcok)) {
            blockChain.add(blcok);
            return true;
        }
        return false;
    }

    //类似与Algorand里的startProposal
    private void beginProposal(Simulation simulation, double time) {
        protocolState = CorrectPbftNode.ProtocolState.PROPOSAL;
        //    猜测此处equals是自反，只要括号内的值为非空，则返回true
        if (equals(simulation.getLeader(cycle))) {
            Proposal proposal = new Proposal();
            Message message = new ProposalMessage(cycle, proposal);
            simulation.broadcast(this, message, time);
        }
        resetTimeout(simulation, time);
    }

    private void beginPrePrepare(Simulation simulation, double time) {
        protocolState = CorrectPbftNode.ProtocolState.PRE_PREPARE;
//    获得prevote的proposal，形成message并广播
        Message message = new PrePreareMessage(cycle, getProposalToPrePrepare(simulation));
        simulation.broadcast(this, message, time);
        resetTimeout(simulation, time);
    }

    private Proposal getProposalToPrePrepare(Simulation simulation) {
        // Find the latest proposal which had 2/3 pre-votes, if any. If there is one, then either that's
        // the proposal we're locked on, or we were locked on an older proposal, in which case that
        // proposal unlocks us. Either way, we're able to vote for that proposal.
//    锁定最近的拥有2/3 pre-votes的proposal
        for (int prevCycle = cycle - 1; prevCycle >= 0; --prevCycle) {
            Set<Proposal> prePreparedProposals = getCycleState(prevCycle).getPrePreparedProposals(simulation);
            for (Proposal prePreparedProposal : prePreparedProposals) {
                if (prePreparedProposal != null) {
                    return prePreparedProposal;
                }
            }
        }

        // If we got here, then no proposal has received 2/3 pre-votes, so we never would have
        // pre-committed any proposal. Thus, we're not locked so we're free to vote for whatever
        // proposal we've received, if any.
        Set<Proposal> currentProposals = getCurrentCycleState().proposals;
        if (!currentProposals.isEmpty()) {
            return currentProposals.iterator().next();
        } else {
            return null;
        }
    }

    private void beginPrepare(Simulation simulation, double time) {
        protocolState = CorrectPbftNode.ProtocolState.PREPARE;
        Set<Proposal> prePreparedProposals = getCurrentCycleState().getPrePreparedProposals(simulation);
        Message message;
        if (prePreparedProposals.isEmpty()) {
            message = new PrepareMessage(cycle, null);
        } else {
            Proposal proposal = prePreparedProposals.iterator().next();
            message = new PrepareMessage(cycle, proposal);
        }
        simulation.broadcast(this, message, time);
        resetTimeout(simulation, time);
    }

    private void beginCommit(Simulation simulation, double time) {
        protocolState = CorrectPbftNode.ProtocolState.COMMIT;
        Set<Proposal> PreparedProposals = getCurrentCycleState().getPreparedProposals(simulation);
        Message message;
        if (PreparedProposals.isEmpty()) {
            message = new CommitMessage(cycle, null);
        } else {
            Proposal proposal = PreparedProposals.iterator().next();
            message = new CommitMessage(cycle, proposal);
        }
        simulation.broadcast(this,message,time);
        resetTimeout(simulation,time);
    }

    private void resetTimeout(Simulation simulation, double time) {
        nextTimer = time + timeout;
        simulation.scheduleEvent(new TimerEvent(nextTimer, this));
    }

    private CorrectPbftNode.CycleState getCurrentCycleState() {
        return getCycleState(cycle);
    }

    /* putIfAbsent If the specified key is not already associated with
    a value (or is mapped to null) associates it with the given value and returns null,
    else returns the current value.*/
//    cyclestate是一个map，用putIfAbsent判断是否null，null则安排一个空CycleState
//    即此处返回对应 cycle数的CycleState
    private CorrectPbftNode.CycleState getCycleState(int c) {
        cycleStates.putIfAbsent(c, new CorrectPbftNode.CycleState());
        return cycleStates.get(c);
    }

    private int quorumSize(Simulation simulation) {
        int nodes = simulation.getNetwork().getNodes().size();
        return nodes * 2 / 3 + 1;
    }

    private class CycleState {
        final Set<Proposal> proposals = new HashSet<>();
        final Map<Proposal, Integer> prePrepareCounts = new HashMap<>();
        final Map<Proposal, Integer> prepareCounts = new HashMap<>();
        final Map<Proposal, Integer> commitCounts = new HashMap<>();
        //  获得投票数大于最小值（quorumSize（））的proposal集合
        Set<Proposal> getPrePreparedProposals(Simulation simulation) {
            return Util.keysWithMinCount(prePrepareCounts, quorumSize(simulation));
        }
        Set<Proposal> getPreparedProposals(Simulation simulation) {
            return Util.keysWithMinCount(prepareCounts, quorumSize(simulation));
        }
        //  获得投票数大于最小值（quorumSize（））的proposal集合
        Set<Proposal> getCommittedProposals(Simulation simulation) {
            return Util.keysWithMinCount(commitCounts, quorumSize(simulation));
        }
    }

    //  类似Phase
    private enum ProtocolState {
        PROPOSAL, PRE_PREPARE, PREPARE, COMMIT
    }

    //创建区块
    public Block createNewBlock() {
        Block block = new Block();
        block.setIndex(blockChain.size());
        //时间戳
        block.setTimestamp(System.currentTimeMillis());
        block.setTransactions(new ArrayList<Transaction>());

        //上一区块的哈希
        block.setPreviousHash(Util.SHA256(blockChain.isEmpty()?"123":blockChain.get(blockChain.size()-1).toString()));
//        System.out.println("区块数:"+ blockChain.size()+";createBlock:"+block);
        return block;
    }



    private boolean isValidNewBlock(Block block){
        if(blockChain.isEmpty()){
            return true;
        }else {
            Block previousBlock = blockChain.get(blockChain.size()-1);
            if (!Util.SHA256(previousBlock.toString()).equals(block.getPreviousHash())) {
//                System.out.println("新区块的前一个区块hash验证不通过,上一个区块的hash应该为："+ Util.SHA256(previousBlock.toString()) +";这一个区块上一个区块hash为"+ block.getPreviousHash());
                return false;
            }
        }
        return  true;
    }

    private boolean isValidProposal(ProposalMessage proposalMessage){
        if(isValidNewBlock(proposalMessage.getProposal().getBlock())) {
            return true;
        }

        return false;
    }


    public CopyOnWriteArrayList<Block> getBlockChain() {
        return blockChain;
    }

    public Block getLastBlock() {
        if(blockChain.isEmpty()) return null;
        return blockChain.get(blockChain.size()-1);
    }
}

