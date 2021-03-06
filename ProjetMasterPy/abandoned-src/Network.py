'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import random
import abc
from utils import *

SPEED_OF_LIGHT = 299792458.0


class Network:
    nodes = []

    def __init__(self, nodes):
        self.nodes = nodes

    def getNodes(self):
        return self.nodes

    def getLeader(self, index):
        return self.nodes[index % len(self.nodes)]

    @abc.abstractmethod
    def getLatency(self, source, destination):
        """Method that should do something."""

    # The speed of light through a medium with a given index of refraction, in meters per second.
    def speedOfLight(self, refractiveIndex):
        return SPEED_OF_LIGHT / refractiveIndex


'''
A network in which all nodes are directly connected through a fiber optic cable, but there are
 * random delays up to 3x.
'''


class FullyConnectedNetwork(Network):
    rand = random.random()

    def __init__(self, nodes):
        super().__init__(nodes)  # self.rand = rand

def getLatency(source, destination):
    baseCaseLatency = getDistance(source,destination) / SPEED_OF_LIGHT
    multiplier = 1 + random.random()
    # print("distance = %.5f" % getDistance(source,destination))
    return multiplier * baseCaseLatency
