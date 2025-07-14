package com.example.routeplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 交通路线规划系统主应用类
 */
@SpringBootApplication
public class TransportSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransportSystemApplication.class, args);
        System.out.println("=== 交通路线规划系统启动成功 ===");
        System.out.println("API 文档: http://localhost:8080/api/route/health");
        System.out.println("示例请求: http://localhost:8080/api/route/plan?startNode=15&endNode=13&timePoint=1");
    }
}