'''
Version 1.0

Date: 2021.12.16

author: Johanna Xiao Xuewen

'''
import math


class Vector3d:
    '3D位置信息 x、y、z'

    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z

    def __eq__(self, other):
        if self.x == other.x and self.y == other.y and self.z == other.z:
            return True
        else:
            return False

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
