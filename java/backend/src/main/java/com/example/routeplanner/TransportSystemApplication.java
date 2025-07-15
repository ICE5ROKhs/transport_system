package com.example.routeplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

/**
 * 交通路线规划系统主应用类
 */
@SpringBootApplication
public class TransportSystemApplication {

    public static void main(String[] args) {
        // 1. 先启动Python模型服务器
        startPythonModelServer();

        // 2. 启动Java后端服务
        SpringApplication.run(TransportSystemApplication.class, args);
        System.out.println("=== 交通路线规划系统启动成功 ===");
        System.out.println("Java API: http://localhost:8080/api/route/health");
        System.out.println("Python模型API: http://localhost:5000/predict");
        System.out.println("示例请求: http://localhost:8080/api/route/plan?startNode=15&endNode=13&timePoint=1");
    }

    private static void startPythonModelServer() {
        try {
            // 根据你的项目结构，Python脚本在 mode1/api_server.py
            ProcessBuilder pb = new ProcessBuilder("python", "mode1/api_server.py");

            // 设置工作目录为项目根目录
            pb.directory(new java.io.File("../../"));  // 从java/backend回到项目根目录

            pb.start();

            // 等待Python服务器启动
            Thread.sleep(3000);
            System.out.println("✅ Python模型服务器已启动 (端口5000)");

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ 启动Python模型服务器失败: " + e.getMessage());
            System.err.println("请检查Python环境和依赖是否正确安装");
        }
    }
}