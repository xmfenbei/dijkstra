package com.xmfenbei.dijkstra.geojson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FeatureCollection {

    private String type = "FeatureCollection";
    private List<Feature> features;
}
