package com.example.routeplanner.controller;

import com.example.routeplanner.model.Edge;
import com.example.routeplanner.model.Node;
import com.example.routeplanner.model.RouteRequest;
import com.example.routeplanner.model.RouteResponse;
import com.example.routeplanner.service.PythonModelService;
import com.example.routeplanner.service.RoutePlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路线规划控制器
 */
@RestController
@RequestMapping("/route")
@CrossOrigin(origins = "*")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    private RoutePlannerService routePlannerService;

    @Autowired
    private PythonModelService pythonModelService;

    /**
     * 规划最优路径
     */
    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planRoute(@RequestBody RouteRequest request) {
        logger.info("收到路线规划请求: {}", request);

        try {
            RouteResponse response = routePlannerService.planRoute(request);
            logger.info("路线规划完成: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("路线规划失败: {}", e.getMessage());
            RouteResponse errorResponse = new RouteResponse(false, "路线规划失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取所有节点信息
     */
    @GetMapping("/nodes")
    public ResponseEntity<Map<Integer, Node>> getAllNodes() {
        try {
            Map<Integer, Node> nodes = routePlannerService.getAllNodes();
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            logger.error("获取节点信息失败: {}", e.getMessage());
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }

    /**
     * 获取所有边信息
     */
    @GetMapping("/edges")
    public ResponseEntity<List<Edge>> getAllEdges() {
        try {
            List<Edge> edges = routePlannerService.getAllEdges();
            return ResponseEntity.ok(edges);
        } catch (Exception e) {
            logger.error("获取边信息失败: {}", e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * 预测指定节点的流量
     */
    @GetMapping("/predict")
    public ResponseEntity<Map<String, Object>> predictVolume(
            @RequestParam int node,
            @RequestParam int time) {
        try {
            double volume = pythonModelService.predictVolume(node, time);
            Map<String, Object> response = new HashMap<>();
            response.put("node", node);
            response.put("time", time);
            response.put("volume", volume);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("预测流量失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("nodeCount", routePlannerService.getAllNodes().size());
        response.put("edgeCount", routePlannerService.getAllEdges().size());
        response.put("pythonModelAvailable", pythonModelService.isServiceAvailable());

        return ResponseEntity.ok(response);
    }

    /**
     * 简单的路径规划接口（GET方式）
     */
    @GetMapping("/plan")
    public ResponseEntity<RouteResponse> planRouteSimple(
            @RequestParam int startNode,
            @RequestParam int endNode,
            @RequestParam(defaultValue = "100") int timePoint,
            @RequestParam(defaultValue = "0.05") double congestionAlpha) {

        RouteRequest request = new RouteRequest(startNode, endNode, timePoint);
        request.setCongestionAlpha(congestionAlpha);

        return planRoute(request);
    }
}