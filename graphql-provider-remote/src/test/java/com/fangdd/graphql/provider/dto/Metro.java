package com.fangdd.graphql.provider.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 地铁信息
 *
 * @author xuwenzhen
 * @date 2019/6/18
 */
public class Metro {
    /**
     * 地铁线号
     *
     * @demo 12
     */
    @JsonAlias("line_no")
    private int lineNo;

    /**
     * 地铁线名
     *
     * @demo 12号线
     */
    @JsonAlias("line_name")
    private String lineName;

    /**
     * 地铁线别名
     *
     * @demo XX线
     */
    @JsonAlias("line_alias")
    private String lineAlias;

    /**
     * 地铁站号
     *
     * @demo 32
     */
    @JsonAlias("station_no")
    private int stationNo;

    /**
     * 地铁站名
     *
     * @demo 七莘路
     */
    @JsonAlias("station_name")
    private String stationName;

    /**
     * 地铁站点和房源的距离，单位：米
     *
     * @demo 358
     */
    private int distance;

    /**
     * 地铁线颜色
     *
     * @demo 007A61
     */
    private String color;

    /**
     * 地铁站坐标
     *
     * @demo 31.137797, 121.36981800000000000
     */
    private String geo;

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getLineAlias() {
        return lineAlias;
    }

    public void setLineAlias(String lineAlias) {
        this.lineAlias = lineAlias;
    }

    public int getStationNo() {
        return stationNo;
    }

    public void setStationNo(int stationNo) {
        this.stationNo = stationNo;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }
}
