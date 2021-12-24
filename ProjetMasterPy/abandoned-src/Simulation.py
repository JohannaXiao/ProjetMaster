'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import Node
from Network import *
import Message
from Event import *
from ordered_set import OrderedSet


class Simulation:
    network = None
    eventByTime = OrderedSet()

    def __init__(self, network):
        self.network = network  # self.eventByTime = eventByTime

    def broadcast(self, source, message, time):
        for destination in self.network.getNodes():
            # print("time = %.5f" % time)
            latency = getLatency(source, destination)
            # print("latency = %.5f" % latency)
            arrivalTime = time + latency
            # print("time = %.5f, latency = %.5f, arrivalTime = %.5f" % (time,latency,arrivalTime))
            self.eventByTime.add(MessageEvent(arrivalTime, destination, message))
            self.eventByTime = OrderedSet(sorted(list(self.eventByTime), reverse=True))

    def getNetwork(self):
        return self.network

    def getLeader(self, index):
        return self.network.getLeader(index)

    def scheduleEvent(self, event):
        self.eventByTime.add(event)
        self.eventByTime = OrderedSet(sorted(list(self.eventByTime), reverse=True))

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

            subject = event.getSubject()
            if isinstance(event, TimerEvent):
                subject.onTimerEvent(event, self)
            elif isinstance(event, MessageEvent):
                subject.onMessageEvent(event, self)
            else:
                raise AssertionError("Unexpected event:" + event)

        return True
