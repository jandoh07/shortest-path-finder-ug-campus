package org.example;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;

public class AStarAlgorithm {

    public PathResult findShortestPath(Graph graph, String startNodeId, String endNodeId) {
        PriorityQueue<NodeCost> openSet = new PriorityQueue<>(Comparator.comparingDouble(NodeCost::getEstimatedCost));
        Set<String> closedSet = new HashSet<>();
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();

        for (String nodeId : graph.getNodes().keySet()) {
            gScore.put(nodeId, Double.POSITIVE_INFINITY);
            fScore.put(nodeId, Double.POSITIVE_INFINITY);
        }
        gScore.put(startNodeId, 0.0);
        fScore.put(startNodeId, heuristicCostEstimate(graph.getNode(startNodeId), graph.getNode(endNodeId)));

        openSet.add(new NodeCost(startNodeId, fScore.get(startNodeId)));

        while (!openSet.isEmpty()) {
            NodeCost current = openSet.poll();
            String currentNodeId = current.getNodeId();

            if (currentNodeId.equals(endNodeId)) {
                return reconstructPath(cameFrom, currentNodeId, graph, gScore.get(endNodeId));
            }

            closedSet.add(currentNodeId);

            for (Edge edge : graph.getAdjacencyList().getOrDefault(currentNodeId, Collections.emptyList())) {
                String neighborNodeId = edge.getToNodeId();

                if (closedSet.contains(neighborNodeId)) {
                    continue;
                }

//                double tentativeGScore = gScore.get(currentNodeId) + edge.getTimeCost();
                double tentativeGScore = gScore.get(currentNodeId) + edge.getLengthCost();

                if (tentativeGScore < gScore.getOrDefault(neighborNodeId, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighborNodeId, currentNodeId);
                    gScore.put(neighborNodeId, tentativeGScore);
                    fScore.put(neighborNodeId, tentativeGScore + heuristicCostEstimate(graph.getNode(neighborNodeId), graph.getNode(endNodeId)));

                    if (!openSet.contains(new NodeCost(neighborNodeId, fScore.get(neighborNodeId)))) {
                        openSet.add(new NodeCost(neighborNodeId, fScore.get(neighborNodeId)));
                    }
                }
            }
        }

        return new PathResult(Collections.emptyList(), 0.0);
    }

    private double heuristicCostEstimate(Node startNode, Node endNode) {
        double dx = startNode.getLatitude() - endNode.getLatitude();
        double dy = startNode.getLongitude() - endNode.getLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private PathResult reconstructPath(Map<String, String> cameFrom, String currentNodeId, Graph graph, double totalDistance) {
        List<String> path = new ArrayList<>();
        while (cameFrom.containsKey(currentNodeId)) {
            path.add(currentNodeId);
            currentNodeId = cameFrom.get(currentNodeId);
        }
        path.add(currentNodeId);
        Collections.reverse(path);

        List<GeoPosition> geoPositions = new ArrayList<>();
        for (String nodeId : path) {
            Node node = graph.getNode(nodeId);
            geoPositions.add(new GeoPosition(node.getLatitude(), node.getLongitude()));
        }

        return new PathResult(geoPositions, totalDistance);
    }

    private static class NodeCost {
        private final String nodeId;
        private final double estimatedCost;

        public NodeCost(String nodeId, double estimatedCost) {
            this.nodeId = nodeId;
            this.estimatedCost = estimatedCost;
        }

        public String getNodeId() {
            return nodeId;
        }

        public double getEstimatedCost() {
            return estimatedCost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeCost nodeCost = (NodeCost) o;
            return Objects.equals(nodeId, nodeCost.nodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId);
        }
    }
}

