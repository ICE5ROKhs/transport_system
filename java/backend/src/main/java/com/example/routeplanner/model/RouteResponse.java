package com.example.routeplanner.model;

import java.util.List;

/**
 * 路线规划响应实体类
 */
public class RouteResponse {
    private boolean success;
    private String message;

    // 最优路径（考虑拥堵）
    private PathResult optimalPath;

    // 最短距离路径
    private PathResult shortestPath;

    public RouteResponse() {}

    public RouteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PathResult getOptimalPath() {
        return optimalPath;
    }

    public void setOptimalPath(PathResult optimalPath) {
        this.optimalPath = optimalPath;
    }

    public PathResult getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(PathResult shortestPath) {
        this.shortestPath = shortestPath;
    }

    // 为了向后兼容，保留原有的方法
    public List<Integer> getPath() {
        return optimalPath != null ? optimalPath.getPath() : null;
    }

    public void setPath(List<Integer> path) {
        if (optimalPath == null) {
            optimalPath = new PathResult();
        }
        optimalPath.setPath(path);
    }

    public double getTotalCongestion() {
        return optimalPath != null ? optimalPath.getTotalCongestion() : 0.0;
    }

    public void setTotalCongestion(double totalCongestion) {
        if (optimalPath == null) {
            optimalPath = new PathResult();
        }
        optimalPath.setTotalCongestion(totalCongestion);
    }

    public double getTotalDistance() {
        return optimalPath != null ? optimalPath.getTotalDistance() : 0.0;
    }

    public void setTotalDistance(double totalDistance) {
        if (optimalPath == null) {
            optimalPath = new PathResult();
        }
        optimalPath.setTotalDistance(totalDistance);
    }

    public List<Edge> getPathEdges() {
        return optimalPath != null ? optimalPath.getPathEdges() : null;
    }

    public void setPathEdges(List<Edge> pathEdges) {
        if (optimalPath == null) {
            optimalPath = new PathResult();
        }
        optimalPath.setPathEdges(pathEdges);
    }

    @Override
    public String toString() {
        return "RouteResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", optimalPath=" + optimalPath +
                ", shortestPath=" + shortestPath +
                '}';
    }

    /**
     * 路径结果内部类
     */
    public static class PathResult {
        private List<Integer> path;
        private double totalCongestion;
        private double totalDistance;
        private List<Edge> pathEdges;
        private String pathType;
        private double travelTime; // 预计行程时间

        public PathResult() {}

        public PathResult(String pathType) {
            this.pathType = pathType;
        }

        // Getters and Setters
        public List<Integer> getPath() {
            return path;
        }

        public void setPath(List<Integer> path) {
            this.path = path;
        }

        public double getTotalCongestion() {
            return totalCongestion;
        }

        public void setTotalCongestion(double totalCongestion) {
            this.totalCongestion = totalCongestion;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public void setTotalDistance(double totalDistance) {
            this.totalDistance = totalDistance;
        }

        public List<Edge> getPathEdges() {
            return pathEdges;
        }

        public void setPathEdges(List<Edge> pathEdges) {
            this.pathEdges = pathEdges;
        }


        public double getTravelTime() {
            return travelTime;
        }

        public void setTravelTime(double travelTime) {
            this.travelTime = travelTime;
        }

        @Override
        public String toString() {
            return "PathResult{" +
                    "pathType='" + pathType + '\'' +
                    ", path=" + path +
                    ", totalDistance=" + totalDistance +
                    ", totalCongestion=" + totalCongestion +
                    ", travelTime=" + travelTime +
                    ", pathEdges=" + (pathEdges != null ? pathEdges.size() : 0) + " edges" +
                    '}';
        }
    }
}