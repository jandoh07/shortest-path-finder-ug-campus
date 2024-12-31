package org.example;

public class Edge {
    private String fromNodeId;
    private String toNodeId;
    private double timeCost;
    private double lengthCost;

    public Edge(String fromNodeId, String toNodeId, double timeCost, double lengthCost) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.timeCost = timeCost;
        this.lengthCost = lengthCost;
    }

    public String getFromNodeId() { return fromNodeId; }
    public String getToNodeId() { return toNodeId; }
    public double getTimeCost() { return timeCost; }
    public double getLengthCost() { return lengthCost; }
}