'''
Version 1.0

Date: 2021.12.17

author: Johanna Xiao Xuewen

'''

import Node
import EarthPosition
from Event import *
import Proposal
from Message import *
from enum import Enum, auto
from collections import defaultdict


class CorrectPbftNode(Node):
    cycle = 0
    cycleStates = {}
    protocolState = 0
    timeout = 0
    nextTimer = 0

    def __init__(self, position, initialTimeout):
        super().__init__(position, initialTimeout)
        self.timeout = initialTimeout

    def onStart(self, simulation):
        self.beginProposal(simulation, 0)

    def onTimerEvent(self, timerEvent, simulation):
        if self.hasTerminated():
            return
        time = timerEvent.getTime()
        if time is not self.nextTimer:
            return

        if self.protocolState is CorrectPbftNode.ProtocolState.PROPOSAL:
            self.beginPrePrepare(simulation, time)
        elif self.protocolState is CorrectPbftNode.ProtocolState.PRE_PREPARE:
            self.beginPrepare(simulation, time)
        elif self.protocolState is CorrectPbftNode.ProtocolState.PREPARE:
            self.beginCommit(simulation, time)
        elif self.protocolState is CorrectPbftNode.ProtocolState.COMMIT:
            self.cycle += 1
            # 不知道此处是否需要
            #  Exponential backoff.指数退避 / 补偿
            # TODO: 这里的Exponential backoff不知道对PBFT需不需要
            self.timeout *= 2
            self.beginProposal(simulation,time)
        else:
            raise AssertionError("Unexpected protocol state")

    def onMessageEvent(self,messageEvent,simulation):
        if self.hasTerminated():
            return
        message = messageEvent.getMessage()
        time = messageEvent.getTime()
        self.cycleStates.setdefault(message.getCycle())
        cycleState = self.cycleStates.get(message.getCycle())

        if isinstance(message,ProposalMessage):
            cycleState.proposals.add(message.getProposal())
        elif isinstance(message,PrePrepareMessage):
            cycleState.prePrepareCounts[message.getProposal()]+=1
        elif isinstance(message,PrepareMessage):
            cycleState.prepareCounts[message.getProposal()]+=1
        elif isinstance(message,CommitMessage):
            cycleState.commitCounts[message.getProposal()]+=1
            committedProposals = cycleState.getCommittedProposals(simulation)
            if committedProposals is not None:
                committedProposal = committedProposals.pop()
                if committedProposal is not None:
                    self.terminate(committedProposal,time)
        else:
            raise AssertionError("Unexpected message: " + message)

    def beginProposal(self, simulation, time):
        protocolState = CorrectPbftNode.ProtocolState.PROPOSAL
        if simulation.getLeader(self.cycle) is not None:
            proposal = Proposal.Proposal()
            message = ProposalMessage(self.cycle, proposal)
            simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def beginPrePrepare(self, simulation, time):
        protocolState = CorrectPbftNode.ProtocolState.PRE_PREPARE
        message = PrePrepareMessage(self.cycle, self.getProposalToPrePare(simulation))
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def getProposalToPrePare(self, simulation):
        for prevCycle in range(self.cycle - 1, -1, -1):
            prePrepareProposals = self.getCycleState(prevCycle).getPrePrepareProposals(simulation)
            for prePrepareProposal in prePrepareProposals:
                if prePrepareProposal is not None:
                    return prePrepareProposal
        currentProposals = self.getCurrentCycleState().proposals
        if currentProposals is not None:
            # 这里还有待商榷
            return currentProposals.pop()

    def beginPrepare(self, simulation, time):
        protocolState = CorrectPbftNode.ProtocolState.PREPARE
        prePrepareProposals = self.getCurrentCycleState().getPrePrepareProposals(simulation)
        if prePrepareProposals is None:
            message = PrepareMessage(self.cycle, None)
        else:
            proposal = prePrepareProposals.pop()
            message = PrepareMessage(self.cycle, proposal)
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def beginCommit(self, simulation, time):
        protocolState = CorrectPbftNode.ProtocolState.COMMIT
        PreparedProposals = self.getCurrentCycleState().getPreparedProposals(simulation)
        if PreparedProposals is None:
            message = CommitMessage(self.cycle, None)
        else:
            proposal = PreparedProposals.pop()
            message = CommitMessage(self.cycle, proposal)
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    ############################ 基础函数####################
    def resetTimeout(self, simulation, time):
        nextTimer = time + self.timeout
        simulation.scheduleEvent(TimerEvent(nextTimer, self))

    def getCurrentCycleState(self):
        return self.getCycleState(self.cycle)

    def getCycleState(self, c):
        return self.cycleStates.setdefault(c)  # 查找是否存在key，不存在设为default：None

    def quorumSize(self, simulation):
        nodes = simulation.getNetwork().getNodes().size()
        return nodes * 2 / 3 + 1

    class CycleState:
        proposals = set()
        prePrepareCounts = defaultdict(int)
        prepareCounts = defaultdict(int)
        commitCounts = defaultdict(int)

        def getPrePrepareProposals(self, simulation):
            return {k: v for k, v in self.prePrepareCounts.items() if
                    v >= CorrectPbftNode.quorumSize(simulation)}  # 此处嵌套类调用外部类的函数需要外化该类

        def getPrepareProposals(self, simulation):
            return {k: v for k, v in self.prepareCounts.items() if v >= CorrectPbftNode.quorumSize(simulation)}

        def getCommittedProposals(self, simulation):
            return {k: v for k, v in self.commitCounts.items() if v >= CorrectPbftNode.quorumSize(simulation)}

    class ProtocolState(Enum):
        PROPOSAL = auto()
        PRE_PREPARE = auto()
        PREPARE = auto()
        COMMIT = auto()
