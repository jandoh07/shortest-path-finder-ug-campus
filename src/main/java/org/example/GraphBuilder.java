package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {
    private Map<String, Node> nodes = new HashMap<>();
    private List<Edge> edges = new ArrayList<>();
    private Map<String, String> locationMap = new HashMap<>();

    public void loadNodes(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip the header row
                    continue;
                }
                String[] values = line.split(",");
                String nodeId = values[0];
                double lat = Double.parseDouble(values[1]);
                double lon = Double.parseDouble(values[2]);
                String location = values[3];
                if (location != null && !location.isEmpty()) {
                    locationMap.put(location.toLowerCase(), nodeId);
                }
                nodes.put(nodeId, new Node(nodeId, lat, lon));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public void loadEdges(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip the header row
                    continue;
                }

                String[] values = line.split("[,\t]+");

                if (values.length < 3) {
                    System.err.println("Invalid line (less than 3 columns): " + line);
                    continue; // Skip invalid lines
                }

                String fromNodeId = values[0];
                String toNodeId = values[1];

                // Check if both nodes exist in the map
                Node fromNode = nodes.get(fromNodeId);
                Node toNode = nodes.get(toNodeId);


                double timeCost = Double.parseDouble(values[2]);
                double lengthCost = calculateDistance(
                        fromNode.getLatitude(),
                        fromNode.getLongitude(),
                        toNode.getLatitude(),
                        toNode.getLongitude()
                );

                edges.add(new Edge(fromNodeId, toNodeId, timeCost, lengthCost));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the Earth in meters

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in meters
    }

    public Map<String, String> getLocationMap() {
        return locationMap;
    }

    // Method to search locations based on user input - kind of a linear search algorithm
    public List<String> searchLocations(String userInput) {
        List<String> matchedKeys = new ArrayList<>();
        String searchQuery = userInput.toLowerCase();

        for (String key : locationMap.keySet()) {
            if (key.contains(searchQuery)) {
                matchedKeys.add(key);
            }
        }

        return matchedKeys;
    }

    public String getLocationNodeId(String location) {
        return locationMap.get(location);
    }
}
