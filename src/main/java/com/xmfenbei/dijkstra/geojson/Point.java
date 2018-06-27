package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Point {
    double x;
    double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(List<Double> coordinate) {
        this.x = coordinate.get(0);
        this.y = coordinate.get(1);
    }
}
