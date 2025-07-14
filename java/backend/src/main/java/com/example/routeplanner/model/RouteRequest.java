package com.example.routeplanner.model;

/**
 * 路线规划请求实体类
 */
public class RouteRequest {
    // 原有的节点ID方式（保持向后兼容）
    private Integer startNode;
    private Integer endNode;

    // 新增的经纬度方式
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;

    private int timePoint;
    private double congestionAlpha;

    public RouteRequest() {}

    // 原有构造函数（节点ID方式）
    public RouteRequest(int startNode, int endNode, int timePoint) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.timePoint = timePoint;
        this.congestionAlpha = 0.05; // 默认值
    }

    // 新增构造函数（经纬度方式）
    public RouteRequest(double startLatitude, double startLongitude,
                        double endLatitude, double endLongitude, int timePoint) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.timePoint = timePoint;
        this.congestionAlpha = 0.05; // 默认值
    }

    /**
     * 判断是否使用经纬度方式
     */
    public boolean useCoordinates() {
        return startLatitude != null && startLongitude != null &&
                endLatitude != null && endLongitude != null;
    }

    /**
     * 判断是否使用节点ID方式
     */
    public boolean useNodeIds() {
        return startNode != null && endNode != null;
    }

    // Getters and Setters
    public Integer getStartNode() {
        return startNode;
    }

    public void setStartNode(Integer startNode) {
        this.startNode = startNode;
    }

    public Integer getEndNode() {
        return endNode;
    }

    public void setEndNode(Integer endNode) {
        this.endNode = endNode;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
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
        if (useCoordinates()) {
            return "RouteRequest{" +
                    "startCoord=(" + startLatitude + "," + startLongitude + ")" +
                    ", endCoord=(" + endLatitude + "," + endLongitude + ")" +
                    ", timePoint=" + timePoint +
                    ", congestionAlpha=" + congestionAlpha +
                    '}';
        } else {
            return "RouteRequest{" +
                    "startNode=" + startNode +
                    ", endNode=" + endNode +
                    ", timePoint=" + timePoint +
                    ", congestionAlpha=" + congestionAlpha +
                    '}';
        }
    }
}