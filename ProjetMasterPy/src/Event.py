'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''

import abc
import numpy as np
import Node
import Message


class Event:

    def __init__(self, time, subject):
        self.time = time
        self.subject = subject

    # 可比较大小
    def __eq__(self, other):
        return self.time==self.time and self.subject==other.subject

    def __lt__(self, other):
        return self.time < other.time

    def __hash__(self):
        return hash((self.time,self.subject))

    def getTime(self):
        return self.time

    def getSubject(self):
        return self.subject

    def compareTo(self, other):
        delta = self.time - other.time
        if delta == 0 and self.subject == other.subject:
            '''
             We shouldn't return 0, since the messages aren't equal. identityHashCode gives us an
            arbitrary but consistent ordering, although this isn't 100% safe since it assumes no
             collisions (which are rare).
            '''
            self_utf = str(self).encode("utf-8")
            other_utf = str(other).encode("utf-8")
            return hashCode(self_utf.decode("utf-8") - other_utf.decode("utf-8"))
        else:
            return np.sign(delta)


class TimerEvent(Event):
    def __init__(self, time, subject):
        super().__init__(time, subject)


class MessageEvent(Event):

    def __init__(self, time, subject, message):
        super().__init__(time, subject)
        self.message = message

    def getMessage(self):
        return self.message


# 参考链接：https://blog.csdn.net/hongxingabc/article/details/82891503
def hashCode(s):
    seed = 31
    h = 0
    for c in s:
        h = np.int32(seed * h) + ord(c)
    return h
