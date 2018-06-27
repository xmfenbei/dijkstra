package com.xmfenbei.dijkstra.geometry;

import java.util.ArrayList;
import java.util.List;

public class PointToLine {

    /**
     * 计算两点之间的距离
     * @param x1 A点x坐标
     * @param y1 A点y坐标
     * @param x2 B点x坐标
     * @param y2 B点y坐标
     * @return 两点之间距离
     */
    public static Double pointsDistance(Double x1, Double y1, Double x2, Double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 点到线段的最短距离 -- 点A(x0, y0)到由两点B(x1,y1),C(x2,y2)组成的线段的距离
     * @param x0 A点x坐标
     * @param y0 A点y坐标
     * @param x1 B点x坐标
     * @param y1 B点y坐标
     * @param x2 C点x坐标
     * @param y2 C点y坐标
     * @return 点到线段的最短距离
     */
    public static Double pointToLineDistance(Double x0, Double y0, Double x1, Double y1, Double x2, Double y2) {
        Double distance = 0.0;
        // BC
        Double bc = pointsDistance(x1, y1, x2, y2);
        // BA
        Double ba = pointsDistance(x1, y1, x0, y0);
        // CA
        Double ca = pointsDistance(x2, y2, x0, y0);
        // 点在线段上
        if (ca+ba == bc) {
            return distance;
        }
        // 组成直角三角形或钝角三角形，B为直角或钝角
        if (ca * ca >= bc * bc + ba * ba) {
            distance = ba;
            return distance;
        }
        // 组成直角三角形或钝角三角形，C为直角或钝角
        if (ba * ba >= bc * bc + ca * ca) {
            distance = ca;
            return distance;
        }
        // 组成锐角三角形，则求三角形的高
        System.out.println("组成锐角三角形，则求三角形的高。");
        // 半周长
        Double p = (bc + ba + ca) / 2;
        // 海伦公式求面积
        Double s = Math.sqrt(p * (p - bc) * (p - ba) * (p - ca));
        // 返回点到线的距离（利用三角形面积公式求高）
        distance = 2 * s / bc;

        return distance;
    }

    /**
     * 点与线段的垂足 -- 点A(x0, y0)到由两点B(x1,y1),C(x2,y2)组成的线段
     * @param x0 A点x坐标
     * @param y0 A点y坐标
     * @param x1 B点x坐标
     * @param y1 B点y坐标
     * @param x2 C点x坐标
     * @param y2 C点y坐标
     * @return 垂足
     */
    public static List<Double> footPoint(Double x0, Double y0, Double x1, Double y1, Double x2, Double y2) {
        Double p = ((x0 - x1) * (x2 - x1) + (y0 - y1) * (y2 - y1));
        Double se = ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        // k即点到线段的投影长度与线段长度比
        Double k = p/se;
        // 垂足坐标
        List<Double> point = new ArrayList<Double>(2);
        Double x = x1 + k * (x2 - x1);
        point.add(x);
        Double y = y1 + k * (y2 - y1);
        point.add(y);

        return point;
    }

    /**
     * 点映射成线段上的点 -- 点A(x0, y0)到由两点B(x1,y1),C(x2,y2)组成的线段
     * @param x0 A点x坐标
     * @param y0 A点y坐标
     * @param x1 B点x坐标
     * @param y1 B点y坐标
     * @param x2 C点x坐标
     * @param y2 C点y坐标
     * @return 线段上的点
     */
    public static List<Double> pointInLine(Double x0, Double y0, Double x1, Double y1, Double x2, Double y2) {
        List<Double> point = new ArrayList<Double>(2);
        // BC
        Double bc = pointsDistance(x1, y1, x2, y2);
        // BA
        Double ba = pointsDistance(x1, y1, x0, y0);
        // CA
        Double ca = pointsDistance(x2, y2, x0, y0);
        // 点在线段上
        if (ca+ba == bc) {
            point.add(x0);
            point.add(y0);
        }
        // 组成直角三角形或钝角三角形，b为直角或钝角
        if (ca * ca >= bc * bc + ba * ba) {
            point.add(x1);
            point.add(y1);
        }
        // 组成直角三角形或钝角三角形，c为直角或钝角
        if (ba * ba >= bc * bc + ca * ca) {
            point.add(x2);
            point.add(y2);
        }
        // 组成锐角三角形，则求垂足
        point = footPoint(x0, y0, x1, y1, x2, y2);

        return point;
    }
}
