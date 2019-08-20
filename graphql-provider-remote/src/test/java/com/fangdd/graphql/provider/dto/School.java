package com.fangdd.graphql.provider.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 学校信息
 *
 * @author xuwenzhen
 * @date 2019/6/18
 */
public class School {
    /**
     * 学校id
     *
     * @demo 297
     */
    @JsonAlias("school_id")
    private Long schoolId;

    /**
     * 学校名称
     *
     * @demo 上海交通大学附属实验小学
     */
    @JsonAlias("school_name")
    private String schoolName;

    /**
     * 学校简称
     *
     * @demo 交大附小
     */
    private String alias;

    /**
     * 城市id
     *
     * @demo 121
     */
    @JsonAlias("city_id")
    private Long cityId;

    /**
     * 区域id
     *
     * @demo 977
     */
    @JsonAlias("district_id")
    private Long districtId;

    /**
     * 板块id
     *
     * @demo 12247
     */
    @JsonAlias("section_id")
    private Long sectionId;

    /**
     * 学校阶段：1-小学；2-9年制学校（小学+中学）；
     *
     * @demo 1
     */
    private Integer stage;

    /**
     * 学校性质：1-公办；2-私立；
     *
     * @demo 1
     */
    private Integer type;

    /**
     * 学校等级：1-普通；2-区重点；3-市重点；
     *
     * @demo 3
     */
    private Integer level;

    /**
     * 学校地址
     *
     * @demo 德宏路2366号
     */
    private String address;

    /**
     * 学校电话
     *
     * @demo 54736653
     */
    private String telephone;

    /**
     * 学校简介
     *
     * @demo 上海交通大学附属实验小学于2004年由上海交通大学和闵行区教育局联合创办...
     */
    private String description;

    /**
     * 经度
     *
     * @demo 121.441247
     */
    private Double lng;

    /**
     * 维度
     *
     * @demo 31.019843
     */
    private Double lat;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
