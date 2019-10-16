package com.fangdd.graphql.core.util;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author xuwenzhen
 */
public class OkHttpUtilsTest {
    @Test
    public void formatSelections() throws Exception {
        List<String> selections = Lists.newArrayList(
                "id",
                "name",
                "flat.id",
                "flat.name",
                "agent.id",
                "agent.name",
                "agent.store.id",
                "agent.store.name",
                "house.city.name"
        );
        String selectionStr = OkHttpUtils.formatSelections(selections);
        System.out.println(selectionStr);
        Assert.assertEquals(selectionStr, "id!name!flat!.id!.name!agent!.id!.name!.store!..id!..name!house!.city!..name");
    }
}