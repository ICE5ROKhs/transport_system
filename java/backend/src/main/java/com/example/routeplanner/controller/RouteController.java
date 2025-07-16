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

import java.util.*;

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
     * 根据经纬度规划最优路径（返回两种路径）
     */
    @PostMapping("/plan/coordinates")
    public ResponseEntity<DualRouteResponse> planRouteByCoordinates(@RequestBody CoordinateRouteRequest request) {
        logger.info("收到基于经纬度的路线规划请求: {}", request);

        try {
            // 找到起点和终点最近的节点
            int startNodeId = findNearestNode(request.getStartLatitude(), request.getStartLongitude());
            int endNodeId = findNearestNode(request.getEndLatitude(), request.getEndLongitude());

            if (startNodeId == -1) {
                return ResponseEntity.badRequest().body(
                        new DualRouteResponse(false, "找不到起点附近的节点", null, null));
            }

            if (endNodeId == -1) {
                return ResponseEntity.badRequest().body(
                        new DualRouteResponse(false, "找不到终点附近的节点", null, null));
            }

            // 1. 计算最优路径（考虑拥堵）
            RouteRequest optimalRequest = new RouteRequest(startNodeId, endNodeId, request.getTimePoint());
            optimalRequest.setCongestionAlpha(request.getCongestionAlpha());
            RouteResponse optimalRoute = routePlannerService.planRoute(optimalRequest);

            // 2. 计算绝对距离最短路径（不考虑拥堵）
            RouteRequest shortestRequest = new RouteRequest(startNodeId, endNodeId, request.getTimePoint());
            shortestRequest.setCongestionAlpha(0.0); // 设置为0，不考虑拥堵
            RouteResponse shortestRoute = routePlannerService.planRoute(shortestRequest);

            // 添加节点信息到响应中
            Map<Integer, Node> allNodes = routePlannerService.getAllNodes();
            Node startNode = allNodes.get(startNodeId);
            Node endNode = allNodes.get(endNodeId);

            String nodeInfo = String.format(
                    "起点: (%.6f, %.6f) -> 最近节点%d (%.6f, %.6f) [距离: %.2f km]\n终点: (%.6f, %.6f) -> 最近节点%d (%.6f, %.6f) [距离: %.2f km]",
                    request.getStartLatitude(), request.getStartLongitude(), startNodeId,
                    startNode.getLatitude(), startNode.getLongitude(),
                    calculateDistance(request.getStartLatitude(), request.getStartLongitude(),
                            startNode.getLatitude(), startNode.getLongitude()),
                    request.getEndLatitude(), request.getEndLongitude(), endNodeId,
                    endNode.getLatitude(), endNode.getLongitude(),
                    calculateDistance(request.getEndLatitude(), request.getEndLongitude(),
                            endNode.getLatitude(), endNode.getLongitude())
            );

            // 增强路径消息
            if (optimalRoute.isSuccess()) {
                optimalRoute.setMessage("最优路径规划成功（考虑拥堵情况）\n" + nodeInfo);
            }
            if (shortestRoute.isSuccess()) {
                shortestRoute.setMessage("绝对距离最短路径规划成功\n" + nodeInfo);
            }

            // 创建双路径响应
            DualRouteResponse dualResponse = new DualRouteResponse(
                    optimalRoute.isSuccess() && shortestRoute.isSuccess(),
                    "路径规划完成",
                    optimalRoute,
                    shortestRoute
            );

            // 添加路径比较信息
            if (optimalRoute.isSuccess() && shortestRoute.isSuccess()) {
                dualResponse.setComparison(compareRoutes(optimalRoute, shortestRoute));
            }

            logger.info("基于经纬度的双路径规划完成: {}", dualResponse);
            return ResponseEntity.ok(dualResponse);

        } catch (Exception e) {
            logger.error("基于经纬度的路线规划失败: {}", e.getMessage());
            DualRouteResponse errorResponse = new DualRouteResponse(false, "路线规划失败: " + e.getMessage(), null, null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 根据经纬度规划最优路径 (GET方式)
     */
    @GetMapping("/plan/coordinates")
    public ResponseEntity<DualRouteResponse> planRouteByCoordinatesGet(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng,
            @RequestParam(defaultValue = "100") int timePoint,
            @RequestParam(defaultValue = "0.05") double congestionAlpha) {

        CoordinateRouteRequest request = new CoordinateRouteRequest();
        request.setStartLatitude(startLat);
        request.setStartLongitude(startLng);
        request.setEndLatitude(endLat);
        request.setEndLongitude(endLng);
        request.setTimePoint(timePoint);
        request.setCongestionAlpha(congestionAlpha);

        return planRouteByCoordinates(request);
    }

    /**
     * 找到距离指定经纬度最近的节点
     */
    private int findNearestNode(double latitude, double longitude) {
        Map<Integer, Node> allNodes = routePlannerService.getAllNodes();
        double minDistance = Double.MAX_VALUE;
        int nearestNodeId = -1;

        for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
            Node node = entry.getValue();
            double distance = calculateDistance(latitude, longitude, node.getLatitude(), node.getLongitude());

            if (distance < minDistance) {
                minDistance = distance;
                nearestNodeId = entry.getKey();
            }
        }

        logger.debug("找到距离 ({}, {}) 最近的节点: {}, 距离: {} km",
                latitude, longitude, nearestNodeId, minDistance);

        return nearestNodeId;
    }

    /**
     * 计算两点间距离（使用 Haversine 公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    /**
     * 获取指定经纬度附近的节点（用于调试）
     */
    @GetMapping("/nearest")
    public ResponseEntity<Map<String, Object>> getNearestNode(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") int count) {

        try {
            Map<Integer, Node> allNodes = routePlannerService.getAllNodes();
            List<Map<String, Object>> nearestNodes = new ArrayList<>();

            // 计算所有节点的距离并排序
            List<Map.Entry<Integer, Double>> nodeDistances = new ArrayList<>();
            for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
                Node node = entry.getValue();
                double distance = calculateDistance(latitude, longitude, node.getLatitude(), node.getLongitude());
                nodeDistances.add(new AbstractMap.SimpleEntry<>(entry.getKey(), distance));
            }

            // 按距离排序
            nodeDistances.sort(Map.Entry.comparingByValue());

            // 取前count个节点
            for (int i = 0; i < Math.min(count, nodeDistances.size()); i++) {
                Map.Entry<Integer, Double> entry = nodeDistances.get(i);
                int nodeId = entry.getKey();
                double distance = entry.getValue();
                Node node = allNodes.get(nodeId);

                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("nodeId", nodeId);
                nodeInfo.put("latitude", node.getLatitude());
                nodeInfo.put("longitude", node.getLongitude());
                nodeInfo.put("distance", distance);
                nearestNodes.add(nodeInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("queryPoint", Map.of("latitude", latitude, "longitude", longitude));
            response.put("nearestNodes", nearestNodes);
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取最近节点失败: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 比较两个路径的差异
     */
    private RouteComparison compareRoutes(RouteResponse optimalRoute, RouteResponse shortestRoute) {
        RouteComparison comparison = new RouteComparison();

        // 距离比较
        comparison.setDistanceDifference(optimalRoute.getTotalDistance() - shortestRoute.getTotalDistance());
        comparison.setDistanceRatio(optimalRoute.getTotalDistance() / shortestRoute.getTotalDistance());

        // 拥堵比较
        comparison.setCongestionDifference(optimalRoute.getTotalCongestion() - shortestRoute.getTotalCongestion());
        comparison.setCongestionRatio(optimalRoute.getTotalCongestion() / shortestRoute.getTotalCongestion());

        // 路径长度比较
        comparison.setPathLengthDifference(optimalRoute.getPath().size() - shortestRoute.getPath().size());

        // 判断是否为同一路径
        comparison.setSamePath(optimalRoute.getPath().equals(shortestRoute.getPath()));

        // 推荐路径
        if (comparison.isSamePath()) {
            comparison.setRecommendation("两种算法得到相同路径，这就是最佳选择");
        } else if (comparison.getDistanceDifference() < 0.5 && comparison.getCongestionRatio() < 0.9) {
            comparison.setRecommendation("推荐使用最优路径，能够有效避开拥堵且距离差异不大");
        } else if (comparison.getDistanceDifference() > 2.0) {
            comparison.setRecommendation("推荐使用最短距离路径，最优路径绕行距离过长");
        } else {
            comparison.setRecommendation("建议根据实时交通情况选择合适的路径");
        }

        return comparison;
    }



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

    // ==================== 内部类 ====================

    /**
     * 基于经纬度的路径规划请求类
     */
    public static class CoordinateRouteRequest {
        private double startLatitude;
        private double startLongitude;
        private double endLatitude;
        private double endLongitude;
        private int timePoint = 100;
        private double congestionAlpha = 0.05;

        // Getters and Setters
        public double getStartLatitude() {
            return startLatitude;
        }

        public void setStartLatitude(double startLatitude) {
            this.startLatitude = startLatitude;
        }

        public double getStartLongitude() {
            return startLongitude;
        }

        public void setStartLongitude(double startLongitude) {
            this.startLongitude = startLongitude;
        }

        public double getEndLatitude() {
            return endLatitude;
        }

        public void setEndLatitude(double endLatitude) {
            this.endLatitude = endLatitude;
        }

        public double getEndLongitude() {
            return endLongitude;
        }

        public void setEndLongitude(double endLongitude) {
            this.endLongitude = endLongitude;
        }

        public int getTimePoint() {
            return timePoint;
        }

        public void setTimePoint(int timePoint) {
            this.timePoint = timePoint;
        }

        public double getCongestionAlpha() {
            return congestionAlpha;
        }

        public void setCongestionAlpha(double congestionAlpha) {
            this.congestionAlpha = congestionAlpha;
        }

        @Override
        public String toString() {
            return "CoordinateRouteRequest{" +
                    "startLatitude=" + startLatitude +
                    ", startLongitude=" + startLongitude +
                    ", endLatitude=" + endLatitude +
                    ", endLongitude=" + endLongitude +
                    ", timePoint=" + timePoint +
                    ", congestionAlpha=" + congestionAlpha +
                    '}';
        }
    }

    /**
     * 双路径响应类
     */
    public static class DualRouteResponse {
        private boolean success;
        private String message;
        private RouteResponse optimalRoute;
        private RouteResponse shortestRoute;
        private RouteComparison comparison;

        public DualRouteResponse() {}

        public DualRouteResponse(boolean success, String message, RouteResponse optimalRoute, RouteResponse shortestRoute) {
            this.success = success;
            this.message = message;
            this.optimalRoute = optimalRoute;
            this.shortestRoute = shortestRoute;
        }

        // Getters and Setters


        public void setComparison(RouteComparison comparison) {
            this.comparison = comparison;
        }

        @Override
        public String toString() {
            return "DualRouteResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", optimalRoute=" + optimalRoute +
                    ", shortestRoute=" + shortestRoute +
                    ", comparison=" + comparison +
                    '}';
        }
    }

    /**
     * 路径比较类
     */
    public static class RouteComparison {
        private double distanceDifference;
        private double distanceRatio;
        private double congestionDifference;
        private double congestionRatio;
        private int pathLengthDifference;
        private boolean samePath;
        private String recommendation;

        // Getters and Setters
        public double getDistanceDifference() {
            return distanceDifference;
        }

        public void setDistanceDifference(double distanceDifference) {
            this.distanceDifference = distanceDifference;
        }

        public void setDistanceRatio(double distanceRatio) {
            this.distanceRatio = distanceRatio;
        }


        public void setCongestionDifference(double congestionDifference) {
            this.congestionDifference = congestionDifference;
        }

        public double getCongestionRatio() {
            return congestionRatio;
        }

        public void setCongestionRatio(double congestionRatio) {
            this.congestionRatio = congestionRatio;
        }


        public void setPathLengthDifference(int pathLengthDifference) {
            this.pathLengthDifference = pathLengthDifference;
        }

        public boolean isSamePath() {
            return samePath;
        }

        public void setSamePath(boolean samePath) {
            this.samePath = samePath;
        }


        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        @Override
        public String toString() {
            return "RouteComparison{" +
                    "distanceDifference=" + distanceDifference +
                    ", distanceRatio=" + distanceRatio +
                    ", congestionDifference=" + congestionDifference +
                    ", congestionRatio=" + congestionRatio +
                    ", pathLengthDifference=" + pathLengthDifference +
                    ", samePath=" + samePath +
                    ", recommendation='" + recommendation + '\'' +
                    '}';
        }
    }
}