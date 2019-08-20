package com.fangdd.graphql.provider.dto;

import com.fangdd.graphql.provider.annotation.GraphqlAlias;
import com.fangdd.graphql.provider.annotation.GraphqlField;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xuwenzhen
 */
public class House implements Serializable {
    private static final String STR_ONSALE = "在售";
    private static final String STR_CAR_SERVICE = "专车接送";
    @JsonAlias("houseId")
    private Integer id;

    @JsonAlias("houseName")
    private String name;

    @JsonAlias("houseFlat")
    private HouseFlat flat;

    private Integer flatId;

    @GraphqlAlias("gallery")
    private List<HouseImage> image;

    private Map<String, HouseImage> gallery;

    @GraphqlAlias("houses")
    private List<House> houseList;

    @JsonAlias("car")
    private Integer hasCarService;

    private Integer onSale;

    @JsonAlias("tagList")
    @GraphqlField(dependency = {"hasCarService", "onSale"})
    private List<String> tags;

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

    public HouseFlat getFlat() {
        return flat;
    }

    public void setFlat(HouseFlat flat) {
        this.flat = flat;
    }

    public Integer getFlatId() {
        return flatId;
    }

    public void setFlatId(Integer flatId) {
        this.flatId = flatId;
    }

    public List<HouseImage> getImage() {
        return image;
    }

    public void setImage(List<HouseImage> image) {
        this.image = image;
    }

    public Map<String, HouseImage> getGallery() {
        return gallery;
    }

    public void setGallery(Map<String, HouseImage> gallery) {
        this.gallery = gallery;
    }

    public List<House> getHouseList() {
        return houseList;
    }

    public void setHouseList(List<House> houseList) {
        this.houseList = houseList;
    }

    public Integer getHasCarService() {
        return hasCarService;
    }

    public void setHasCarService(Integer hasCarService) {
        this.hasCarService = hasCarService;
    }

    public Integer getOnSale() {
        return onSale;
    }

    public void setOnSale(Integer onSale) {
        this.onSale = onSale;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        //这里还需要做防止重复读取的问题
        if (this.getOnSale() != null && getOnSale().equals(1) && !tags.contains(STR_ONSALE)) {
            tags.add(0, STR_ONSALE);
        }
        if (this.getHasCarService() != null && this.getHasCarService().equals(1) && !tags.contains(STR_CAR_SERVICE)) {
            tags.add(0, STR_CAR_SERVICE);
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
