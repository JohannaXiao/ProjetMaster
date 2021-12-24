import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorrectPoSWbftNode extends Node{
    private int cycle = 0;
    private Map<Integer, CorrectPoSWbftNode.CycleState> cycleStates = new HashMap<>();
    private ProtocolState protocolState;
    private double timeout;
    private double nextTimer;
//    private boolean participeOrNot = false;

    CorrectPoSWbftNode(EarthPosition position, double initialTimeout) {
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
        cycleStates.putIfAbsent(message.getCycle(), new CorrectPoSWbftNode.CycleState());
        CorrectPoSWbftNode.CycleState cycleState = cycleStates.get(message.getCycle());

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
                    terminate(committedProposal, time);
//                    System.out.printf("terminatedTime = %.10f\n",time);

                }
            }
        } else {
            throw new AssertionError("Unexpected message: " + message);
        }
    }

    //类似与Algorand里的startProposal
    private void beginProposal(Simulation simulation, double time) {
        protocolState = CorrectPoSWbftNode.ProtocolState.PROPOSAL;
        //    猜测此处equals是自反，只要括号内的值为非空，则返回true
        if (equals(simulation.getLeader(cycle))) {
            Proposal proposal = new Proposal();
            Message message = new ProposalMessage(cycle, proposal);
            simulation.broadcast(this, message, time);
        }
        resetTimeout(simulation, time);
    }

    private void beginPrePrepare(Simulation simulation, double time) {
        protocolState = CorrectPoSWbftNode.ProtocolState.PRE_PREPARE;
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
        protocolState = CorrectPoSWbftNode.ProtocolState.PREPARE;
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
        protocolState = ProtocolState.COMMIT;
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

    private CorrectPoSWbftNode.CycleState getCurrentCycleState() {
        return getCycleState(cycle);
    }

    /* putIfAbsent If the specified key is not already associated with
    a value (or is mapped to null) associates it with the given value and returns null,
    else returns the current value.*/
//    cyclestate是一个map，用putIfAbsent判断是否null，null则安排一个空CycleState
//    即此处返回对应 cycle数的CycleState
    private CorrectPoSWbftNode.CycleState getCycleState(int c) {
        cycleStates.putIfAbsent(c, new CorrectPoSWbftNode.CycleState());
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

    private enum ProtocolState {
        PROPOSAL, PRE_PREPARE, PREPARE, COMMIT
    }
}
