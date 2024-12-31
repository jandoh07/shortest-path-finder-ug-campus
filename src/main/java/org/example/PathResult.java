package org.example;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class PathResult {
    private final List<GeoPosition> path;
    private final double distance;

    public PathResult(List<GeoPosition> path, double distance) {
        this.path = path;
        this.distance = distance;
    }

    public List<GeoPosition> getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }
}
