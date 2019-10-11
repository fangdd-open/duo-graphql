package com.fangdd.graphql.provider.controller;

import com.fangdd.graphql.provider.annotation.GraphqlModule;
import com.fangdd.graphql.provider.dto.*;

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

    public Pagination<House> test2() {
        return null;
    }


    public Pagination<Trade> tradeSearch() {
        return null;
    }

    public AggregatePagination<Estate> test3() {
        return null;
    }
}
