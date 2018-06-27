package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RouteFeatureCollection {

    private String type = "FeatureCollection";
    private List<RouteFeature> features;
}
