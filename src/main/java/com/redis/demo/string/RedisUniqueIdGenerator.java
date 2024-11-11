package com.redis.demo;


import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RedisUniqueIdGenerator {
    private static final String REDIS_HOST = "117.72.84.121";
    private static final int REDIS_PORT = 6379;
    private static final int PASS_WORD = 123456;
    private static final String ID_KEY_PREFIX = "unique_id:";
    private static final String DATE_FORMAT = "yyyyMMdd"; // 用于前缀

    private Jedis jedis;


    public RedisUniqueIdGenerator() {
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.auth(String.valueOf(PASS_WORD));
    }

    public String generateUniqueId(String keyPrefix) {
        // 获取当前日期作为前缀
        String datePrefix = "20241111";
        // Redis key，用于自增计数
        String redisKey = keyPrefix + ":" + datePrefix;

        // 自增操作
        long uniqueId = jedis.incr(redisKey);
        // 设置过期时间（例如，一天），防止 key 无限增加
        jedis.expire(redisKey, 86400);

        // 拼接生成全局唯一 ID
        return keyPrefix + datePrefix + String.format("%05d", uniqueId);
    }


    public String getId(){
        return jedis.get("ORDER:20241111");
    }

    public static void main(String[] args) {
        RedisUniqueIdGenerator generator = new RedisUniqueIdGenerator();

        String uniqueId = generator.generateUniqueId("ORDER");

        String id = generator.getId();

        System.out.println("id = " + id);
        System.out.println("Generated Unique ID: " + uniqueId);
    }
}
