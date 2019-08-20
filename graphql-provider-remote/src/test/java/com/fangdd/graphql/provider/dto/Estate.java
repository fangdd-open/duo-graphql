package com.fangdd.graphql.provider.dto;

import com.fangdd.graphql.provider.annotation.GraphqlAlias;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;
import java.util.List;

/**
 * 二手房房源
 *
 * @author xuwenzhen
 * @date 2019/6/14
 */
public class Estate implements Serializable {
    /**
     * 二手房房源id
     *
     * @demo
     */
    @JsonAlias("cp_estate_id")
    private Integer id;

    /**
     * 地铁信息
     */
    @GraphqlAlias("transportation.metro_detail")
    private List<Metro> metros;

    /**
     * 学区信息
     */
    @JsonAlias("school")
    private List<School> schools;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Metro> getMetros() {
        return metros;
    }

    public void setMetros(List<Metro> metros) {
        this.metros = metros;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }
}
