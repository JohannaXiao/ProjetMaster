'''
Version 1.0

Date: 

author: Johanna Xiao Xuewen

'''

import pandas as pd
import csv
from matplotlib import pyplot as plt

fileName = '20211220_1804_failednode.csv'
with open(fileName) as f:
    reader = csv.reader(f)
    header_row = next(reader)

    initial_timeout,pbft,pbft2 = [],[],[]
    for row in reader:
        if isinstance(row,list):
            initial_timeout.append(float(0) if row[0].isspace() else float(row[0]))
            pbft.append(float(0) if row[1].isspace() else float(row[1]))
            pbft2.append(float(0) if row[2].isspace() else float(row[2]))
        # else:
        #     break

    fig = plt.figure(dpi=128, figsize=(10, 6))
    plt.plot(initial_timeout,pbft,label='0.2')
    plt.plot(initial_timeout,pbft2,label='0.1')
    # plt.scatter(initial_timeout,pbft)
    # plt.ylim((0,1.4))
    # 设置图形的格式
    plt.title("test", fontsize=24)
    plt.xlabel('Failed Node Number', fontsize=16)
    plt.legend()
    plt.ylabel("Termination Time(seconds)", fontsize=16)
    # plt.tick_params(axis='both', which='major', labelsize=16)
    # plt.savefig("temperature.png", bbox_inches='tight')
    plt.show()