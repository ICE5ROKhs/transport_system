//路径规划响应结果封装
package com.example.routeplanner.dto;

import java.util.List;

public class RouteResponse {
    private List<Integer> path;
    private double totalWeight;

    public RouteResponse(List<Integer> path, double totalWeight) {
        this.path = path;
        this.totalWeight = totalWeight;
    }

    public List<Integer> getPath() { return path; }
    public double getTotalWeight() { return totalWeight; }
}
