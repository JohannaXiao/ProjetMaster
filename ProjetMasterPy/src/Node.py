'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import abc
from functools import total_ordering
import Vector3d
import EarthPosition
from Proposal import Proposal
import Simulation
import Event

@total_ordering
class Node(object):
    output = None
    terminationTime = 0
    position = None

    def __init__(self, position):
        self.position = position
        # print('使用了equal函数的对象的id',id(self))
        # self.output =output
        # self.terminationTime=terminationTime

    def __eq__(self, other):
        return self.position == other.position

    def __lt__(self, other):
        return self.terminationTime<other.terminationTime

    def __hash__(self):
        return hash(self.position)

    @abc.abstractmethod
    def onStart(self, simulation):
        """Method that should do something."""

    @abc.abstractmethod
    def onTimerEvent(self, timerEvent, simulation):
        """Method that should do something."""

    @abc.abstractmethod
    def onMessageEvent(self, messageEvent, simulation):
        """Method that should do something."""

    def hasTerminated(self):
        return self.output is not None

    def terminate(self,output,terminationTime):
        self.output=output
        self.terminationTime = terminationTime

    def getDistance(self,other):
        return self.position.getDistance(other.position)

    def getTerminationTime(self):
        return self.terminationTime

class FailedNode(Node):
    def __init__(self,position):
        super().__init__(position)

    def onStart(self, simulation):
        pass
    def onTimerEvent(self, timerEvent, simulation):
        pass
    def onMessageEvent(self, messageEvent, simulation):
        pass