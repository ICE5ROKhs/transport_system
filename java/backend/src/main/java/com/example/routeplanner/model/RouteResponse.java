package com.example.routeplanner.model;

import java.util.List;

/**
 * 路线规划响应实体类
 */
public class RouteResponse {
    private boolean success;
    private String message;
    private List<Integer> path;
    private double totalCongestion;
    private double totalDistance;
    private List<Edge> pathEdges;

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

    @Override
    public String toString() {
        return "RouteResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", path=" + path +
                ", totalCongestion=" + totalCongestion +
                ", totalDistance=" + totalDistance +
                ", pathEdges=" + pathEdges +
                '}';
    }
}