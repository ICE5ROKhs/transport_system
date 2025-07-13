
import numpy as np
import xgboost as xgb
import pickle

# 加载模型和信息
model = xgb.XGBRegressor()
model.load_model("models/traffic_flow_model.json")

with open("models/model_info.pkl", "rb") as f:
    model_info = pickle.load(f)

def predict_traffic_flow(node, hour, day=None):
    """
    预测交通流量

    Parameters:
    node: 节点编号
    hour: 小时 (0-23)
    day: 可选的天数，如果不提供，将使用数据中的平均天数

    Returns:
    预测的交通流量值
    """
    # 如果没有提供天数，使用一个合理的默认值
    if day is None:
        day = 72  # 使用中间值作为默认天数

    # 计算所有特征
    time_stamp = day * 24 + hour
    day_of_week = day % 7
    is_weekend = int(day_of_week >= 5)
    hour_sin = np.sin(2 * np.pi * hour / 24)
    hour_cos = np.cos(2 * np.pi * hour / 24)

    # 按照训练时的特征顺序构造特征向量
    features = [node, day, hour, time_stamp, day_of_week, is_weekend, hour_sin, hour_cos]

    # 预测
    prediction = model.predict(np.array([features]))[0]
    return prediction

# 简化的预测函数，只需要节点和小时
def predict_flow_simple(node, hour):
    """
    简化的预测函数，只需要节点和小时
    这是为了兼容原有代码: flow_u = model.predict(np.array([[u, TIME_POINT]]))[0]
    """
    return predict_traffic_flow(node, hour)

# 为了完全兼容原有代码，还可以创建一个模拟的predict方法
class TrafficFlowPredictor:
    def __init__(self):
        self.model = xgb.XGBRegressor()
        self.model.load_model("models/traffic_flow_model.json")

        with open("models/model_info.pkl", "rb") as f:
            self.model_info = pickle.load(f)

        self.default_day = 72

    def predict(self, X):
        """
        兼容原有代码的预测方法
        X: 形状为 (n_samples, 2) 的数组，每行是 [node, hour]
        """
        predictions = []
        for row in X:
            node, hour = row
            prediction = self.predict_traffic_flow(node, hour)
            predictions.append(prediction)
        return np.array(predictions)

    def predict_traffic_flow(self, node, hour, day=None):
        if day is None:
            day = self.default_day

        time_stamp = day * 24 + hour
        day_of_week = day % 7
        is_weekend = int(day_of_week >= 5)
        hour_sin = np.sin(2 * np.pi * hour / 24)
        hour_cos = np.cos(2 * np.pi * hour / 24)

        features = [node, day, hour, time_stamp, day_of_week, is_weekend, hour_sin, hour_cos]
        prediction = self.model.predict(np.array([features]))[0]
        return prediction

# 使用示例：
# 方法1：直接使用预测函数
# flow = predict_flow_simple(node=1, hour=8)

# 方法2：使用兼容类（完全兼容原有代码）
# predictor = TrafficFlowPredictor()
# flow_u = predictor.predict(np.array([[u, TIME_POINT]]))[0]
