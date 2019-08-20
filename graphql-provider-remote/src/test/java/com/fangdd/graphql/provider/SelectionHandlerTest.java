package com.fangdd.graphql.provider;

import com.fangdd.graphql.provider.controller.EstateController;
import com.fangdd.graphql.provider.controller.HouseController;
import com.fangdd.graphql.provider.dto.House;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuwenzhen
 */
public class SelectionHandlerTest {
    @Test
    public void getClassNameMap() throws Exception {
        Method method = HouseController.class.getMethod("test1");
        SelectionHandler.analyseTypeMapping(method);
        System.out.println(SelectionHandler.print());
    }

    @Test
    public void getSelections() throws Exception {
        String selections = "id!name!flat!.id!.name!image!.url!houseList!.name";

        Method method = HouseController.class.getMethod("test1");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        System.out.println(SelectionHandler.print());

        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, null);
        Assert.assertNotNull(newSelections);
        System.out.println("Selections: " + newSelections.stream().collect(Collectors.joining(",")));

        Assert.assertEquals("houseId", newSelections.get(0));
        Assert.assertEquals("houseName", newSelections.get(1));
        Assert.assertEquals("houseFlat.flatId", newSelections.get(2));
        Assert.assertEquals("houseFlat.flatName", newSelections.get(3));
        Assert.assertEquals("gallery.url", newSelections.get(4));
        Assert.assertEquals("houses.houseName", newSelections.get(5));
        newSelections.forEach(System.out::println);
    }

    @Test
    public void getSelectionsWithGraphqlField() throws Exception {
        String selections = "id!name!flat!.id!.name!image!.url!houseList!.name!tags";

        Method method = HouseController.class.getMethod("test1");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        System.out.println(SelectionHandler.print());

        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, null);
        Assert.assertNotNull(newSelections);
        System.out.println("Selections: " + newSelections.stream().collect(Collectors.joining(",")));

        Assert.assertEquals(9, newSelections.size());
        Assert.assertEquals("houseId", newSelections.get(0));
        Assert.assertEquals("houseName", newSelections.get(1));
        Assert.assertEquals("houseFlat.flatId", newSelections.get(2));
        Assert.assertEquals("houseFlat.flatName", newSelections.get(3));
        Assert.assertEquals("gallery.url", newSelections.get(4));
        Assert.assertEquals("houses.houseName", newSelections.get(5));
        Assert.assertEquals("tagList", newSelections.get(6));
        Assert.assertEquals("car", newSelections.get(7));
        Assert.assertEquals("onSale", newSelections.get(8));
        newSelections.forEach(System.out::println);
    }

    @Test
    public void getSelectionsPrefix() throws Exception {
        getSelectionsPrefix2();
        getSelectionsPrefix3();

    }

    @Test
    public void getSelectionsPrefix2() throws Exception {
        String selections = "list!.id!.name!.flat!..id!..name";
        Method method = HouseController.class.getMethod("test2");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, "list");
        System.out.println("house selections: " + newSelections.stream().collect(Collectors.joining(",")));
        Assert.assertNotNull(newSelections);
        Assert.assertEquals("houseId", newSelections.get(0));
        Assert.assertEquals("houseName", newSelections.get(1));
        Assert.assertEquals("houseFlat.flatId", newSelections.get(2));
        Assert.assertEquals("houseFlat.flatName", newSelections.get(3));
        newSelections.forEach(System.out::println);
    }

    @Test
    public void getSelectionsPrefix3() throws Exception {
        String selections = "list!.id!.cellId!.cellName!total";

        Method method = HouseController.class.getMethod("tradeSearch");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, "list");
        System.out.println("trade selections: " + newSelections.stream().collect(Collectors.joining(",")));
        Assert.assertNotNull(newSelections);
        Assert.assertEquals("tradeId", newSelections.get(0));
        Assert.assertEquals("cellId", newSelections.get(1));
        Assert.assertEquals("cellName", newSelections.get(2));
        newSelections.forEach(System.out::println);
    }

    /**
     * 多级属性的测试
     * @throws Exception
     */
    @Test
    public void getSelectionsPrefix4() throws Exception {
        String selections = "list!.metros!..stationName!.schools!..schoolName";
        Method method = EstateController.class.getMethod("search");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, "list");
        System.out.println("estate selections: " + newSelections.stream().collect(Collectors.joining(",")));
        Assert.assertNotNull(newSelections);
        Assert.assertEquals("transportation.metro_detail.station_name", newSelections.get(0));
        Assert.assertEquals("school.school_name", newSelections.get(1));
        newSelections.forEach(System.out::println);
    }

    @Test
    public void getGenericSelections() throws Exception {
        String selections = "id!name!flat!.id!.name";

        Method method = HouseController.class.getMethod("test");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, null);

        Assert.assertNotNull(newSelections);
        Assert.assertEquals("houseId", newSelections.get(0));
        Assert.assertEquals("houseName", newSelections.get(1));
        Assert.assertEquals("houseFlat.flatId", newSelections.get(2));
        Assert.assertEquals("houseFlat.flatName", newSelections.get(3));
        newSelections.forEach(System.out::println);
    }

    @Test
    public void objectMapperSerial() {
        String json = "{\"houseName\": \"楼盘名称\", \"cityId\": 1337, \"houseFlat\": {\"id\": 23445}}";
        ObjectMapper objectMapper = getObjectMapper();

        House house = null;
        try {
            house = objectMapper.readValue(json, House.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(house);
        Assert.assertEquals("楼盘名称", house.getName());


        String houseStr = null;
        try {
            houseStr = objectMapper.writeValueAsString(house);
            System.out.println(houseStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(houseStr);
        Assert.assertTrue(houseStr.contains("name"));
    }

    @Test
    public void objectMapperTypeSerial() throws Exception {
        String selections = "id!name!flat!.id!.name!image!.url!houseList!.name!tags";
        Method method = HouseController.class.getMethod("test1");
        PojoSelection pojoSelection = SelectionHandler.analyseTypeMapping(method);
        List<String> newSelections = SelectionHandler.getSelections(pojoSelection, selections, null);
        String json = "{\"id\": 1234,\"houseName\": \"楼盘名称\", \"hasCarService\": 1, \"onSale\": 1, \"tagList\": [\"南北通透\", \"满五唯一\"]}";
        ObjectMapper objectMapper = getObjectMapper();
        House house = null;
        try {
            house = objectMapper.readValue(json, House.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(house);
        Assert.assertEquals("楼盘名称", house.getName());
        Assert.assertNotNull(house.getTags());
        Assert.assertTrue(house.getTags().contains("专车接送"));
        Assert.assertTrue(house.getTags().contains("在售"));
    }

    private ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                    //反序列化时，忽略目标对象没有的属性
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                    //下面配置是值为null时，不显示
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)

                    //下面一个配置是集合返回为空时，不显示
                    .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    }

}