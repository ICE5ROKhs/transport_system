import pandas as pd
import numpy as np
import os
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.preprocessing import StandardScaler
import time

# 导入各种模型
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, AdaBoostRegressor
from sklearn.linear_model import LinearRegression, Ridge, Lasso, ElasticNet
from sklearn.svm import SVR
from sklearn.neighbors import KNeighborsRegressor
from sklearn.tree import DecisionTreeRegressor
from sklearn.neural_network import MLPRegressor
import xgboost as xgb
from sklearn.ensemble import ExtraTreesRegressor
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF

# 可视化库
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.patches import Rectangle
import matplotlib.patches as patches

# 设置中文字体和样式
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'DejaVu Sans',
                                   'WenQuanYi Zen Hei', 'STHeiti', 'sans-serif']
plt.rcParams['axes.unicode_minus'] = False
plt.rcParams['font.family'] = 'sans-serif'

plt.style.use('default')  # 使用默认样式
sns.set_palette("husl")

# 忽略警告
import warnings
warnings.filterwarnings('ignore')

print("当前工作目录：", os.getcwd())

# 1. 数据读取和预处理
print("正在读取数据...")
df = pd.read_csv("newmerged51_transformed.csv", encoding="utf-8")  # 修改为您的新文件名

# 检查是否有 Unnamed 列并删除
unnamed_cols = [col for col in df.columns if 'Unnamed' in str(col)]
if unnamed_cols:
    print(f"发现并删除 Unnamed 列: {unnamed_cols}")
    df = df.drop(columns=unnamed_cols)

# 重命名列名为标准格式（假设第一列是时间，第二列是节点，第三列是车流量）
df.columns = ['time', 'node', 'volume']
print("数据列名已重命名为: time, node, volume")

print(f"原始数据形状: {df.shape}")
print("数据预览：")
print(df.head())

# 2. 数据类型转换和清理
print("开始数据清理...")

# 转换数据类型
df["time"] = pd.to_numeric(df["time"], errors='coerce')
df = df.dropna(subset=['time'])
df["time"] = df["time"].astype(int)

df["node"] = pd.to_numeric(df["node"], errors='coerce')
df = df.dropna(subset=['node'])
df["node"] = df["node"].astype(int)

df["volume"] = pd.to_numeric(df["volume"], errors='coerce')
df = df.dropna(subset=['volume'])

print("数据类型转换完成")
print(f"清理后数据形状: {df.shape}")

# 3. 异常值处理
print("处理异常值...")
Q1 = df['volume'].quantile(0.25)
Q3 = df['volume'].quantile(0.75)
IQR = Q3 - Q1
lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR

outliers = df[(df['volume'] < lower_bound) | (df['volume'] > upper_bound)]
print(f"异常值数量: {len(outliers)} ({len(outliers) / len(df) * 100:.2f}%)")

# 移除异常值
df_clean = df[(df['volume'] >= lower_bound) & (df['volume'] <= upper_bound)]
print(f"清理后数据形状: {df_clean.shape}")

# 4. 特征工程
print("创建新特征...")
df_clean = df_clean.copy()

