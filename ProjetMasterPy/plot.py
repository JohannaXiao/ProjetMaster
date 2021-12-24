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
from scipy import optimize

def f_1(x, A, B):
    return A * x + B

fileName = 'nodenum2-1000failedRate01TimerSys.csv'
with open(fileName) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    # initial_timeout,pbft = [],[]
    nodenum = []
    pbft = []
    for row in reader:
        if isinstance(row, list):
            # initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            nodenum.append(float(0) if row[0].isspace() else float(row[0]))
            pbft.append(float(0) if row[1].isspace() else float(row[1]))

        # else:  #     break
    # remove outliners
    # delnode = [100,510,530,590]
    # delnode_pbft = [1.1129374148370441,1.9679618677117297,1.9436666400509963,1.8723211761908416]
    # delnode = [250,290]
    # delnode_pbft = [float(0),1.883966536133234]
    delnode = [442,462,922]
    delnode_pbft = [float(0),float(0),float(0)]
    for node in delnode:
        nodenum.remove(node)
    for i in delnode_pbft:
        pbft.remove(i)

    new_pbft = [x / 1000 for x in pbft]
    plt.style.use(['science', 'no-latex'])
    fig = plt.figure(dpi=128, figsize=(10, 6))
    # plt.plot(initial_timeout,tendermint,label='tendermint')
    # plt.scatter(initial_timeout,tendermint)
    # plt.plot(initial_timeout,algorand, label='algorand')
    # plt.plot(initial_timeout,mir,label='mir')
    plt.plot(nodenum, new_pbft, '--o', label='pbft')
    # plt.scatter(nodenum,pbft)
    plt.grid(linestyle=':')
    plt.xlim(xmax=1000)

    # 直线拟合与绘制
    # nodenum1=nodenum[0:260]
    # pbft1=pbft[0:260]
    # print(nodenum1)
    # print(pbft1)
    # x1=np.arange(0,260,100)
    # A,B = optimize.curve_fit(f_1,nodenum1,pbft1)[0]
    # x1=np.arange(0,260,100)
    # y1=A*x1+B
    # plt.plot(x1,y1,'y')

    # 设置图形的格式
    plt.title("Rate of FailedNode = 0.1", fontsize=12)
    # plt.title("Number of nodes = 1000", fontsize=12)
    # plt.xlabel('Initial Timeout(seconds)', fontsize=16)
    plt.xlabel('Number of nodes', fontsize=16)
    plt.legend()
    # plt.ylabel("Termination Time(ms)", fontsize=16)
    plt.ylabel("Termination Time(seconds)", fontsize=16)
    # plt.tick_params(axis='both', which='major', labelsize=16)
    # plt.savefig("temperature.png", bbox_inches='tight')
    # plt.show()
    plt.savefig("SysTime1000-3.png")



