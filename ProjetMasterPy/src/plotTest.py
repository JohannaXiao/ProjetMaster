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

# matplotlib.rc("font",family='SimHei')
plt.rcParams['font.sans-serif'] = 'Songti SC'
plt.rcParams['axes.unicode_minus'] = False




fileName = 'Proof_of_work_data.csv'
with open(fileName) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    Difficulty=[]
    elapsed_time=[]
    hash_power=[]
    for row in reader:
        if isinstance(row, list):
            Difficulty.append(float(0) if row[0].isspace() else float(row[0]))
            elapsed_time.append(float(0) if row[1].isspace() else float(row[1]))
            hash_power.append(float(0) if row[2].isspace() else float(row[2]))


    # plt.style.use(['science', 'no-latex'])
    fig = plt.figure(dpi=128, figsize=(10, 6))

    # plt.plot(Difficulty,hash_power,  '--o')
    plt.plot(Difficulty,elapsed_time,  '--o')
    # plt.scatter(nodenum,pbft)
    plt.grid(linestyle=':')
    # plt.xlim(xmax=1000)


    # 设置图形的格式
    # plt.title("Rate of FailedNode = 0.1", fontsize=12)
    plt.title("PoW共识算法测试", fontsize=16)
    # plt.title("Number of nodes = 1000", fontsize=12)
    # plt.xlabel('Initial Timeout(seconds)', fontsize=16)
    plt.xlabel('难度值', fontsize=12)
    # plt.xlabel('Number of nodes', fontsize=16)
    # plt.legend()
    # plt.ylabel("Termination Time(ms)", fontsize=16)
    # plt.ylabel("单位区块产生所需算力", fontsize=12)
    plt.ylabel("单位区块产生所花费的时间", fontsize=12)
    # plt.tick_params(axis='both', which='major', labelsize=16)
    # plt.savefig("temperature.png", bbox_inches='tight')
    plt.show()
    # plt.savefig("PoSWbftinfluencedByfailednode.png")



