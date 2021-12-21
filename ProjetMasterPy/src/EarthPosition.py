'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''
import math
import random
from Vector3d import Vector3d

EARTH_RADIUS = 6.378e6
class EarthPosition:

    def __init__(self,direction):
        self.direction = direction
        # self.x = x
        # self.y = y
        # self.z = z

    def __eq__(self, other):
        return self.direction == other.direction

    def __lt__(self, other):
        return self.direction<other.direction

    def __hash__(self):
        return hash(self.direction)

    def nextDouble(self,min,max):
        # 设置随机种子
        # random.seed(seed)
        rand = random.random()
        return min+rand*(max-min)

    # def randomPosition(self):
    #     while True:
    #         # x = self.nextDouble(seed,-1,1)
    #         # y = self.nextDouble(seed,-1,1)
    #         # z = self.nextDouble(seed,-1,1)
    #         x = self.nextDouble(-1,1)
    #         y = self.nextDouble(-1,1)
    #         z = self.nextDouble(-1,1)
    #         self.direction = Vector3d(x,y,z)
    #         if self.direction.norm()<=1:
    #             return self.direction.normalized()



