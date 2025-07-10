import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import os

# 打印当前路径，确认数据文件位置
print("当前工作目录：", os.getcwd())

# 1. 读取原始 CSV，第一列是时间戳，其余列是节点编号
df = pd.read_csv("newmerged40个观测站数据.csv", encoding="utf-8")
df = df.rename(columns={df.columns[0]: "time"})  # 重命名第一列为 time
df["time"] = df["time"].astype(int)

# 2. 转成长表：列是节点，行为时间，melt 得到 (time, node, volume)
df_long = df.melt(id_vars=["time"], var_name="node", value_name="volume")
df_long["node"] = df_long["node"].astype(int)
print("数据预览：")
print(df_long.head())

# 3. 准备训练数据
X = df_long[["node", "time"]]
y = df_long["volume"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# 4. 训练 XGBoost 模型
model = xgb.XGBRegressor(n_estimators=100, learning_rate=0.1)
model.fit(X_train, y_train)

# 5. 模型评估
y_pred = model.predict(X_test)
mse = mean_squared_error(y_test, y_pred)
print(f"\n模型预测 RMSE（越小越好）: {mse ** 0.5:.2f}")

# 6. 保存模型
if not os.path.exists("models"):
    os.makedirs("models")
model.save_model("models/node_volume_model.json")
print("模型已保存至 models/node_volume_model.json")
