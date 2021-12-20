'''
Version 1.0

Date: 2021.12.17

author: Johanna Xiao Xuewen

'''

import Node
import EarthPosition
from Event import *
from Proposal import Proposal
from Message import *
from enum import Enum, auto
from collections import defaultdict


class CorrectPbftNode(Node.Node):
    cycle = 0
    cycleStates = {}
    protocolState = 0
    timeout = 0
    nextTimer = 0

    def __init__(self, position, initialTimeout):
        super().__init__(position)
        self.timeout = initialTimeout

    # def __new__(cls):
    #     return super().__new__(cls)

    def onStart(self, simulation):
        self.beginProposal(simulation, 0)

    def onTimerEvent(self, timerEvent, simulation):
        if self.hasTerminated():
            return
        time = timerEvent.getTime()
        if time is not self.nextTimer:
            return

        if self.protocolState is ProtocolState.PROPOSAL:
            self.beginPrePrepare(simulation, time)
        elif self.protocolState is ProtocolState.PRE_PREPARE:
            self.beginPrepare(simulation, time)
        elif self.protocolState is ProtocolState.PREPARE:
            self.beginCommit(simulation, time)
        elif self.protocolState is ProtocolState.COMMIT:
            self.cycle += 1
            # 不知道此处是否需要
            #  Exponential backoff.指数退避 / 补偿
            # TODO: 这里的Exponential backoff不知道对PBFT需不需要
            self.timeout *= 2
            self.beginProposal(simulation, time)
        else:
            raise AssertionError("Unexpected protocol state")

    def onMessageEvent(self, messageEvent, simulation):
        if self.hasTerminated():
            return
        message = messageEvent.getMessage()
        time = messageEvent.getTime()
        # self.cycleStates.setdefault(message.getCycle())
        if not message.getCycle() in self.cycleStates:
            self.cycleStates[message.getCycle()] = CycleState()

        cycleState = self.cycleStates[message.getCycle()]

        if isinstance(message, ProposalMessage):
            cycleState.proposals.add(message.getProposal())
        elif isinstance(message, PrePrepareMessage):
            if not message.getProposal() in cycleState.prePrepareCounts:
                cycleState.prePrepareCounts[message.getProposal()] = 0
            cycleState.prePrepareCounts[message.getProposal()] += 1
        elif isinstance(message, PrepareMessage):
            if not message.getProposal() in cycleState.prepareCounts:
                cycleState.prepareCounts[message.getProposal()] = 0
            cycleState.prepareCounts[message.getProposal()] += 1
        elif isinstance(message, CommitMessage):
            if not message.getProposal() in cycleState.commitCounts:
                cycleState.commitCounts[message.getProposal()] = 0
            cycleState.commitCounts[message.getProposal()] += 1
            committedProposals = cycleState.getCommittedProposals(self, simulation)
            if committedProposals:
                committedProposal = committedProposals.pop()
                if committedProposal is not None:
                    self.terminate(committedProposal, time)
        else:
            raise AssertionError("Unexpected message: " + message)

    def beginProposal(self, simulation, time):
        self.protocolState = ProtocolState.PROPOSAL
        if self==simulation.getLeader(self.cycle):
            proposal = Proposal(self, time)
            message = ProposalMessage(self.cycle, proposal)
            simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def beginPrePrepare(self, simulation, time):
        self.protocolState = ProtocolState.PRE_PREPARE
        message = PrePrepareMessage(self.cycle, self.getProposalToPrePrepare(simulation))
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def getProposalToPrePrepare(self, simulation):
        for prevCycle in range(self.cycle - 1, -1, -1):
            prePrepareProposals = self.getCycleState(prevCycle).getPrePrepareProposals(self, simulation)
            for prePrepareProposal in prePrepareProposals:
                if prePrepareProposal:
                    return prePrepareProposal
        currentProposals = self.getCurrentCycleState().proposals
        if currentProposals:
            # 这里还有待商榷
            return currentProposals.pop()
        else:
            return None

    def beginPrepare(self, simulation, time):
        self.protocolState = ProtocolState.PREPARE
        prePrepareProposals = self.getCurrentCycleState().getPrePrepareProposals(self, simulation)
        if not prePrepareProposals:
            message = PrepareMessage(self.cycle, Proposal(self, time))
        else:
            proposal = prePrepareProposals.pop()
            message = PrepareMessage(self.cycle, proposal)
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    def beginCommit(self, simulation, time):
        self.protocolState = ProtocolState.COMMIT
        PreparedProposals = self.getCurrentCycleState().getPreparedProposals(self, simulation)
        if not PreparedProposals:
            message = CommitMessage(self.cycle, Proposal(self, time))
        else:
            proposal = PreparedProposals.pop()
            message = CommitMessage(self.cycle, proposal)
        simulation.broadcast(self, message, time)
        self.resetTimeout(simulation, time)

    ############################ 基础函数####################
    def resetTimeout(self, simulation, time):
        self.nextTimer = time + self.timeout
        simulation.scheduleEvent(TimerEvent(self.nextTimer, self))

    def getCurrentCycleState(self):
        return self.getCycleState(self.cycle)

    def getCycleState(self, c):
        if c in self.cycleStates:
            pass
        else:
            self.cycleStates[c] = CycleState()
        return self.cycleStates[c]

    # 查找是否存在key，不存在设为default：None

    def quorumSize(self, simulation):
        nodes = len(simulation.getNetwork().getNodes())
        return nodes * 2 / 3 + 1


class CycleState(object):
    # proposals = set()
    # prePrepareCounts = defaultdict(int)
    # prepareCounts = defaultdict(int)
    # commitCounts = defaultdict(int)
    proposals = set()
    prePrepareCounts = {}
    prepareCounts = {}
    commitCounts = {}

    def __init__(self):
        self.proposals = set()
        self.prePrepareCounts = {}
        self.prepareCounts = {}
        self.commitCounts = {}

    def getPrePrepareProposals(self, node, simulation):
        res = set()
        for k, v in self.prePrepareCounts.items():
            if v >= node.quorumSize(simulation):
                res.add(k)
            else:
                pass
        return res

    # 此处嵌套类调用外部类的函数需要外化该类


    def getPreparedProposals(self, node, simulation):
        res = set()
        for k, v in self.prepareCounts.items():
            if v >= node.quorumSize(simulation):
                res.add(k)
            else:
                pass
        return res

    def getCommittedProposals(self, node, simulation):
        res = set()
        for k, v in self.commitCounts.items():
            if v >= node.quorumSize(simulation):
                res.add(k)
            else:
                pass
        return res

class ProtocolState(Enum):
    PROPOSAL = auto()
    PRE_PREPARE = auto()
    PREPARE = auto()
    COMMIT = auto()
