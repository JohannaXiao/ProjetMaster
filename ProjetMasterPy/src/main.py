'''
Version 1.0

Date: 2021.12.17

author: Johanna Xiao Xuewen

'''

from Node import Node, FailedNode
from CorrectPbftNode import *
import EarthPosition
from Network import *
from Simulation import *
from Vector3d import Vector3d
import random
import sys
import numpy as np
from ordered_set import OrderedSet
import pprint

RANDOM_SEED = 12345
TIME_LIMIT = 4
SAMPLES = 100


def runPbft(initialTimeout, correctNodeCount, failedNodeCount):
    rand = random.random()
    nodes = []
    for i in range(correctNodeCount):
        position = EarthPosition.EarthPosition(Vector3d(0, 0, 0))
        position.randomPosition()
        # CorrectPbftNode(position, initialTimeout)
        nodes.append(CorrectPbftNode(position, initialTimeout))

    for i in range(failedNodeCount):
        position = EarthPosition.EarthPosition(Vector3d(0, 0, 0))
        position.randomPosition()
        nodes.append(FailedNode(position))

    random.shuffle(nodes)
    network = FullyConnectedNetwork(nodes, rand)
    # eventBytime = OrderedSet()
    simulation = Simulation(network)
    if not simulation.run(TIME_LIMIT):
        # pprint.pprint(nodes)
        # pprint.pprint(network.nodes)
        # pprint.pprint(simulation.network.nodes)
        return []

    # print(nodes)
    # print(network.nodes)
    # print(simulation.network.nodes)

    correctNodes = []
    for node in simulation.network.nodes:
        if isinstance(node, CorrectPbftNode):
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
    pbftBestLatency = sys.float_info.max
    pbftBestTimeout = 0
    pbftOveralStats = []
    # nodes = []
    for initialTimeout in np.arange(0.01, 0.41, 0.01):

        for i in range(SAMPLES):
            pbftStats = runPbft(initialTimeout, 4, 1)
            if pbftStats:
                pbftOveralStats.extend(pbftStats)
                # print(pbftStats)
        if len(pbftOveralStats) > 0 and np.mean(pbftOveralStats) < pbftBestLatency:
            pbftBestLatency = np.mean(pbftOveralStats)
            pbftBestTimeout = initialTimeout

        print("%.2f, %s," % (initialTimeout, str(np.mean(pbftOveralStats)) if pbftOveralStats else " "))
        # print(pbftOveralStats)

    print("\n")
    print("pbft best with timeout %.2f: %.4f" % (pbftBestTimeout, pbftBestLatency))

    # node1 = Node.Node(position=EarthPosition.EarthPosition(direction=Vector3d(0,0,0)))
    # node2 = Node.Node(position=EarthPosition.EarthPosition(direction=Vector3d(0,0,0)))
    # print(node1==node2)
