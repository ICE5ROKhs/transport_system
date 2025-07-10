package com.example.routeplanner.model;

/**
 * 边实体类 - 表示图中的边
 */
public class Edge {
    private int from;
    private int to;
    private double distance;
    private double weight;
    private double congestion;

    public Edge() {}

    public Edge(int from, int to, double distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public Edge(int from, int to, double distance, double weight) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.weight = weight;
    }

    // Getters and Setters
    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getCongestion() {
        return congestion;
    }

    public void setCongestion(double congestion) {
        this.congestion = congestion;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                ", distance=" + distance +
                ", weight=" + weight +
                ", congestion=" + congestion +
                '}';
    }
}