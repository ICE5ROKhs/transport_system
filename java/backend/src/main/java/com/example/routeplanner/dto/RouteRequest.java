//路径规划请求参数封装
package com.example.routeplanner.dto;

public class RouteRequest {
    private int start;
    private int end;
    private int time;

    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }
    public int getEnd() { return end; }
    public void setEnd(int end) { this.end = end; }
    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }
}