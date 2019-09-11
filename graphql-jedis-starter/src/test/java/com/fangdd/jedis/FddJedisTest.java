package com.fangdd.jedis;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * @author xuwenzhen
 */
public class FddJedisTest extends BaseJunitTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void test() {
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();

        String key = "hello";
        String value = "world";
        opsForValue.set(key, value, 1, TimeUnit.SECONDS);

        String v = (String) opsForValue.get(key);
        Assert.assertEquals(value, v);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String v2 = (String) opsForValue.get(key);
        Assert.assertNull(v2);
    }
}
