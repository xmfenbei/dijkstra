package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Polygon {
    private String type = "Polygon";
    private List<List<List<Double>>> coordinates;
}
