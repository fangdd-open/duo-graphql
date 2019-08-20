package com.fangdd.graphql.provider.controller;

import com.fangdd.graphql.provider.annotation.GraphqlModule;
import com.fangdd.graphql.provider.dto.House;
import com.fangdd.graphql.provider.dto.Pagination;
import com.fangdd.graphql.provider.dto.Trade;

import java.util.List;

/**
 * @author xuwenzhen
 */
@GraphqlModule("test")
public class HouseController {
    public List<House> test() {
        return null;
    }

    public House test1() {
        return null;
    }

    public Pagination<House, Integer> test2() {
        return null;
    }


    public Pagination<Trade, Integer> tradeSearch() {
        return null;
    }
}
