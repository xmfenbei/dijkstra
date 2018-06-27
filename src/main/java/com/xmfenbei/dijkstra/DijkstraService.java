package com.xmfenbei.dijkstra;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.xmfenbei.dijkstra.geojson.Feature;
import com.xmfenbei.dijkstra.geojson.FeatureCollection;
import com.xmfenbei.dijkstra.geojson.LineString;
import com.xmfenbei.dijkstra.geojson.Point;
import com.xmfenbei.dijkstra.geojson.Polygon;
import com.xmfenbei.dijkstra.geojson.RouteFeature;
import com.xmfenbei.dijkstra.geojson.RouteFeatureCollection;
import com.xmfenbei.dijkstra.geometry.PointToLine;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DijkstraService {

    /**
     * 获取GeoJSON结构数据
     * @param filePath 文件路径
     * @return GeoJSON结构数据
     */
    public FeatureCollection featureCollection(String filePath) {
        try {
            InputStream inputStream = new FileInputStream(filePath);
            String text = IOUtils.toString(inputStream,"utf8");
            FeatureCollection featureCollection = JSON.parseObject(text, FeatureCollection.class);

            return featureCollection;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取路径层数据
     * @param featureCollection
     * @return
     */
    public RouteFeatureCollection routeFeatureCollection(FeatureCollection featureCollection) {
        RouteFeatureCollection routeFeatureCollection = new RouteFeatureCollection();
        List<RouteFeature> routeFeatureList = new ArrayList<RouteFeature>();
        routeFeatureCollection.setFeatures(routeFeatureList);

        for (Feature feature : featureCollection.getFeatures()) {
            if (feature.getProperties().getLayer().equals("Route")) {
                String geometry = feature.getGeometry();
                LineString lineString = JSON.parseObject(geometry, LineString.class);
                RouteFeature routeFeature = new RouteFeature();
                routeFeature.setGeometry(lineString);
                routeFeature.setProperties(feature.getProperties());

                routeFeatureList.add(routeFeature);
            }
        }

        return routeFeatureCollection;
    }

    /**
     * 获取出口点列表
     * @param featureCollection GeoJSON结构数据
     * @param point 点
     * @return 出口点列表
     */
    public List<List<Double>> entryPointList(FeatureCollection featureCollection, List<Double> point) {
        for (Feature feature : featureCollection.getFeatures()) {
            if (!(feature.getProperties().getType().equals("office") ||
                    feature.getProperties().getType().equals("conference"))) {
                continue;
            }

            Polygon polygon = JSON.parseObject(feature.getGeometry(), Polygon.class);
            List<Point> polygonPointList = new ArrayList<Point>();
            for (int j=0; j < polygon.getCoordinates().size(); j++) {
                List<List<Double>> pointList = polygon.getCoordinates().get(j);
                for (int i=0; i < pointList.size(); i++) {
                    Point polygonPoint = new Point(pointList.get(i));
                    polygonPointList.add(polygonPoint);
                }
            }

            Point point0 = new Point(point);
            if (isPolygonContainsPoint(polygonPointList, point0)) {
                System.out.println("当前区域");

                List<List<Double>> pointList = new ArrayList<List<Double>>();

                JSONArray entryPoints = JSON.parseArray(feature.getProperties().getEntryPoints());
                for (int i = 0; i < entryPoints.size(); i++) {
                    JSONArray jsonObject = entryPoints.getJSONArray(i);

                    List<Double> entryPoint = new ArrayList<Double>(2);
                    pointList.add(entryPoint);
                    entryPoint.add(jsonObject.getDouble(0));
                    entryPoint.add(jsonObject.getDouble(1));
                }

                return pointList;
            }
        }

        return null;
    }


    /**
     * 返回一个点是否在一个多边形区域内
     * @param mPoints 多边形坐标点列表
     * @param point   待判断点
     * @return true 多边形包含这个点, false 多边形未包含这个点。
     */
    public Boolean isPolygonContainsPoint(List<Point> mPoints, Point point) {
        int nCross = 0;

        for (int i = 0; i < mPoints.size(); i++) {
            Point p1 = mPoints.get(i);
            Point p2 = mPoints.get((i+1) % mPoints.size());
            // 取多边形任意一个边,做点point的水平延长线,求解与当前边的交点个数
            // p1p2是水平线段,要么没有交点,要么有无限个交点
            if (p1.getY() == p2.getY()) {
                continue;
            }
            // point 在p1p2 底部 --> 无交点
            if (point.getY() < Math.min(p1.getY(), p2.getY())) {
                continue;
            }
            // point 在p1p2 顶部 --> 无交点
            if (point.getY() >= Math.max(p1.getY(), p2.getY())) {
                continue;
            }
            // 求解 point点水平线与当前p1p2边的交点的 X 坐标
            double x = (point.getY() - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY()) + p1.getX();
            if (x > point.getX()) {
                // 当x=point.x时,说明point在p1p2线段上
                nCross++; // 只统计单边交点
            }
        }

        // 单边交点为偶数，点在多边形之外
        return (nCross % 2 == 1);
    }

    /**
     * 添加点到点列表的路径结构
     * @param features
     * @param point1
     * @param pointList
     */
    public void addRouteFeature(List<RouteFeature> features, List<Double> point1, List<List<Double>> pointList) {
        for (List<Double> point2 : pointList) {
            RouteFeature routeFeature = new RouteFeature();
            features.add(routeFeature);

            LineString lineString = new LineString();
            routeFeature.setGeometry(lineString);

            List<List<Double>> coordinates = new ArrayList<List<Double>>(2);
            lineString.setCoordinates(coordinates);

            coordinates.add(point1);
            coordinates.add(point2);
        }
    }


    public List<List<Double>> pointToRoute(List<Double> point, RouteFeatureCollection routeFeatureCollection) {
        Double shortDistance = 99999999.99;
        // 点
        Double lat0 = point.get(0);
        Double lng0 = point.get(1);

        LineString shortLineString = null;

        List<List<Double>> entryPoints = new ArrayList<List<Double>>();
        for (RouteFeature routeFeature : routeFeatureCollection.getFeatures()) {
            LineString lineString = routeFeature.getGeometry();
            for (int i=0; i<lineString.getCoordinates().size()-1; i++) {
                Double lat1 = lineString.getCoordinates().get(i).get(0);
                Double lng1 = lineString.getCoordinates().get(i).get(1);
                Double lat2 = lineString.getCoordinates().get(i+1).get(0);
                Double lng2 = lineString.getCoordinates().get(i+1).get(1);

                Double distance = PointToLine.pointToLineDistance(lat0, lng0, lat1, lng1, lat2, lng2);
                if (distance < shortDistance) {
                    entryPoints.clear();
                    shortDistance = distance;

                    List<Double> point1 = new ArrayList<Double>(2);
                    point1.add(lat1);
                    point1.add(lng1);
                    entryPoints.add(point1);

                    List<Double> point2 = new ArrayList<Double>(2);
                    point2.add(lat2);
                    point2.add(lng2);
                    entryPoints.add(point2);

                    shortLineString = lineString;
                }
            }
        }

        Double lat1 = entryPoints.get(0).get(0);
        Double lng1 = entryPoints.get(0).get(1);

        Double lat2 = entryPoints.get(1).get(0);
        Double lng2 = entryPoints.get(1).get(1);

        // 求垂足
        List<Double> footPoint = PointToLine.pointInLine(lat0, lng0, lat1, lng1, lat2, lng2);

        List<List<Double>> coordinates = shortLineString.getCoordinates();
        Integer index = coordinates.indexOf(entryPoints.get(0));
        coordinates.add(index+1, footPoint);


        List<List<Double>> pointList = new ArrayList<List<Double>>(1);
        pointList.add(footPoint);

        return pointList;
    }


    /**
     * 获取路径文件的数据结构
     * @return
     * @throws Exception
     */
    public Map<String, Map<String, Double>> routeMap(RouteFeatureCollection featureCollection) {
        Map<String, Map<String, Double>> oMap = new HashMap<String, Map<String, Double>>();
        // 路径点
        List<String> pointList = new ArrayList<String>();

        for (RouteFeature feature : featureCollection.getFeatures()) {
            LineString lineString = feature.getGeometry();
            List<List<Double>> coordinates = lineString.getCoordinates();

            System.out.println("点数：" + coordinates.size());
            for (int i = 0; i < coordinates.size() - 1; i ++) {
                Double lat1 = 0.0;
                Double lng1 = 0.0;
                Double lat2 = 0.0;
                Double lng2 = 0.0;

                List<Double> coordinate1 = coordinates.get(i);
                lat1 = coordinate1.get(1);
                lng1 = coordinate1.get(0);

                String sPoint = JSON.toJSONString(coordinate1);
                if (pointList.contains(sPoint)) {
                    System.out.println("点已存在");
                } else {
                    pointList.add(sPoint);
                }

                List<Double> coordinate2 = coordinates.get(i + 1);
                lat2 = coordinate2.get(1);
                lng2 = coordinate2.get(0);

                String ePoint = JSON.toJSONString(coordinate2);
                if (pointList.contains(ePoint)) {
                    System.out.println("点已存在");
                } else {
                    pointList.add(ePoint);
                }

                Double distance = PointToLine.pointsDistance(lat1, lng1, lat2, lng2);

                Map<String, Double> iMap1 = oMap.get(sPoint);
                if (iMap1 == null) {
                    iMap1 = new HashMap<String, Double>();
                    iMap1.put(ePoint, distance);
                    oMap.put(sPoint, iMap1);
                } else {
                    iMap1.put(ePoint, distance);
                }

                Map<String, Double> iMap2 = oMap.get(ePoint);
                if (iMap2 == null) {
                    iMap2 = new HashMap<String, Double>();
                    iMap2.put(sPoint, distance);
                    oMap.put(ePoint, iMap2);
                } else {
                    iMap2.put(sPoint, distance);
                }
            }
        }

        System.out.println(oMap);

        return oMap;
    }

    /**
     * 最短路径算法实现部分
     * @param routeMap
     * @param startPoint
     * @param endPoint
     * @return
     */
    public Map<String, Object> dijkstra(Map<String, Map<String, Double>> routeMap, List<Double> startPoint, List<Double> endPoint) {
        //最初顶点到原点最短距离值
        Map<String, Double> startDistance = new HashMap<String, Double>();
        //存储已经访问过的点
        Map<String, Integer> visited = new HashMap<String, Integer>();
        //记录前置节点，最终绘制路线
        Map<String, String> father = new HashMap<String, String>();

        String end = JSON.toJSONString(endPoint);
        String start = JSON.toJSONString(startPoint);

        // 第一步：构造初始条件
        visited.put(start, 1);

        for (String x : routeMap.get(start).keySet()) {
            Double distancex = routeMap.get(start).get(x);
            startDistance.put(x, distancex);
            father.put(x, start);
        }

        while (true) {
            // 第二步：找出未访问过的最短路径点
            Double shortDistance = 99999999.99;
            String shortPoint = "";
            for (String x : routeMap.get(start).keySet()) {
                if (visited.get(x) == null && (startDistance.get(x) < shortDistance)) {
                    shortDistance = startDistance.get(x);
                    shortPoint = x;
                }
            }

            visited.put(shortPoint, 1);

            System.out.println(end);
            if (shortPoint.equals(end)) {
                break;
            }

            if (shortPoint.length() < 1) {
                System.out.println("shortPoint====================="+shortPoint);
            }

            for (String x : routeMap.get(shortPoint).keySet()) {
                System.out.println("x====================="+x);
                //未访问过
                if (visited.get(x) == null) {
                    Double distancex = startDistance.get(x)==null?99999999.99:startDistance.get(x);
                    Double nDistancex = startDistance.get(shortPoint) + routeMap.get(shortPoint).get(x);
                    if (distancex > nDistancex) {
                        Map<String, Double> startMap = routeMap.get(start);
                        startMap.put(x, nDistancex);

                        startDistance.put(x, nDistancex);
                        father.put(x, shortPoint);
                    }
                }
            }
        }

        // 最短距离
        Double distance = startDistance.get(end);

        // 存储路径
        List<String> shortPath = new ArrayList<String>();

        while (true) {
            shortPath.add(end);
            if (end.equals(start)) {
                break;
            }
            end = father.get(end);
        }

        Map<String, Object> pathMap = new HashMap<String, Object>();
        pathMap.put("distance", distance);
        pathMap.put("path", shortPath);


        StringBuilder shorLen = new StringBuilder();
        for (int k = 0; k < shortPath.size(); k++) {
            if (k < shortPath.size()-1) {
                shorLen.append(shortPath.get(k) + "->");
            } else {
                shorLen.append(shortPath.get(k));
            }
        }

        System.out.println("最短路径" + shorLen.toString() + ",最短距离" + distance);


        return pathMap;
    }

    public RouteFeatureCollection pathFeatureCollection(List<String> path, RouteFeatureCollection routeFeatureCollection) {
        RouteFeatureCollection pathFeatureCollection = new RouteFeatureCollection();

        List<RouteFeature> features = new ArrayList<RouteFeature>(1);
        pathFeatureCollection.setFeatures(features);

        for (RouteFeature routeFeature : routeFeatureCollection.getFeatures()) {
            LineString lineString = routeFeature.getGeometry();

            List<List<Double>> coordinates = new ArrayList<List<Double>>();

            for (int i = 0; i < path.size(); i++) {
                List<Double> coordinate = new ArrayList<Double>(2);
                JSONArray jsonArray = JSON.parseArray(path.get(i));
                coordinate.add(jsonArray.getDouble(0));
                coordinate.add(jsonArray.getDouble(1));
                if (lineString.getCoordinates().contains(coordinate)) {
                    System.out.println("包含");
                    coordinates.add(coordinate);
                }
            }

            if (coordinates.size() > 1) {
                lineString.setCoordinates(coordinates);
                features.add(routeFeature);
            }
        }

        return pathFeatureCollection;
    }
}
