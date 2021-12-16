'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import abc
import Vector3d
import EarthPosition
import Proposal
import Simulation
import Event


class Node:
    output = 0
    terminationTime = 0

    def __init__(self, position):
        self.position = position

    def __eq__(self, other):
        return self.position == other.position

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
