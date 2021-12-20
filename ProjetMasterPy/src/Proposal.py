'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''


class Proposal(object):
    # header = 0

    def __init__(self, node, time):
        # self.header = 0
        self.node = node
        self.time = time

    def __hash__(self):
        return hash((self.node, self.time))

    def __eq__(self, other):
        return (self.node, self.time) == (other.node, other.time)
