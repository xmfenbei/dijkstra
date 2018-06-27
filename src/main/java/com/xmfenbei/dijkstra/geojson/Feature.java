package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Feature {

    private String type = "Feature";
    private String geometry;
    private FeatureProperties properties;
}
