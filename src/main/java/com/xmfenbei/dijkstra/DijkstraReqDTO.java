package com.xmfenbei.dijkstra;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DijkstraReqDTO {
//    String mapPath;
    List<Double> endPoint;
    List<Double> startPoint;
}
