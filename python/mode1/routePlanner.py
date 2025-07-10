import pandas as pd
import numpy as np
import networkx as nx
import xgboost as xgb
from math import radians, cos, sin, asin, sqrt
import matplotlib.pyplot as plt

# === 参数 ===
ALPHA = 0.05  # 拥堵系数中的车流权重
DISTANCE_THRESHOLD_KM = 2.0  # 最大连接距离（单位：km）
TIME_POINT = 100
START_NODE = 15
END_NODE = 20

# === 1. 加载经纬度节点数据 ===
node_df = pd.read_csv("sensors北交周边.csv", dtype={"sensor_id": str}).dropna()
node_df["sensor_id"] = node_df["sensor_id"].astype(int)
edge_df = pd.read_csv("edges.csv")
# === 2. 构建图：自动连接临近节点 ===
G = nx.Graph()

def haversine(lon1, lat1, lon2, lat2):
    # 计算两点间地球表面距离（km）
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * asin(sqrt(a))
    r = 6371  # 地球平均半径
    return c * r

# 加入所有节点
for _, row in node_df.iterrows():
    G.add_node(int(row.sensor_id), pos=(row.latitude, row.longitude))

# 加入边：根据表中指定的连接
for _, row in edge_df.iterrows():
    id1 = int(row['from'])
    id2 = int(row['to'])

    lat1, lon1 = node_df.loc[node_df['sensor_id'] == id1, ['latitude', 'longitude']].values[0]
    lat2, lon2 = node_df.loc[node_df['sensor_id'] == id2, ['latitude', 'longitude']].values[0]

    d = haversine(lon1, lat1, lon2, lat2)
    G.add_edge(id1, id2, distance=d)

# === 3. 加载流量预测模型 ===
model = xgb.XGBRegressor()
model.load_model("models/node_volume_model.json")

# === 4. 为每条边计算 拥堵系数 ===
for u, v, data in G.edges(data=True):
    d = data["distance"]
    flow_u = model.predict(np.array([[u, TIME_POINT]]))[0]
    flow_v = model.predict(np.array([[v, TIME_POINT]]))[0]
    avg_flow = (flow_u + flow_v) / 2
    congestion = d * (1 + ALPHA * avg_flow)
    data["weight"] = congestion
# === 5. 计算最优路径 ===
path = None
try:
    path = nx.shortest_path(G, source=START_NODE, target=END_NODE, weight="weight")
    total = nx.shortest_path_length(G, source=START_NODE, target=END_NODE, weight="weight")
    print("✅ 推荐路径：", " → ".join(map(str, path)))
    print(f"总拥堵系数：{total:.2f}")
except nx.NetworkXNoPath:
    print("❌ 起点与终点之间无可达路径，请检查图结构或连接阈值。")

# === 6. 可视化图结构 ===
plt.figure(figsize=(12, 8))
pos = nx.get_node_attributes(G, 'pos')
nx.draw_networkx_nodes(G, pos, node_size=50, node_color='skyblue')
nx.draw_networkx_edges(G, pos, width=0.5, edge_color='gray')
nx.draw_networkx_labels(G, pos, font_size=8)

# 如果找到路径，高亮显示路径
if path:
    edge_path = list(zip(path[:-1], path[1:]))
    nx.draw_networkx_edges(G, pos, edgelist=edge_path, edge_color='red', width=2)
    nx.draw_networkx_nodes(G, pos, nodelist=path, node_color='red', node_size=70)

plt.title(f"visible graph( {G.number_of_nodes()} points, {G.number_of_edges()} sides)")
plt.axis('off')
plt.tight_layout()
plt.show()




