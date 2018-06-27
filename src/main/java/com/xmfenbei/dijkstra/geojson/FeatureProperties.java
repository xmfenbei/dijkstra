package com.xmfenbei.dijkstra.geojson;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FeatureProperties {

    @JSONField(name = "OBJECTID")
    private String objectId;
    @JSONField(name = "Layer")
    private String layer;
    @JSONField(name = "Type")
    private String type;
    @JSONField(name = "Name")
    private String name;
    @JSONField(name = "EntryPoint")
    private String entryPoints;
}
