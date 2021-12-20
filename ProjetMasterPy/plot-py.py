'''
Version 1.0

Date: 

author: Johanna Xiao Xuewen

'''

import pandas as pd
import csv
from matplotlib import pyplot as plt

fileName = 'ten.csv'
with open(fileName) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    initial_timeout,pbft = [],[]
    # initial_timeout,tendermint,algorand,pbft = [],[],[],[]
    for row in reader:
        # initial_timeout.append(float(0) if np.isnan(row[0]) else float(row[0]))
        # tendermint.append(float(0) if np.isnan(row[1]) else float(row[1]))
        # algorand.append(float(0) if np.isnan(row[2]) else float(row[2]))
        # mir.append(float(0) if np.isnan(row[3]) else float(row[3]))
        # initial_timeout.append(float(row[0]))
        # tendermint.append(float(row[1]))
        # algorand.append(float(row[2]))
        # mir.append(float(row[3]))
        # print(type(row))
        if isinstance(row,list):
            initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            # tendermint.append(float(0) if row[1].isspace() else float(row[1]))
            # algorand.append(float(0) if row[2].isspace() else float(row[2]))
            # mir.append(float(0) if row[3].isspace() else float(row[3]))
            # pbft.append(float(0) if row[4].isspace() else float(row[4]))
            # tendermint.append(float(0) if row[1].isspace() else float(row[1]))
            pbft.append(float(0) if row[1].isspace() else float(row[1]))
        # else:
        #     break

    fig = plt.figure(dpi=128, figsize=(10, 6))
    # plt.plot(initial_timeout,tendermint,label='tendermint')
    # plt.plot(initial_timeout,algorand, label='algorand')
    # plt.plot(initial_timeout,mir,label='mir')
    plt.plot(initial_timeout,pbft,label='pbft')
    plt.scatter(initial_timeout,pbft)
    plt.ylim((0,1.75))
    # 设置图形的格式
    plt.title("test", fontsize=24)
    plt.xlabel('Initial Timeout(seconds)', fontsize=16)
    plt.legend()
    plt.ylabel("Termination Time(seconds)", fontsize=16)
    # plt.tick_params(axis='both', which='major', labelsize=16)
    # plt.savefig("temperature.png", bbox_inches='tight')
    plt.show()