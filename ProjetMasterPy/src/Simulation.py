'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import Node
import Network
import Message
from Event import *
from ordered_set import OrderedSet


class Simulation:
    network = None
    eventByTime = OrderedSet()

    def __init__(self, network):
        self.network = network
        # self.eventByTime = eventByTime

    def broadcast(self, source, message, time):
        for destination in self.network.getNodes():
            latency = self.network.getLatency(source, destination)
            arrivalTime = time + latency
            self.eventByTime.add(MessageEvent(arrivalTime, destination, message))
            self.eventByTime = OrderedSet(sorted(list(self.eventByTime),reverse=True))

    def getNetwork(self):
        return self.network

    def getLeader(self, index):
        return self.network.getLeader(index)

    def scheduleEvent(self, event):
        self.eventByTime.add(event)
        self.eventByTime = OrderedSet(sorted(list(self.eventByTime),reverse=True))

    '''
      * Run until all events have been processed, including any newly added events which may be added
   * while running.
   *
   * @param timeLimit the maximum amount of time before the simulation halts
   * @return whether the simulation completed within the time limit 
    '''

    def run(self, timelimit):

        for node in self.network.nodes:
            node.onStart(self)
        while self.eventByTime:
            event = self.eventByTime.pop()
            if event.getTime() > timelimit:
                return False

            # subject = event.getSubject()
            if isinstance(event, TimerEvent):
                event.getSubject().onTimerEvent(event, self)
            elif isinstance(event, MessageEvent):
                event.getSubject().onMessageEvent(event, self)
            else:
                raise AssertionError("Unexpected event:" + event)

        return True
