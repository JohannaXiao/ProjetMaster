import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif'] = 'Songti SC'

squares = [1, 4, 9, 16, 25]
# fig表示整个图片，ax表示图表
fig, ax = plt.subplots()
# 绘图，并设置线的宽度
ax.plot(squares, linewidth=3)
# 设置图表的标题，并给坐标轴加上标签
ax.set_title("平方数", fontsize=24)
ax.set_xlabel("值", fontsize=14)
ax.set_ylabel("值的平方", fontsize=14)

# 设置刻度标记的大小
ax.tick_params(axis="both", labelsize=14)

plt.show()


# 查询当前系统所有字体
from matplotlib.font_manager import FontManager
import subprocess

mpl_fonts = set(f.name for f in FontManager().ttflist)

print('all font list get from matplotlib.font_manager:')
for f in sorted(mpl_fonts):
    print('\t' + f)

import matplotlib.pyplot as plt
# 定义文本框和箭头格式
decisionNode = dict(boxstyle = "sawtooth", fc = "0.8")
leafNode = dict(boxstyle = "round4", fc = "0.8")
arrow_args = dict(arrowstyle = "<-")
# 绘制带箭头的注解
def plotNode(nodeTxt, centerPt, parentPt, nodeType) :
  createPlot.ax1.annotate(nodeTxt, xy = parentPt, xycoords = 'axes fraction', xytext = centerPt, textcoords = 'axes fraction', va = 'center', ha = 'center', bbox = nodeType, arrowprops = arrow_args)
def createPlot() :
  fig = plt.figure(1, facecolor='white')
  fig.clf()
  createPlot.ax1 = plt.subplot(111, frameon = False)
  plotNode(U'决策节点', (0.5, 0.1), (0.1, 0.5), decisionNode)
  plotNode(U'叶节点', (0.8, 0.1), (0.3, 0.8), leafNode)
  plt.show()
createPlot()


