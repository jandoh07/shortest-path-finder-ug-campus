package org.example;


import java.util.*;

public class Graph {
    private Map<String, Node> nodes;
    private Map<String, List<Edge>> adjacencyList;

    public Graph(Map<String, Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.adjacencyList = new HashMap<>();
        for (Edge edge : edges) {
            adjacencyList.computeIfAbsent(edge.getFromNodeId(), k -> new ArrayList<>()).add(edge);
        }
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }


    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }
}
