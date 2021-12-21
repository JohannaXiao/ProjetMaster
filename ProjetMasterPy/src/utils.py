'''
Version 1.0

Date: 

author: Johanna Xiao Xuewen

'''

import math
import random
from Vector3d import Vector3d

EARTH_RADIUS = 6.378e6


#   The great-circle distance to another earth position, in meters.
def getDistance(node1, node2):
    product = node1.position.direction.x * node2.position.direction.x + node1.position.direction.y * node2.position.direction.y + node1.position.direction.z * node2.position.direction.z
    product = max(product, -1)
    product = min(product, 1)
    centralAngle = math.acos(product)

    # if not centralAngle:
        # print("centralAngle = %.5f" %centralAngle)
        # print("acos returned NaN")
    return EARTH_RADIUS * centralAngle


def nextDouble(min, max):
    # 设置随机种子
    # random.seed(seed)
    rand = random.random()
    return min + rand * (max - min)


# random position normalnized
def randomPosition():
    while True:
        x = nextDouble(-1, 1)
        y = nextDouble(-1, 1)
        z = nextDouble(-1, 1)
        norm = math.sqrt(x * x + y * y + z * z)
        if norm <= 1:
            x = 1 / norm * x
            y = 1 / norm * y
            z = 1 / norm * z
        direction = Vector3d(x, y, z)
        return direction
