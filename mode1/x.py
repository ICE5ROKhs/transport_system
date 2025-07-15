# api_server.py - 修复版本
# 使用安全的XGBoost加载器

import os
import sys
from flask import Flask, request, jsonify
import numpy as np

# 添加安全加载器
from safe_xgboost_loader import SafeXGBoostLoader

app = Flask(__name__)

# 全局变量
model_loader = None
model = None

def initialize_model():
    """初始化模型"""
    global model_loader, model

    try:
        # 模型路径 - 根据你的实际路径修改
        model_path = "mode1/your_model.model"  # 替换为你的实际模型路径

        print("🚀 初始化XGBoost模型...")

        # 使用安全加载器
        model_loader = SafeXGBoostLoader()
        model = model_loader.load_model(model_path)

        if model is not None:
            print("✅ 模型初始化成功")
            return True
        else:
            print("❌ 模型初始化失败")
            return False

    except Exception as e:
        print(f"❌ 模型初始化异常: {e}")

        # 尝试创建默认模型
        try:
            model_loader = SafeXGBoostLoader()
            model = model_loader._create_default_model("default_model.json")
            print("✅ 使用默认模型")
            return True
        except Exception as e2:
            print(f"❌ 创建默认模型失败: {e2}")
            return False

@app.route('/predict', methods=['POST'])
def predict():
    """预测接口"""
    global model_loader

    try:
        # 获取请求数据
        data = request.get_json()

        if not data or 'features' not in data:
            return jsonify({'error': '缺少features参数'}), 400

        # 转换为numpy数组
        features = np.array(data['features'])

        # 确保特征维度正确
        if features.ndim == 1:
            features = features.reshape(1, -1)

        # 预测
        if model_loader is None:
            return jsonify({'error': '模型未初始化'}), 500

        predictions = model_loader.predict(features)

        # 返回结果
        return jsonify({
            'predictions': predictions.tolist(),
            'status': 'success'
        })

    except Exception as e:
        print(f"❌ 预测失败: {e}")
        return jsonify({'error': f'预测失败: {str(e)}'}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    global model_loader

    status = {
        'status': 'healthy' if model_loader is not None else 'unhealthy',
        'model_loaded': model_loader is not None,
        'message': 'Python API Server is running'
    }

    return jsonify(status)

@app.route('/reload_model', methods=['POST'])
def reload_model():
    """重新加载模型"""
    try:
        success = initialize_model()
        if success:
            return jsonify({'message': '模型重新加载成功', 'status': 'success'})
        else:
            return jsonify({'message': '模型重新加载失败', 'status': 'failed'}), 500
    except Exception as e:
        return jsonify({'message': f'重新加载失败: {str(e)}', 'status': 'error'}), 500

def main():
    """主函数"""
    print("🚀 启动Python API Server...")

    # 初始化模型
    model_success = initialize_model()

    if model_success:
        print("✅ Python API Server 启动成功")
    else:
        print("⚠️  Python API Server 启动（模型加载失败）")

    # 启动Flask服务器
    try:
        app.run(host='0.0.0.0', port=5000, debug=False)
    except Exception as e:
        print(f"❌ Flask服务器启动失败: {e}")

if __name__ == "__main__":
    main()