package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LineString {

    private String type = "LineString";
    private List<List<Double>> coordinates;
}
