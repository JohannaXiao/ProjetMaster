'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''
import math


class Vector3d(object):
    '3D位置信息 x、y、z'
    x=0
    y=0
    z=0

    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z

    def __eq__(self, other):
        if self.x == other.x and self.y == other.y and self.z == other.z:
            return True
        else:
            return False

    def __lt__(self, other):
        if self.x!=other.x:
            return self.x<other.x
        elif self.y!= other.y:
            return self.y<other.y
        elif self.z!=other.z:
            return self.z<other.z
        else:
            return self.x==other.x

    def __hash__(self):
        return hash((self.x,self.y,self.z))

    def norm(self):
        return math.sqrt(self.x * self.x + self.y * self.y + self.z * self.z)

    def scaled(self, s):
        return Vector3d(s * self.x, s * self.y, s * self.z)

    def normalized(self):
        return self.scaled(1 / self.norm())

    def dotProduct(self, that):
        return self.x * that.x + self.y * that.y + self.z * that.z

    def toString(self):
        return (str(self.x, str(self.y), str(self.z)))
