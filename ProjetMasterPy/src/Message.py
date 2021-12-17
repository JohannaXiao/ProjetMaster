'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import abc
import Proposal

class Message:
    cycle =0
    proposal =Proposal.Proposal()
    def __init__(self,cycle,proposal):
        self.cycle=cycle
        self.proposal=proposal

    def getCycle(self):
        return self.cycle

    def getProposal(self):
        return self.proposal


class ProposalMessage(Message):
    def __init__(self,cycle,proposal):
        super().__init__(cycle,proposal)

    def toString(self):
        return ("ProposalMessage[cycle=%d, proposal=%s]" %(self.getCycle(),self.getProposal()))

# A PBFT pre-prepare message
class PrePrepareMessage(Message):
    def __init__(self,cycle,proposal):
        super().__init__(cycle,proposal)

    def toString(self):
        return ("PrePrepareMessage[cycle=%d, proposal=%s]" %(self.getCycle(),self.getProposal()))


# A PBFT prepare messag
class PrepareMessage(Message):
    def __init__(self,cycle,proposal):
        super().__init__(cycle,proposal)

    def toString(self):
        return ("PrepareMessage[cycle=%d, proposal=%s]" %(self.getCycle(),self.getProposal()))

# A PBFT commit message
class CommitMessage(Message):
    def __init__(self,cycle,proposal):
        super().__init__(cycle,proposal)

    def toString(self):
        return ("CommitMessage[cycle=%d, proposal=%s]" %(self.getCycle(),self.getProposal()))
