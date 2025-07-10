package com.example.routeplanner.model;

/**
 * 路线规划请求实体类
 */
public class RouteRequest {
    private int startNode;
    private int endNode;
    private int timePoint;
    private double congestionAlpha;

    public RouteRequest() {}

    public RouteRequest(int startNode, int endNode, int timePoint) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.timePoint = timePoint;
        this.congestionAlpha = 0.05; // 默认值
    }

    // Getters and Setters
    public int getStartNode() {
        return startNode;
    }

    public void setStartNode(int startNode) {
        this.startNode = startNode;
    }

    public int getEndNode() {
        return endNode;
    }

    public void setEndNode(int endNode) {
        this.endNode = endNode;
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
        return "RouteRequest{" +
                "startNode=" + startNode +
                ", endNode=" + endNode +
                ", timePoint=" + timePoint +
                ", congestionAlpha=" + congestionAlpha +
                '}';
    }
}