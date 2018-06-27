package com.xmfenbei.dijkstra;

import com.alibaba.fastjson.JSON;
import com.xmfenbei.dijkstra.geojson.FeatureCollection;
import com.xmfenbei.dijkstra.geojson.RouteFeature;
import com.xmfenbei.dijkstra.geojson.RouteFeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DijkstraController {

    @Autowired
    private DijkstraService service;

    @RequestMapping(value = "/dijkstra", method = RequestMethod.GET)
    public String dijkstra(@RequestParam(name = "endPoint", defaultValue = "[118.0584944,24.5956423]") String endPoint1,
                            @RequestParam(name = "startPoint", defaultValue = "[118.0587042,24.5957176]") String startPoint1) {
        List<Double> endPoint = JSON.parseArray(endPoint1, Double.class);
        List<Double> startPoint = JSON.parseArray(startPoint1, Double.class);;
        // 1.获取源文件GeoJSON结构数据
        String inputFilePath = "D:/GitHub/dijkstra/target/classes/map20180619.geojson";
        FeatureCollection featureCollection = service.featureCollection(inputFilePath);

        // 2.从GeoJSON结构数据获取路径结构数据
        RouteFeatureCollection routeFeatureCollection = service.routeFeatureCollection(featureCollection);
        List<RouteFeature> features = routeFeatureCollection.getFeatures();

        // 3. 添加起点入口点路径数据
//        List<Double> startPoint = reqDTO.getStartPoint();
        // 首先判断是否在office图层
        List<List<Double>> startEntryPointList = service.entryPointList(featureCollection, startPoint);
        if (startEntryPointList == null) {
            startEntryPointList = service.pointToRoute(startPoint, routeFeatureCollection);
        }
        service.addRouteFeature(features, startPoint, startEntryPointList);

        // 4. 添加终点入口点路径数据
//        List<Double> endPoint = reqDTO.getEndPoint();
        List<List<Double>> endEntryPointList = service.entryPointList(featureCollection, endPoint);
        if (endEntryPointList == null) {
            endEntryPointList = service.pointToRoute(endPoint, routeFeatureCollection);
        }
        service.addRouteFeature(features, endPoint, endEntryPointList);

        // 5.根据路径结构数据转换成点与点之间连接的映射图
        Map<String, Map<String, Double>> routeMap = service.routeMap(routeFeatureCollection);
        System.out.println("routeMap: " + routeMap);

        // 6. 最短路径
        Map<String, Object> pathMap = service.dijkstra(routeMap, startPoint, endPoint);

        // 4.获取起点到终点的最短路径节点列表
        List<String> path = (List<String>) pathMap.get("path");
        System.out.println(path);

        RouteFeatureCollection pathFeatureCollection = service.pathFeatureCollection(path, routeFeatureCollection);

        return JSON.toJSONString(pathFeatureCollection);
    }
}
