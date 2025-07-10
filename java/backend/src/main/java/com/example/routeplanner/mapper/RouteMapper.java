package com.example.routeplanner.mapper;

import com.example.routeplanner.model.Edge;
import com.example.routeplanner.model.Node;
import com.example.routeplanner.model.RouteResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路由映射器 - 用于数据转换和映射
 */
@Component
public class RouteMapper {

    /**
     * 将路径转换为详细的路径信息
     */
    public RouteResponse enhanceRouteResponse(RouteResponse response, Map<Integer, Node> nodeMap) {
        if (response.getPath() != null && !response.getPath().isEmpty()) {
            // 添加路径详细信息
            List<String> pathDetails = response.getPath().stream()
                    .map(nodeId -> {
                        Node node = nodeMap.get(nodeId);
                        if (node != null) {
                            return String.format("节点%d(%.4f,%.4f)",
                                    nodeId, node.getLatitude(), node.getLongitude());
                        }
                        return "节点" + nodeId;
                    })
                    .collect(Collectors.toList());

            // 更新消息
            String pathStr = String.join(" → ", pathDetails);
            response.setMessage(response.getMessage() + "\n路径详情: " + pathStr);
        }
        return response;
    }

    /**
     * 将节点列表转换为坐标数组
     */
    public double[][] pathToCoordinates(List<Integer> path, Map<Integer, Node> nodeMap) {
        return path.stream()
                .map(nodeId -> {
                    Node node = nodeMap.get(nodeId);
                    if (node != null) {
                        return new double[]{node.getLatitude(), node.getLongitude()};
                    }
                    return new double[]{0.0, 0.0};
                })
                .toArray(double[][]::new);
    }

    /**
     * 计算路径统计信息
     */
    public Map<String, Object> calculatePathStatistics(RouteResponse response) {
        return Map.of(
                "nodeCount", response.getPath() != null ? response.getPath().size() : 0,
                "edgeCount", response.getPathEdges() != null ? response.getPathEdges().size() : 0,
                "totalDistance", response.getTotalDistance(),
                "totalCongestion", response.getTotalCongestion(),
                "avgCongestionPerKm", response.getTotalDistance() > 0 ?
                        response.getTotalCongestion() / response.getTotalDistance() : 0
        );
    }

    /**
     * 格式化路径为字符串
     */
    public String formatPath(List<Integer> path) {
        if (path == null || path.isEmpty()) {
            return "无路径";
        }
        return path.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" → "));
    }
}