# 时间特征
df_clean['hour'] = (df_clean['time'] % 24).astype('int64')
df_clean['day'] = ((df_clean['time'] // 24) % 30).astype('int64')
df_clean['day_of_week'] = ((df_clean['time'] // 24) % 7).astype('int64')
df_clean['is_weekend'] = df_clean['day_of_week'].isin([5, 6]).astype('int64')

# 判断高峰时段
df_clean['is_morning_peak'] = df_clean['hour'].between(7, 9).astype('int64')
df_clean['is_evening_peak'] = df_clean['hour'].between(17, 19).astype('int64')
df_clean['is_night'] = df_clean['hour'].between(22, 6).astype('int64')

# 节点统计特征
node_stats = df_clean.groupby('node')['volume'].agg(['mean', 'std']).reset_index()
node_stats.columns = ['node', 'node_avg_volume', 'node_std_volume']
node_stats['node'] = node_stats['node'].astype('int64')
df_clean = df_clean.merge(node_stats, on='node', how='left')

# 时间统计特征
time_stats = df_clean.groupby('hour')['volume'].agg(['mean', 'std']).reset_index()
time_stats.columns = ['hour', 'hour_avg_volume', 'hour_std_volume']
time_stats['hour'] = time_stats['hour'].astype('int64')
df_clean = df_clean.merge(time_stats, on='hour', how='left')

print("特征工程完成")

# 5. 准备训练数据
feature_cols = ['node', 'time', 'hour', 'day', 'day_of_week', 'is_weekend',
                'is_morning_peak', 'is_evening_peak', 'is_night',
                'node_avg_volume', 'node_std_volume', 'hour_avg_volume', 'hour_std_volume']

X = df_clean[feature_cols]
y = df_clean['volume']

# 为了提高运行速度，我们使用一个较小的样本进行模型对比
print("采样数据以提高运行速度...")
if len(X) > 50000:
    sample_size = 50000
    sample_indices = np.random.choice(len(X), sample_size, replace=False)
    X_sample = X.iloc[sample_indices]
    y_sample = y.iloc[sample_indices]
else:
    X_sample = X
    y_sample = y

print(f"使用样本大小: {len(X_sample)}")

# 特征标准化
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X_sample)

# 数据分割
X_train, X_test, y_train, y_test = train_test_split(
    X_scaled, y_sample, test_size=0.2, random_state=42
)

print(f"训练集大小: {X_train.shape}")
print(f"测试集大小: {X_test.shape}")

# 6. 定义所有模型
models = {
    # 集成方法
    'Random Forest': RandomForestRegressor(n_estimators=100, random_state=42, n_jobs=-1),
    'Extra Trees': ExtraTreesRegressor(n_estimators=100, random_state=42, n_jobs=-1),
    'Gradient Boosting': GradientBoostingRegressor(n_estimators=100, random_state=42),
    'AdaBoost': AdaBoostRegressor(n_estimators=100, random_state=42),
    'XGBoost': xgb.XGBRegressor(n_estimators=100, random_state=42, n_jobs=-1),

    # 线性模型
    'Linear Regression': LinearRegression(),
    'Ridge': Ridge(alpha=1.0),
    'Lasso': Lasso(alpha=1.0),
    'ElasticNet': ElasticNet(alpha=1.0, l1_ratio=0.5),

    # 支持向量机
    'SVR (RBF)': SVR(kernel='rbf', C=1.0),
    'SVR (Linear)': SVR(kernel='linear', C=1.0),

    # 基于邻居的方法
    'K-Neighbors': KNeighborsRegressor(n_neighbors=5, n_jobs=-1),

    # 决策树
    'Decision Tree': DecisionTreeRegressor(random_state=42),

    # 神经网络
    'Neural Network': MLPRegressor(hidden_layer_sizes=(100, 50), max_iter=500, random_state=42),
}

# 7. 模型训练和评估
results = {}
training_times = {}
predictions = {}

print("\n开始模型训练和评估...")
print("=" * 80)

for name, model in models.items():
    print(f"正在训练: {name}")

    try:
        # 计算训练时间
        start_time = time.time()
        model.fit(X_train, y_train)
        training_time = time.time() - start_time
        training_times[name] = training_time

        # 预测
        y_pred = model.predict(X_test)
        predictions[name] = y_pred

        # 评估指标
        rmse = np.sqrt(mean_squared_error(y_test, y_pred))
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        relative_error = rmse / y_test.mean() * 100

        results[name] = {
            'RMSE': rmse,
            'MAE': mae,
            'R2': r2,
            'Relative_Error': relative_error,
            'Training_Time': training_time
        }

        print(f"  ✓ 完成 - RMSE: {rmse:.2f}, R²: {r2:.3f}")

    except Exception as e:
        print(f"  ✗ 失败: {str(e)}")
        continue

print("\n" + "=" * 80)

# 8. 结果汇总和排序
print("\n模型性能对比结果:")
print("=" * 100)
print(f"{'模型名称':<20} {'RMSE':<10} {'MAE':<10} {'R²':<10} {'相对误差%':<12} {'训练时间(s)':<12}")
print("-" * 100)

# 按RMSE排序
sorted_results = sorted(results.items(), key=lambda x: x[1]['RMSE'])

for name, metrics in sorted_results:
    print(f"{name:<20} {metrics['RMSE']:<10.2f} {metrics['MAE']:<10.2f} "
          f"{metrics['R2']:<10.3f} {metrics['Relative_Error']:<12.2f} "
          f"{metrics['Training_Time']:<12.2f}")

# 9. 最佳模型分析
best_model_name = sorted_results[0][0]
best_metrics = sorted_results[0][1]

print(f"\n最佳模型: {best_model_name}")
print(f"最佳RMSE: {best_metrics['RMSE']:.2f}")
print(f"最佳相对误差: {best_metrics['Relative_Error']:.2f}%")
print(f"最佳R²: {best_metrics['R2']:.3f}")

# 10. 性能分析
print(f"\n性能分析:")
print("-" * 50)

# 找出最快的模型
fastest_model = min(training_times.items(), key=lambda x: x[1])
print(f"最快训练模型: {fastest_model[0]} ({fastest_model[1]:.2f}s)")

# 找出最高准确度的模型
highest_r2 = max(results.items(), key=lambda x: x[1]['R2'])
print(f"最高R²模型: {highest_r2[0]} (R²={highest_r2[1]['R2']:.3f})")

# 找出最低相对误差的模型
lowest_relative_error = min(results.items(), key=lambda x: x[1]['Relative_Error'])
print(f"最低相对误差模型: {lowest_relative_error[0]} ({lowest_relative_error[1]['Relative_Error']:.2f}%)")

# 11. 推荐建议
print(f"\n推荐建议:")
print("-" * 50)

if best_metrics['Relative_Error'] < 20:
    print("✓ 模型性能良好 (相对误差 < 20%)")
elif best_metrics['Relative_Error'] < 30:
    print("⚠ 模型性能中等 (相对误差 20-30%)")
else:
    print("✗ 模型性能较差 (相对误差 > 30%)")

print(f"\n根据不同需求的推荐:")
print(f"• 追求最高精度: {best_model_name}")
print(f"• 追求训练速度: {fastest_model[0]}")
print(f"• 平衡性能和速度: {sorted_results[1][0] if len(sorted_results) > 1 else best_model_name}")

# 12. 保存最佳模型
if not os.path.exists("models"):
    os.makedirs("models")

best_model = models[best_model_name]
import joblib

joblib.dump(best_model, "models/best_model.pkl")
joblib.dump(scaler, "models/scaler.pkl")

print(f"\n最佳模型 ({best_model_name}) 已保存到 models/best_model.pkl")
print("标准化器已保存到 models/scaler.pkl")

# 13. 特征重要性分析（如果支持）
if hasattr(best_model, 'feature_importances_'):
    print(f"\n{best_model_name} 特征重要性:")
    print("-" * 40)
    feature_importance = pd.DataFrame({
        'feature': feature_cols,
        'importance': best_model.feature_importances_
    }).sort_values('importance', ascending=False)

    for idx, row in feature_importance.head(10).iterrows():
        print(f"{row['feature']:<20}: {row['importance']:.4f}")

# ================================
# 14. 生成可视化图表
# ================================

print("\n开始生成可视化图表...")

# 创建结果DataFrame便于可视化
results_df = pd.DataFrame(results).T
results_df.reset_index(inplace=True)
results_df.rename(columns={'index': 'Model'}, inplace=True)

# 创建图表保存目录
if not os.path.exists("charts"):
    os.makedirs("charts")

# 设置图表样式
plt.style.use('default')
colors = plt.cm.Set3(np.linspace(0, 1, len(results_df)))

# 图表1：模型性能对比柱状图
fig, axes = plt.subplots(2, 2, figsize=(16, 12))
fig.suptitle('模型性能对比分析', fontsize=16, fontweight='bold')

# RMSE对比
ax1 = axes[0, 0]
bars1 = ax1.bar(range(len(results_df)), results_df['RMSE'], color=colors)
ax1.set_xlabel('模型')
ax1.set_ylabel('RMSE')
ax1.set_title('RMSE 对比 (越小越好)')
ax1.set_xticks(range(len(results_df)))
ax1.set_xticklabels(results_df['Model'], rotation=45, ha='right')
ax1.grid(axis='y', alpha=0.3)

# 添加数值标签
for i, bar in enumerate(bars1):
    height = bar.get_height()
    ax1.text(bar.get_x() + bar.get_width() / 2., height,
             f'{height:.1f}', ha='center', va='bottom', fontsize=8)

# R²对比
ax2 = axes[0, 1]
bars2 = ax2.bar(range(len(results_df)), results_df['R2'], color=colors)
ax2.set_xlabel('模型')
ax2.set_ylabel('R²')
ax2.set_title('R² 对比 (越大越好)')
ax2.set_xticks(range(len(results_df)))
ax2.set_xticklabels(results_df['Model'], rotation=45, ha='right')
ax2.grid(axis='y', alpha=0.3)

# 添加数值标签
for i, bar in enumerate(bars2):
    height = bar.get_height()
    ax2.text(bar.get_x() + bar.get_width() / 2., height,
             f'{height:.3f}', ha='center', va='bottom', fontsize=8)

# MAE对比
ax3 = axes[1, 0]
bars3 = ax3.bar(range(len(results_df)), results_df['MAE'], color=colors)
ax3.set_xlabel('模型')
ax3.set_ylabel('MAE')
ax3.set_title('MAE 对比 (越小越好)')
ax3.set_xticks(range(len(results_df)))
ax3.set_xticklabels(results_df['Model'], rotation=45, ha='right')
ax3.grid(axis='y', alpha=0.3)

# 添加数值标签
for i, bar in enumerate(bars3):
    height = bar.get_height()
    ax3.text(bar.get_x() + bar.get_width() / 2., height,
             f'{height:.1f}', ha='center', va='bottom', fontsize=8)

# 训练时间对比
ax4 = axes[1, 1]
bars4 = ax4.bar(range(len(results_df)), results_df['Training_Time'], color=colors)
ax4.set_xlabel('模型')
ax4.set_ylabel('训练时间 (秒)')
ax4.set_title('训练时间对比 (越小越好)')
ax4.set_xticks(range(len(results_df)))
ax4.set_xticklabels(results_df['Model'], rotation=45, ha='right')
ax4.grid(axis='y', alpha=0.3)

# 添加数值标签
for i, bar in enumerate(bars4):
    height = bar.get_height()
    ax4.text(bar.get_x() + bar.get_width() / 2., height,
             f'{height:.2f}', ha='left', va='center', fontsize=8)

plt.tight_layout()
plt.savefig('charts/model_comparison_bars.png', dpi=300, bbox_inches='tight')
plt.show()

# 图表2：模型性能热力图
fig, ax = plt.subplots(figsize=(12, 8))
# 标准化数据用于热力图
normalized_data = results_df[['RMSE', 'MAE', 'R2', 'Relative_Error', 'Training_Time']].copy()
# 对于越小越好的指标，使用倒数
normalized_data['RMSE'] = 1 / normalized_data['RMSE']
normalized_data['MAE'] = 1 / normalized_data['MAE']
normalized_data['Relative_Error'] = 1 / normalized_data['Relative_Error']
normalized_data['Training_Time'] = 1 / normalized_data['Training_Time']

# 标准化到0-1范围
from sklearn.preprocessing import MinMaxScaler

scaler_viz = MinMaxScaler()
normalized_data_scaled = scaler_viz.fit_transform(normalized_data)

# 创建热力图
im = ax.imshow(normalized_data_scaled, cmap='RdYlGn', aspect='auto')
ax.set_xticks(range(len(normalized_data.columns)))
ax.set_xticklabels(['RMSE', 'MAE', 'R²', 'Relative_Error', 'Training_Time'])
ax.set_yticks(range(len(results_df)))
ax.set_yticklabels(results_df['Model'])
ax.set_title('模型性能热力图 (绿色=更好)', fontsize=14, fontweight='bold')

# 添加数值标签
for i in range(len(results_df)):
    for j in range(len(normalized_data.columns)):
        text = ax.text(j, i, f'{normalized_data_scaled[i, j]:.2f}',
                       ha="center", va="center", color="black", fontsize=8)

plt.colorbar(im, ax=ax, label='性能得分 (0-1)')
plt.tight_layout()
plt.savefig('charts/model_performance_heatmap.png', dpi=300, bbox_inches='tight')
plt.show()

# 图表3：性能vs训练时间散点图
fig, ax = plt.subplots(figsize=(12, 8))
scatter = ax.scatter(results_df['Training_Time'], results_df['RMSE'],
                     c=results_df['R2'], s=100, alpha=0.7, cmap='viridis')

# 添加模型名称标签
for i, txt in enumerate(results_df['Model']):
    ax.annotate(txt, (results_df['Training_Time'].iloc[i], results_df['RMSE'].iloc[i]),
                xytext=(5, 5), textcoords='offset points', fontsize=9)

ax.set_xlabel('训练时间 (秒)')
ax.set_ylabel('RMSE')
ax.set_title('模型性能 vs 训练时间 (气泡颜色代表R²)')
ax.grid(True, alpha=0.3)
plt.colorbar(scatter, label='R²')
plt.tight_layout()
plt.savefig('charts/performance_vs_time.png', dpi=300, bbox_inches='tight')
plt.show()

print("\n" + "=" * 60)
print("📊 分析完成！主要图表已生成")
print("=" * 60)
print("生成的图表包括：")
print("1. 📊 model_comparison_bars.png - 模型性能对比柱状图")
print("2. 🔥 model_performance_heatmap.png - 模型性能热力图")
print("3. 💫 performance_vs_time.png - 性能vs训练时间散点图")
print(f"\n所有图表已保存到 'charts/' 目录")
print("=" * 60)

print(f"\n✅ 综合模型对比分析完成！")
print(f"📁 模型文件: models/best_model.pkl")
print(f"📁 标准化器: models/scaler.pkl")
print(f"📁 可视化图表: charts/目录")