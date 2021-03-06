import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CorrectTendermintNode extends Node {
  private int cycle = 0;
  private Map<Integer, CycleState> cycleStates = new HashMap<>();
  private ProtocolState protocolState;
  private double timeout;
  private double nextTimer;

  CorrectTendermintNode(EarthPosition position, double initialTimeout) {
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
        beginPreVote(simulation, time);
        break;
      case PRE_VOTE:
        beginPreCommit(simulation, time);
        break;
      case PRE_COMMIT:
//        ++cycle;
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
    cycleStates.putIfAbsent(message.getCycle(), new CycleState());
    CycleState cycleState = cycleStates.get(message.getCycle());

    if (message instanceof ProposalMessage) {
      cycleState.proposals.add(message.getProposal());
    } else if (message instanceof PreVoteMessage) {
      cycleState.preVoteCounts.merge(message.getProposal(), 1, Integer::sum);
    } else if (message instanceof PreCommitMessage) {
      cycleState.preCommitCounts.merge(message.getProposal(), 1, Integer::sum);
      Set<Proposal> committedProposals = cycleState.getCommittedProposals(simulation);
      if (!committedProposals.isEmpty()) {
        Proposal committedProposal = committedProposals.iterator().next();
        if (committedProposal != null) {
          terminate(committedProposal, time);
        }
      }
    } else {
      throw new AssertionError("Unexpected message: " + message);
    }
  }

  //类似与Algorand里的startProposal
  private void beginProposal(Simulation simulation, double time) {
    protocolState = ProtocolState.PROPOSAL;
    //    猜测此处equals是自反，只要括号内的值为非空，则返回true
    if (equals(simulation.getLeader(cycle))) {
      Proposal proposal = new Proposal();
      Message message = new ProposalMessage(cycle, proposal);
      simulation.broadcast(this, message, time);
    }
    resetTimeout(simulation, time);
  }

  private void beginPreVote(Simulation simulation, double time) {
    protocolState = ProtocolState.PRE_VOTE;
//    获得prevote的proposal，形成message并广播
    Message message = new PreVoteMessage(cycle, getProposalToPreVote(simulation));
    simulation.broadcast(this, message, time);
    resetTimeout(simulation, time);
  }

  private Proposal getProposalToPreVote(Simulation simulation) {
    // Find the latest proposal which had 2/3 pre-votes, if any. If there is one, then either that's
    // the proposal we're locked on, or we were locked on an older proposal, in which case that
    // proposal unlocks us. Either way, we're able to vote for that proposal.
//    锁定最近的拥有2/3 pre-votes的proposal
    for (int prevCycle = cycle - 1; prevCycle >= 0; --prevCycle) {
      Set<Proposal> preVotedProposals = getCycleState(prevCycle).getPreVotedProposals(simulation);
      for (Proposal preVotedProposal : preVotedProposals) {
        if (preVotedProposal != null) {
          return preVotedProposal;
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

  private void beginPreCommit(Simulation simulation, double time) {
    protocolState = ProtocolState.PRE_COMMIT;
    Set<Proposal> preVotedProposals = getCurrentCycleState().getPreVotedProposals(simulation);
    Message message;
    if (preVotedProposals.isEmpty()) {
      message = new PreCommitMessage(cycle, null);
    } else {
      Proposal proposal = preVotedProposals.iterator().next();
      message = new PreCommitMessage(cycle, proposal);
    }
    simulation.broadcast(this, message, time);
    resetTimeout(simulation, time);
  }

  private void resetTimeout(Simulation simulation, double time) {
    nextTimer = time + timeout;
    simulation.scheduleEvent(new TimerEvent(nextTimer, this));
  }

  private CycleState getCurrentCycleState() {
    return getCycleState(cycle);
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

  private int quorumSize(Simulation simulation) {
    int nodes = simulation.getNetwork().getNodes().size();
    return nodes * 2 / 3 + 1;
  }

  private class CycleState {
    final Set<Proposal> proposals = new HashSet<>();
    final Map<Proposal, Integer> preVoteCounts = new HashMap<>();
    final Map<Proposal, Integer> preCommitCounts = new HashMap<>();
    //  获得投票数大于最小值（quorumSize（））的proposal集合
    Set<Proposal> getPreVotedProposals(Simulation simulation) {
      return Util.keysWithMinCount(preVoteCounts, quorumSize(simulation));
    }
    //  获得投票数大于最小值（quorumSize（））的proposal集合
    Set<Proposal> getCommittedProposals(Simulation simulation) {
      return Util.keysWithMinCount(preCommitCounts, quorumSize(simulation));
    }
  }

//  类似Phase
  private enum ProtocolState {
    PROPOSAL, PRE_VOTE, PRE_COMMIT
  }
}
