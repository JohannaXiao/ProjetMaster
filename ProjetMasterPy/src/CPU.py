import matplotlib.pyplot as plt
import psutil
import time
import datetime
#获得想要进程的信息(用进程名找到进程号)
def get_process_info(process_name):
    '''
    获取进程信息
    :param process_name: 输入进程名
    '''
    for i in psutil.pids():
        pid = None
        p =  psutil.Process(i)
        if p.name() == process_name:
            pid = i
            break
    with open("E:\python\Py_29\data.txt",mode="a") as f:
        for i in range(10):
                cpu_list = psutil.Process(pid).cpu_times()
                mem_list = psutil.Process(pid).memory_info()
                time_ack = datetime.datetime.now().strftime("%M:%S")
                mem_prent = mem_list[0] / mem_list[1]
                f.write(str(time_ack) + "-" + str(mem_prent) + "-" + str(cpu_list[0]) + "-" +"\n")
                time.sleep(1)

get_process_info("Pycharm.exe")
with open("case.log") as f:
    cenent = f.readlines()
cpu_list = []
mem_list = []
time_list = []
for i in cenent:
    list_a = i.split("-")
    time_list.append(list_a[0])
    cpu_list.append(list_a[2])
    mem_list.append(list_a[1])

plt.rcParams['font.sans-serif']=['SimHei']  #中文标示
plt.legend(loc='best') #图例显示及位置
plt.xticks(rotation=60)
plt.plot(cpu_list,time_list,'r',label='cpu')  #红色标示cpu，且图例名字为cpu
# plt.plot(y,x1,'b',label='men')  #红色标示cpu，且图例名字为cpu
plt.xlabel('时间') #X轴标题
plt.ylabel('cpu占用率')#y轴标题
plt.title('某某进程cpu内存占用图') #标题
plt.grid(True)#显示网格
plt.gcf().autofmt_xdate()
plt.show()

