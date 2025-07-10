@SpringBootApplication
package com.example.routeplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoutePlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutePlannerApplication.class, args);
        System.out.println("? Java 后端已启动，访问接口示例： http://localhost:8080/route/plan?start=15&end=20&time=100");
    }
}
