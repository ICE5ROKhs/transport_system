package com.example.routeplanner.controller;

import com.example.routeplanner.model.Graph;
import com.example.routeplanner.model.RoutePlanner;
import com.example.routeplanner.util.NodeLoader;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/route")
public class RouteController {

    private static final double DISTANCE_THRESHOLD_KM = 2.0;
    private static final double ALPHA = 0.05;
    private static final String SENSOR_CSV = "data/sensors.csv";  // 请替换为你的文件路径
    private static final String PYTHON_API_URL = "http://127.0.0.1:5000/predict";

    private Graph graph;

    public RouteController() throws IOException {
        // 启动时加载图
        this.graph = NodeLoader.loadGraphFromCSV(SENSOR_CSV, DISTANCE_THRESHOLD_KM);
    }

    @GetMapping("/plan")
    public Map<String, Object> getRoute(
            @RequestParam("start") int startNode,
            @RequestParam("end") int endNode,
            @RequestParam("time") int time
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            RoutePlanner planner = new RoutePlanner(graph, PYTHON_API_URL, ALPHA);
            List<Integer> path = planner.findShortestPath(startNode, endNode, time);

            if (path == null || path.isEmpty()) {
                response.put("status", "fail");
                response.put("message", "? 无可达路径，请检查图或连接距离。");
            } else {
                double total = planner.calculatePathWeight(path, time);
                response.put("status", "success");
                response.put("path", path);
                response.put("totalCongestion", total);
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }
}
//double volume = TrafficPredictor.getPredictedVolume(15, 100);
//System.out.println("预测流量为：" + volume);
