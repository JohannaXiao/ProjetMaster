'''
Version 1.0

Date:

author: Johanna Xiao Xuewen

original data provided by reading file

'''
import pandas as pd
import numpy as np
import csv
from matplotlib import pyplot as plt


fileName1 = 'nodenum2-1000PoSWbftSelectednum250failrate01.csv'
fileName2 = 'nodenum1000PosWbftSelectednum250failrate015.csv'
fileName3 = 'nodenum1000PosWbftSelectednum250failrate02.csv'

with open(fileName1) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    # initial_timeout,PoSWbft = [],[]
    nodenum = []
    PoSWbft_01= []
    for row in reader:
        if isinstance(row, list):
            # initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            nodenum.append(float(0) if row[0].isspace() else float(row[0]))
            PoSWbft_01.append(float(0) if row[1].isspace() else float(row[1]))
with open(fileName2) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    PoSWbft_015 = []
    for row in reader:
        if isinstance(row, list):
            # initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            PoSWbft_015.append(float(0) if row[1].isspace() else float(row[1]))
with open(fileName3) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    PoSWbft_02 = []
    for row in reader:
        if isinstance(row, list):
            # initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            PoSWbft_02.append(float(0) if row[1].isspace() else float(row[1]))



new_PoSWbft_01 = [x / 1000 for x in PoSWbft_01]
new_PoSWbft_015 = [x / 1000 for x in PoSWbft_015]
new_PoSWbft_02 = [x / 1000 for x in PoSWbft_02]
plt.style.use(['science', 'no-latex'])
fig = plt.figure(dpi=128, figsize=(10, 6))
# plt.plot(initial_timeout,tendermint,label='tendermint')
# plt.scatter(initial_timeout,tendermint)
# plt.plot(initial_timeout,algorand, label='algorand')
# plt.plot(initial_timeout,mir,label='mir')
plt.plot(nodenum, new_PoSWbft_01, '--o', label='Fail rate=0.1')
plt.plot(nodenum, new_PoSWbft_015, '--s', label='Fail rate=0.15')
plt.plot(nodenum, new_PoSWbft_02, '--^', label='Fail rate=0.2')

# plt.scatter(nodenum,PoSWbft)
plt.grid(linestyle=':')
plt.xlim(xmax=1000)

# 直线拟合与绘制
# nodenum1=nodenum[0:260]
# PoSWbft1=PoSWbft[0:260]
# print(nodenum1)
# print(PoSWbft1)
# x1=np.arange(0,260,100)
# A,B = optimize.curve_fit(f_1,nodenum1,PoSWbft1)[0]
# x1=np.arange(0,260,100)
# y1=A*x1+B
# plt.plot(x1,y1,'y')

# 设置图形的格式
plt.title("PoSWbft influenced by number of node", fontsize=12)
# plt.title("Number of nodes = 1000", fontsize=12)
# plt.xlabel('Initial Timeout(seconds)', fontsize=16)
plt.xlabel('Number of nodes', fontsize=16)
plt.legend()
# plt.ylabel("Termination Time(ms)", fontsize=16)
plt.ylabel("Termination Time(seconds)", fontsize=16)
# plt.tick_params(axis='both', which='major', labelsize=16)
# plt.savefig("temperature.png", bbox_inches='tight')
# plt.show()
plt.savefig("PoSWbftinfluencedBynodenum.png")



