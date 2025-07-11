from sklearn.datasets import make_blobs, load_iris
import matplotlib.pyplot as plt

# 支持中文
plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

n_samples = 1500
random_state = 170
x, y = make_blobs(n_samples=n_samples, random_state=random_state)
# x, y = load_iris(True) # 莺尾花
print(x.shape, y.shape)
plt.scatter(x[:, 0], x[:, 1], c=y)
plt.title(u"原始数据分布")
plt.show()

1
2
3
4
5
6
7
8
9
10
11
12
13
14
15