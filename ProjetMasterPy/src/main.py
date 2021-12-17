'''
Version 1.0

Date: 2021.12.17

author: Johanna Xiao Xuewen

'''

from Node import *
from CorrectPbftNode import *
import EarthPosition
from Network import *
from Simulation import *
import random

RANDOM_SEED = 12345
TIME_LIMIT = 4
SAMPLES = 100

def runPbft(initialTimeout,correctNodeCount,failedNodeCount):
    rand = random.random()
    nodes = []
    for i in range(correctNodeCount):
        position = EarthPosition.EarthPosition.randomPosition(rand)
        nodes.append(CorrectPbftNode.CorrectPbftNode(position,initialTimeout))
    for i in range(failedNodeCount):
        position = EarthPosition.EarthPosition.randomPosition(rand)
        nodes.append(FailedNode(position))

    random.shuffle(nodes)
    network = FullyConnectedNetwork(nodes,rand)
    simulation = Simulation(network)
    if simulation.run(TIME_LIMIT) is not True:
        return []

    correctNodes = []
    for node in nodes:
        if isinstance(node,CorrectPbftNode):
            correctNodes.append(node)
    for node in correctNodes:
        if node.hasTerminated():
            pass
        else:
            print("WARNING: Not all Pbft nodes terminated.")
            return []
    # 输出node的TerminationTime
    TerminationTimes = []
    for node in nodes:
        TerminationTimes.append(node.getTerminationTime())
    return TerminationTimes

if __name__ == '__main__':
    print("initial_timeout,pbft")
