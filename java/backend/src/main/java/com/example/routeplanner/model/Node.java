package com.example.routeplanner.model;

/**
 * 节点实体类 - 表示图中的节点
 */
public class Node {
    private int sensorId;
    private double latitude;
    private double longitude;
    private double predictedVolume;

    public Node() {}

    public Node(int sensorId, double latitude, double longitude) {
        this.sensorId = sensorId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getPredictedVolume() {
        return predictedVolume;
    }

    public void setPredictedVolume(double predictedVolume) {
        this.predictedVolume = predictedVolume;
    }

    @Override
    public String toString() {
        return "Node{" +
                "sensorId=" + sensorId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", predictedVolume=" + predictedVolume +
                '}';
    }
}