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
    eventByTime = OrderedSet()
    def __init__(self,network):
        self.network=network
        # self.eventByTime = eventByTime

    def broadcast(self,source,message,time):
        for destination in self.network.getNodes():
            latency = self.network.getLatency(source,destination)
            arrivalTime = time +latency
            self.eventByTime.append(Event.MessageEvent(arrivalTime,destination,message))

    def getNetwork(self):
        return self.network

    def getLeader(self,index):
        return self.network.getLeader(index)

    def scheduleEvent(self,event):
        self.eventByTime[event.getTime()]=event

    '''
      * Run until all events have been processed, including any newly added events which may be added
   * while running.
   *
   * @param timeLimit the maximum amount of time before the simulation halts
   * @return whether the simulation completed within the time limit 
    '''
    def run(self,timelimit):
        for node in self.network.getNodes():
            node.onStart(self)
        while self.eventByTime is not None:
            event = self.eventByTime.pop()
            if event.getTime()>timelimit:
                return False

            subject = event.getSubject()
            if  isinstance(event,TimerEvent):
                subject.onTimerEvent(event,self)
            elif isinstance(event,MessageEvent):
                subject.onMessageEvent(event,self)
            else:
                raise AssertionError("Unexpected event:" + event)

        return True

