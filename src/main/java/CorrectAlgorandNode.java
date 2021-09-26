import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CorrectAlgorandNode extends Node {
    private final double timeout;
    private final Map<Integer, CycleState> cycleStates = new HashMap<>();
    private int cycle = 0;
    private double nextTimer;
    private Phase phase;

    CorrectAlgorandNode(EarthPosition position, double timeout) {
        super(position);
        this.timeout = timeout;
    }

    @Override
//    发起Proposal，时间初始化为0
    void onStart(Simulation simulation) {
        startProposal(simulation, 0);
    }

    @Override
    void onTimerEvent(TimerEvent timerEvent, Simulation simulation) {
//        问是否已经完成proposal
        if (hasTerminated()) {
            return;
        }

        double time = timerEvent.getTime();
        if (time != nextTimer) {
            // It's a stale timer; we must have made the relevant state transition based on observed
            // messages rather than a timer. Ignore it.
            return;
        }
//      根据不同的Phase进行操作
        switch (phase) {
            case PROPOSAL:
                doFiltering(simulation, time);
                break;
            case CERTIFYING:
                doFirstFinishing(simulation, time);
                break;
            case SECOND_FINISHING:
                throw new AssertionError("Eh? Shouldn't receive a timeout here.");
        }
    }

    @Override
    void onMessageEvent(MessageEvent messageEvent, Simulation simulation) {
        if (hasTerminated()) {
            return;
        }

        Message message = messageEvent.getMessage();
        double time = messageEvent.getTime();

//      根据message类型不同进行不同处理
        if (message instanceof ProposalMessage) {
            handleProposalMessage((ProposalMessage) message);
        } else if (message instanceof SoftVoteMessage) {
            handleSoftVoteMessage(simulation, time, (SoftVoteMessage) message);
        } else if (message instanceof CertVoteMessage) {
            handleCertVoteMessage(simulation, time, (CertVoteMessage) message);
        } else if (message instanceof NextVoteMessage) {
            handleNextVoteMessage(simulation, time, (NextVoteMessage) message);
        }
    }

    private void handleProposalMessage(ProposalMessage proposalMessage) {
//        在对应cycle的State下的proposal集合中加入ProposalMessage的Proposal
        getCycleState(proposalMessage.getCycle()).proposals.add(proposalMessage.getProposal());
    }

    private void handleSoftVoteMessage(Simulation simulation, double time,
                                       SoftVoteMessage softVoteMessage) {
        getCycleState(softVoteMessage.getCycle()).addSoftVote(softVoteMessage);
        if (cycle != softVoteMessage.getCycle()) {
            return;
        }

        CycleState currentCycleState = getCurrentCycleState();
        Set<Proposal> softVotedProposals = currentCycleState.getSoftVotedProposals(simulation);
//        如果Phase == Certifying，则本cycle下的softvote需要确认proposal
        if (phase == Phase.CERTIFYING && currentCycleState.myCertifiedProposal == null) {
            if (!softVotedProposals.isEmpty()) {
                Proposal proposalToCertify = softVotedProposals.iterator().next();
                Message certVote = new CertVoteMessage(cycle, proposalToCertify);
                simulation.broadcast(this, certVote, time);
                currentCycleState.myCertifiedProposal = proposalToCertify;
            }
//            如果是Second Finishing，说明已经确认过某个Proposal，进入nextvote中待下一次投票确认
        } else if (phase == Phase.SECOND_FINISHING) {
            for (Proposal softVotedProposal : softVotedProposals) {
                if (softVotedProposal != null &&
                        currentCycleState.myNextVotedProposals.add(softVotedProposal)) {
                    Message nextVote = new NextVoteMessage(cycle, softVotedProposal);
                    simulation.broadcast(this, nextVote, time);
                }
            }
//            Second Finishing状态下，myCertifiedProposal==null，上一个cycle的nextvote也null，代表proposal要完了吧，把这个null nextvote广播出去
            if (cycle > 0 && currentCycleState.myCertifiedProposal == null) {
                CycleState lastCycleState = getLastCycleState();
                if (lastCycleState.getNextVotedProposals(simulation).contains(null)) {
                    if (currentCycleState.myNextVotedProposals.add(null)) {
                        Message nextVote = new NextVoteMessage(cycle, null);
                        simulation.broadcast(this, nextVote, time);
                    }
                }
            }
        }
    }

    private void handleCertVoteMessage(Simulation simulation, double time,
                                       CertVoteMessage certVoteMessage) {
        CycleState messageCycleState = getCycleState(certVoteMessage.getCycle());
        messageCycleState.addCertVote(certVoteMessage);
        Set<Proposal> certifiedProposals = messageCycleState.getCertifiedProposals(simulation);

        if (!certifiedProposals.isEmpty()) {
            Proposal certifiedProposal = certifiedProposals.iterator().next();
            if (certifiedProposal == null) {
                throw new AssertionError("Shouldn't have cert-votes for nil?");
            }
            terminate(certifiedProposal, time);
        }
    }

    private void handleNextVoteMessage(Simulation simulation, double time,
                                       NextVoteMessage nextVoteMessage) {
        CycleState messageCycleState = getCycleState(nextVoteMessage.getCycle());
        messageCycleState.addNextVote(nextVoteMessage);
        boolean currentCycle = cycle == nextVoteMessage.getCycle();

        if (currentCycle && messageCycleState.hasNextVotedProposal(simulation)) {
            while (getCurrentCycleState().hasNextVotedProposal(simulation)) {
                Proposal nextVotedProposal = getCurrentCycleState()
                        .getNextVotedProposals(simulation).iterator().next();
                ++cycle;
                getCurrentCycleState().startingValue = nextVotedProposal;
            }
            startProposal(simulation, time);
        }
    }

    //  基础函数
//  节点发起proposal
    private void startProposal(Simulation simulation, double time) {
        phase = Phase.PROPOSAL;
//    猜测此处equals是自反，只要括号内的值为非空，则返回true
        if (equals(simulation.getLeader(cycle))) {
            Proposal proposal = new Proposal();
            Message message = new ProposalMessage(cycle, proposal);
            simulation.broadcast(this, message, time);
        }
        resetTimeout(simulation, time);
    }

    // 做节点，轮次过滤
    private void doFiltering(Simulation simulation, double time) {
        Set<Proposal> nextVotedProposals;
//    如果在第一轮次，没有proposal，否则如下
        if (cycle > 0) {
//            下一个要投票Proposal即上一cycle的下次要投票的Proposal（这里已经做了需要大于2/3+1的筛选）
            nextVotedProposals = getLastCycleState().getNextVotedProposals(simulation);
        } else {
            nextVotedProposals = new HashSet<>();
        }
//  获得proposal后需对该proposal进行投票
        Proposal proposalToSoftVote = null;
        if (cycle == 0 || cycle > 0 && nextVotedProposals.contains(null)) {
//            首先保证该轮次中Proposal是否还有需要投票的
            if (!getCurrentCycleState().proposals.isEmpty()) {
//                目前需要投票的Proposal集合中，获取下一个进入SoftVote
                proposalToSoftVote = getCurrentCycleState().proposals.iterator().next();
            }
//            再确定上一轮次中是否有剩余需要投票的（按照该逻辑，会先清理上轮次中需要进行nextvote的proposal）
        } else if (cycle > 0 && !nextVotedProposals.isEmpty()) {
            proposalToSoftVote = nextVotedProposals.iterator().next();
        }
//      把现在需要投票的Proposal与Cycle打包成softVote Message，由此节点广播出去
        if (proposalToSoftVote != null) {
            Message softVote = new SoftVoteMessage(cycle, proposalToSoftVote);
            simulation.broadcast(this, softVote, time);
        }
//        将节点状态转换为CERTIFYING确认状态，并重设timeout时间点
        phase = Phase.CERTIFYING;
        resetTimeout(simulation, time);
    }

    private void doFirstFinishing(Simulation simulation, double time) {
        Proposal proposalToNextVote;
//        检查在本cycle中是否存在已经确认的Proposal，如果有则是进入下一次投票的队列中
        if (getCurrentCycleState().myCertifiedProposal != null) {
            proposalToNextVote = getCurrentCycleState().myCertifiedProposal;
        } else if (cycle > 0
                && getLastCycleState().getNextVotedProposals(simulation).contains(null)) {
            proposalToNextVote = null;
        } else {
            proposalToNextVote = getCurrentCycleState().startingValue;
        }

        getCurrentCycleState().myNextVotedProposals.add(proposalToNextVote);
        NextVoteMessage nextVote = new NextVoteMessage(cycle, proposalToNextVote);
        simulation.broadcast(this, nextVote, time);
//      完成first之后进入second
        phase = Phase.SECOND_FINISHING;
    }

//      重设最大时间，即现在的时间点+timeout时间段
    private void resetTimeout(Simulation simulation, double time) {
        nextTimer = time + timeout;
        simulation.scheduleEvent(new TimerEvent(nextTimer, this));
    }
//  获取当前cycle的state
    private CycleState getCurrentCycleState() {
        return getCycleState(cycle);
    }
//  获取上一个cycle的state
    private CycleState getLastCycleState() {
        return getCycleState(cycle - 1);
    }

    /* putIfAbsent If the specified key is not already associated with
     a value (or is mapped to null) associates it with the given value and returns null,
     else returns the current value.*/
//    cyclestate是一个map，用putIfAbsent判断是否null，null则安排一个空CycleState
//    即此处返回对应 cycle数的CycleState
    private CycleState getCycleState(int c) {
        cycleStates.putIfAbsent(c, new CycleState());
        return cycleStates.get(c);
    }

    // 定义CycleState类
//  其中包含 startingValue，myCertifiedProposal， myNextVotedProposals，
//  softVoteCounts，certVoteCounts，nextVoteCounts
    private class CycleState {
        private Proposal startingValue = null;
        private Set<Proposal> proposals = new HashSet<>();
        private Proposal myCertifiedProposal = null;
        private Set<Proposal> myNextVotedProposals = new HashSet<>();
        private Map<Proposal, Integer> softVoteCounts = new HashMap<>();
        private Map<Proposal, Integer> certVoteCounts = new HashMap<>();
        private Map<Proposal, Integer> nextVoteCounts = new HashMap<>();

        //    对应Proposal上的投票数加一
        void addSoftVote(SoftVoteMessage softVote) {
            softVoteCounts.merge(softVote.getProposal(), 1, Integer::sum);
        }

        void addCertVote(CertVoteMessage certVote) {
            certVoteCounts.merge(certVote.getProposal(), 1, Integer::sum);
        }

        void addNextVote(NextVoteMessage nextVote) {
            nextVoteCounts.merge(nextVote.getProposal(), 1, Integer::sum);
        }

        //  获得投票数大于最小值（quorumSize（））的proposal集合
        Set<Proposal> getSoftVotedProposals(Simulation simulation) {
            return Util.keysWithMinCount(softVoteCounts, quorumSize(simulation));
        }

        Set<Proposal> getCertifiedProposals(Simulation simulation) {
            return Util.keysWithMinCount(certVoteCounts, quorumSize(simulation));
        }

        Set<Proposal> getNextVotedProposals(Simulation simulation) {
            return Util.keysWithMinCount(nextVoteCounts, quorumSize(simulation));
        }

        //  判断是否还存在下一个投票Proposal
        boolean hasNextVotedProposal(Simulation simulation) {
            return !getNextVotedProposals(simulation).isEmpty();
        }
    }

    //最小值，投票数超过总节点数地2/3+1才能通过
    private int quorumSize(Simulation simulation) {
        int nodes = simulation.getNetwork().getNodes().size();
        return nodes * 2 / 3 + 1;
    }

    // 节点目前可能的状态
    private enum Phase {
        PROPOSAL, CERTIFYING, SECOND_FINISHING
    }
}
