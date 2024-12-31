package org.example;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;

public class DjikstrasAlgorithm {
    private Map<String, String> previousNodes;
        Map<String, Double> distances = new HashMap<>();

    public Map<String, Double> findShortestPaths(Graph graph, String source) {
        // Priority queue to store nodes and their current shortest distance from the source
        PriorityQueue<NodeDistance> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));
        // Map to store the shortest distance from the source to each node
        previousNodes = new HashMap<>();
        // Initialize distances to infinity
        for (String nodeId : graph.getNodes().keySet()) {
            distances.put(nodeId, Double.POSITIVE_INFINITY);
        }
        // Set the distance to the source node as 0
        distances.put(source, 0.0);
        // Add the source node to the priority queue
        priorityQueue.add(new NodeDistance(source, 0.0));

        while (!priorityQueue.isEmpty()) {
            // Extract the node with the smallest distance
            NodeDistance current = priorityQueue.poll();
            String currentNodeId = current.getNodeId();
            double currentDistance = current.getDistance();

            // Skip if the current distance is greater than the known shortest distance
            if (currentDistance > distances.get(currentNodeId)) {
                continue;
            }

            // For each neighbor of the current node
            for (Edge edge : graph.getAdjacencyList().getOrDefault(currentNodeId, Collections.emptyList())) {
                String neighborNodeId = edge.getToNodeId();
//                double newDistance = currentDistance + edge.getTimeCost();

                double newDistance = currentDistance + edge.getLengthCost();
                // If the new distance is smaller than the current known distance
                if (newDistance < distances.get(neighborNodeId)) {
                    // Update the distance
                    distances.put(neighborNodeId, newDistance);
                    previousNodes.put(neighborNodeId, currentNodeId);
                    // Add the neighbor to the priority queue
                    priorityQueue.add(new NodeDistance(neighborNodeId, newDistance));
                }
            }
        }

        return distances;
    }



    /**
     * Helper class to store a node and its distance for the priority queue.
     */
    private static class NodeDistance {
        private final String nodeId;
        private final double distance;

        public NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        public String getNodeId() {
            return nodeId;
        }

        public double getDistance() {
            return distance;
        }
    }

    // Helper method to print the shortest path from the start node to a given node
    public ArrayList<GeoPosition> printPath(String startNodeId, String endNodeId, Graph graph) {
        Map<String, Node> nodes = graph.getNodes();

        if (!distances.containsKey(endNodeId) || distances.get(endNodeId) == Double.MAX_VALUE) {
            System.out.println("No path from " + startNodeId + " to " + endNodeId);
            return null;
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String current = endNodeId;
        while (current != null) {
            path.add(current);
            current = previousNodes.get(current);
        }
        Collections.reverse(path);



        ArrayList<GeoPosition> geoPositions = new ArrayList<>();
        for (String nodeId : path) {
            Node node = nodes.get(nodeId);
            geoPositions.add(new GeoPosition(node.getLatitude(), node.getLongitude()));
        }



        return geoPositions;
    }

}
