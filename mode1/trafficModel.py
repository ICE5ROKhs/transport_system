import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import os
import numpy as np

# 打印当前路径，确认数据文件位置
print("当前工作目录：", os.getcwd())

# 1. 读取CSV文件，假设列名为：时间, 节点, 车流量
df = pd.read_csv("newmerged51_transformed.csv", encoding="utf-8")
print(f"原始数据形状: {df.shape}")
print(f"原始数据列名: {df.columns.tolist()}")

# 检查是否有 Unnamed 列并删除
unnamed_cols = [col for col in df.columns if 'Unnamed' in str(col)]
if unnamed_cols:
    print(f"发现并删除 Unnamed 列: {unnamed_cols}")
    df = df.drop(columns=unnamed_cols)

# 重命名列名为标准格式
df.columns = ['time', 'node', 'volume']
print("数据列名已重命名为: time, node, volume")

# 2. 数据类型转换和清理
print("开始数据清理...")

# 转换时间列为整数
df["time"] = pd.to_numeric(df["time"], errors='coerce')
df = df.dropna(subset=['time'])
df["time"] = df["time"].astype(int)

# 转换节点列为整数
df["node"] = pd.to_numeric(df["node"], errors='coerce')
df = df.dropna(subset=['node'])
df["node"] = df["node"].astype(int)

# 转换车流量列为数值型
df["volume"] = pd.to_numeric(df["volume"], errors='coerce')
df = df.dropna(subset=['volume'])

print("数据类型转换完成")
print("数据预览：")
print(df.head())
print(f"清理后数据形状: {df.shape}")

# 检查数据完整性
print(f"\n数据完整性检查:")
print(f"时间范围: {df['time'].min()} - {df['time'].max()}")
print(f"节点数量: {df['node'].nunique()}")
print(f"节点范围: {df['node'].min()} - {df['node'].max()}")
print(f"车流量范围: {df['volume'].min()} - {df['volume'].max()}")

# 3. 准备训练数据
X = df[["node", "time"]]
y = df["volume"]

# 检查是否有足够的数据进行训练
if len(X) == 0:
    print("错误：没有有效的训练数据！")
    exit(1)

print(f"\n训练数据形状: X={X.shape}, y={y.shape}")

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# 4. 训练 XGBoost 模型
print("开始训练模型...")
model = xgb.XGBRegressor(n_estimators=100, learning_rate=0.1, random_state=42)
model.fit(X_train, y_train)

# 5. 模型评估
y_pred = model.predict(X_test)
mse = mean_squared_error(y_test, y_pred)
rmse = mse ** 0.5
print(f"\n模型预测 RMSE（越小越好）: {rmse:.2f}")

# 6. 保存模型
if not os.path.exists("models"):
    os.makedirs("models")
model.save_model("models/node_volume_model.json")
print("模型已保存至 models/node_volume_model.json")

# 7. 模型详细信息
print(f"\n=== 模型训练完成 ===")
print(f"训练样本数: {len(X_train)}")
print(f"测试样本数: {len(X_test)}")

print(f"\n特征重要性:")
feature_importance = model.feature_importances_
feature_names = X.columns
for name, importance in zip(feature_names, feature_importance):
    print(f"  {name}: {importance:.4f}")

print(f"\n交通流量统计信息:")
print(f"最小值: {y.min():.2f}")
print(f"最大值: {y.max():.2f}")
print(f"平均值: {y.mean():.2f}")
print(f"标准差: {y.std():.2f}")
print(f"中位数: {y.median():.2f}")

# 计算相对误差
relative_error = rmse / y.mean() * 100
print(f"相对误差: {relative_error:.2f}%")

# 8. 额外的模型性能分析
print(f"\n=== 模型性能分析 ===")
print(f"预测值统计:")
print(f"  预测最小值: {y_pred.min():.2f}")
print(f"  预测最大值: {y_pred.max():.2f}")
print(f"  预测平均值: {y_pred.mean():.2f}")

# 计算 R² 分数
from sklearn.metrics import r2_score
r2 = r2_score(y_test, y_pred)
print(f"R² 分数: {r2:.4f}")

print(f"\n模型训练和评估完成！")