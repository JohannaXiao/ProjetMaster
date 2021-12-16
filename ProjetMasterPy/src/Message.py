'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import Proposal

class Message:
    cycle =0
    proposal =Proposal()
    def __init__(self,cycle,proposal):
        self.cycle=cycle
        self.proposal=proposal

    def getCycle(self):
        return self.cycle
