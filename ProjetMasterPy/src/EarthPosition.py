'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''
import math
import random
import Vector3d

EARTH_RADIUS = 6.378e6
class EarthPosition:

    def __init__(self,direction):
        self.direction = direction

    def __eq__(self, other):
        return self.direction == other.direction

#   The great-circle distance to another earth position, in meters.
    def getDistance(self,node):
        product = self.direction.dotProduct(node.direction)

        product = max(product,-1)
        product =min(product,1)
        centralAngle = math.acos(product)

        if centralAngle is None:
            print("acos returned NaN")
        return EARTH_RADIUS * centralAngle


    def nextDouble(self, rand,min, max):
        rand = random.seed(rand)
        return min+rand.nextDouble()*(max-min)

    def randomPosition(self,rand):
        while True:
            x = self.nextDouble(rand,-1,1)
            y = self.nextDouble(rand,-1,1)
            z = self.nextDouble(rand,-1,1)
            point = Vector3d(x,y,z)
            if point.norm()<=-1:
                return point.normalized()

