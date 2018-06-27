package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RouteFeature {

    private String type = "Feature";
    private LineString geometry;
    private FeatureProperties properties;
}
