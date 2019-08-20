package com.fangdd.graphql.provider.dto;


import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

/**
 * @author xuwenzhen
 */
public class HouseFlat {
    @JsonAlias("flatId")
    private Integer id;

    @JsonAlias("flatName")
    private String name;

    private List<House> houseList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<House> getHouseList() {
        return houseList;
    }

    public void setHouseList(List<House> houseList) {
        this.houseList = houseList;
    }
}
