package com.example.routeplanner.service;

import com.example.routeplanner.model.Edge;
import com.example.routeplanner.model.Node;
import com.example.routeplanner.model.RouteRequest;
import com.example.routeplanner.model.RouteResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 路线规划服务
 */
@Service
public class RoutePlannerService {

    private static final Logger logger = LoggerFactory.getLogger(RoutePlannerService.class);

    @Autowired
    private PythonModelService pythonModelService;

    @Value("${route.planner.congestion-alpha}")
    private double congestionAlpha;

    @Value("${route.planner.max-distance-km}")
    private double maxDistanceKm;

    @Value("${route.planner.node-data-file}")
    private String nodeDataFile;

    @Value("${route.planner.edge-data-file}")
    private String edgeDataFile;

    private Map<Integer, Node> nodes = new HashMap<>();
    private List<Edge> edges = new ArrayList<>();
    private Map<Integer, List<Edge>> adjacencyList = new HashMap<>();

    @PostConstruct
    public void init() {
        loadNodeData();
        loadEdgeData();
        buildAdjacencyList();
        logger.info("路线规划服务初始化完成，节点数: {}, 边数: {}", nodes.size(), edges.size());
    }

    /**
     * 加载节点数据
     */
    private void loadNodeData() {
        try {
            ClassPathResource resource = new ClassPathResource("data/sensors北交周边.csv");
            try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
                List<String[]> records = reader.readAll();

                // 跳过标题行
                for (int i = 1; i < records.size(); i++) {
                    String[] record = records.get(i);
                    if (record.length >= 3) {
                        try {
                            int sensorId = Integer.parseInt(record[0].trim());
                            double latitude = Double.parseDouble(record[1].trim());
                            double longitude = Double.parseDouble(record[2].trim());

                            Node node = new Node(sensorId, latitude, longitude);
                            nodes.put(sensorId, node);
                        } catch (NumberFormatException e) {
                            logger.warn("解析节点数据失败: {}", Arrays.toString(record));
                        }
                    }
                }
            } catch (CsvException e) {
                logger.error("CSV解析错误: {}", e.getMessage());
            }
        } catch (IOException e) {
            logger.error("加载节点数据失败: {}", e.getMessage());
        }
    }

    /**
     * 加载边数据
     */
    private void loadEdgeData() {
        try {
            ClassPathResource resource = new ClassPathResource("data/edges.csv");
            try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
                List<String[]> records = reader.readAll();

                // 跳过标题行
                for (int i = 1; i < records.size(); i++) {
                    String[] record = records.get(i);
                    if (record.length >= 2) {
                        try {
                            int from = Integer.parseInt(record[0].trim());
                            int to = Integer.parseInt(record[1].trim());

                            Node fromNode = nodes.get(from);
                            Node toNode = nodes.get(to);

                            if (fromNode != null && toNode != null) {
                                double distance = calculateDistance(fromNode, toNode);
                                Edge edge = new Edge(from, to, distance);
                                edges.add(edge);

                                // 创建反向边（无向图）
                                Edge reverseEdge = new Edge(to, from, distance);
                                edges.add(reverseEdge);
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("解析边数据失败: {}", Arrays.toString(record));
                        }
                    }
                }
            } catch (CsvException e) {
                logger.error("CSV解析错误: {}", e.getMessage());
            }
        } catch (IOException e) {
            logger.error("加载边数据失败: {}", e.getMessage());
        }
    }

    /**
     * 构建邻接表
     */
    private void buildAdjacencyList() {
        for (Edge edge : edges) {
            adjacencyList.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge);
        }
    }

    /**
     * 计算两点间距离（使用 Haversine 公式）
     */
    private double calculateDistance(Node node1, Node node2) {
        double lat1 = Math.toRadians(node1.getLatitude());
        double lon1 = Math.toRadians(node1.getLongitude());
        double lat2 = Math.toRadians(node2.getLatitude());
        double lon2 = Math.toRadians(node2.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double r = 6371; // 地球半径（公里）

        return c * r;
    }

    /**
     * 规划最优路径
     */
    public RouteResponse planRoute(RouteRequest request) {
        if (!nodes.containsKey(request.getStartNode()) || !nodes.containsKey(request.getEndNode())) {
            return new RouteResponse(false, "起点或终点不存在");
        }

        // 为所有边计算拥堵系数
        calculateCongestionForEdges(request.getTimePoint(), request.getCongestionAlpha());

        // 使用 Dijkstra 算法计算最短路径
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(
                Map.Entry.comparingByValue());

        // 初始化距离
        for (int nodeId : nodes.keySet()) {
            distances.put(nodeId, Double.MAX_VALUE);
        }
        distances.put(request.getStartNode(), 0.0);
        pq.offer(new AbstractMap.SimpleEntry<>(request.getStartNode(), 0.0));

        while (!pq.isEmpty()) {
            Map.Entry<Integer, Double> current = pq.poll();
            int currentNode = current.getKey();
            double currentDist = current.getValue();

            if (currentDist > distances.get(currentNode)) {
                continue;
            }

            List<Edge> neighbors = adjacencyList.get(currentNode);
            if (neighbors != null) {
                for (Edge edge : neighbors) {
                    int neighbor = edge.getTo();
                    double newDist = currentDist + edge.getWeight();

                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previous.put(neighbor, currentNode);
                        pq.offer(new AbstractMap.SimpleEntry<>(neighbor, newDist));
                    }
                }
            }
        }

        // 重构路径
        List<Integer> path = new ArrayList<>();
        Integer current = request.getEndNode();

        if (!distances.containsKey(current) || distances.get(current) == Double.MAX_VALUE) {
            return new RouteResponse(false, "无法找到从起点到终点的路径");
        }

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);

        // 构建响应
        RouteResponse response = new RouteResponse(true, "路径规划成功");
        response.setPath(path);
        response.setTotalCongestion(distances.get(request.getEndNode()));
        response.setTotalDistance(calculateTotalDistance(path));
        response.setPathEdges(getPathEdges(path));

        return response;
    }

    /**
     * 为所有边计算拥堵系数
     */
    /**
     * 为所有边计算拥堵系数（优化版本 - 避免重复预测）
     */

    private void calculateCongestionForEdges(int timePoint, double alpha) {
        Map<Integer, Double> nodeVolumeCache = new HashMap<>();

        for (Edge edge : edges) {
            double flowFrom = nodeVolumeCache.computeIfAbsent(edge.getFrom(),
                    nodeId -> pythonModelService.predictVolume(nodeId, timePoint));
            double flowTo = nodeVolumeCache.computeIfAbsent(edge.getTo(),
                    nodeId -> pythonModelService.predictVolume(nodeId, timePoint));

            double avgFlow = (flowFrom + flowTo) / 2.0;
            double congestion = edge.getDistance() * (1 + alpha * avgFlow);
            edge.setWeight(congestion);
            edge.setCongestion(congestion);
        }
    }

    /**
     * 计算路径总距离
     */
    private double calculateTotalDistance(List<Integer> path) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node fromNode = nodes.get(path.get(i));
            Node toNode = nodes.get(path.get(i + 1));
            totalDistance += calculateDistance(fromNode, toNode);
        }
        return totalDistance;
    }

    /**
     * 获取路径中的边
     */
    private List<Edge> getPathEdges(List<Integer> path) {
        List<Edge> pathEdges = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);

            // 找到对应的边
            for (Edge edge : edges) {
                if (edge.getFrom() == from && edge.getTo() == to) {
                    pathEdges.add(edge);
                    break;
                }
            }
        }
        return pathEdges;
    }

    /**
     * 获取所有节点
     */
    public Map<Integer, Node> getAllNodes() {
        return nodes;
    }

    /**
     * 获取所有边
     */
    public List<Edge> getAllEdges() {
        return edges;
    }
}