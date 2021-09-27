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

fileName = 'data_20210908_1400.csv'
with open(fileName) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    initial_timeout,tendermint,algorand,mir = [],[],[],[]

    for row in reader:
        # initial_timeout.append(float(0) if np.isnan(row[0]) else float(row[0]))
        # tendermint.append(float(0) if np.isnan(row[1]) else float(row[1]))
        # algorand.append(float(0) if np.isnan(row[2]) else float(row[2]))
        # mir.append(float(0) if np.isnan(row[3]) else float(row[3]))
        # initial_timeout.append(float(row[0]))
        # tendermint.append(float(row[1]))
        # algorand.append(float(row[2]))
        # mir.append(float(row[3]))
        initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
        tendermint.append(float(0) if row[1].isspace() else float(row[1]))
        algorand.append(float(0) if row[2].isspace() else float(row[2]))
        mir.append(float(0) if row[3].isspace() else float(row[3]))
    print(initial_timeout)

    fig = plt.figure(dpi=128, figsize=(10, 6))
    plt.plot(initial_timeout,tendermint)
    plt.plot(initial_timeout,algorand)
    plt.plot(initial_timeout,mir)
    # 设置图形的格式
    plt.title("test", fontsize=24)
    plt.xlabel('', fontsize=16)
    # plt.legend()
    # plt.ylabel("Temperature (F)", fontsize=16)
    # plt.tick_params(axis='both', which='major', labelsize=16)
    # plt.savefig("temperature.png", bbox_inches='tight')
    plt.show()