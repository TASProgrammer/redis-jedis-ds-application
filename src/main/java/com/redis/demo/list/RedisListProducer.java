package com.redis.demo.list;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisRateLimiter {
    private static final String REDIS_HOST = "117.72.84.121";
    private static final int REDIS_PORT = 6379;
    private static final String KEY_PREFIX = "rate_limiter:";
    private static final int MAX_REQUESTS = 5;      // 限制请求数量
    private static final int TIME_WINDOW = 60;      // 时间窗口（秒）

    private Jedis jedis;

    public RedisRateLimiter() {
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.auth("123456");
    }

    public boolean producer(String key) {

        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        String redisKey = KEY_PREFIX + key;
        long requestCount = jedis.llen(redisKey) + 1;

        String lindex = jedis.lindex(redisKey, 0);
        long earliestTime = currentTime;
        if(lindex != null){
            earliestTime = Long.parseLong(lindex) / 1000;
        }
        // 开启事务
        Transaction multi = jedis.multi();


        // 检查是否超过最大请求数量
        if (requestCount > MAX_REQUESTS) {
            // 如果最早请求在时间窗口之外，则移除它
            if (currentTime - earliestTime >= TIME_WINDOW) {
                System.out.println("移除了一个最早的请求");
                multi.lpop(redisKey); // 移除最早的请求
            } else {
                multi.exec(); // 提交事务
                return false; // 超出限制，拒绝请求
            }
        }

        // 向 List 中添加当前请求时间戳（秒）
        multi.rpush(redisKey, String.valueOf(currentTime));
        // 设置 List 的过期时间，保证旧的请求自动过期
        multi.expire(redisKey, TIME_WINDOW);

        multi.exec(); // 提交事务
        return true; // 允许请求
    }






    public static void main(String[] args) {
        RedisRateLimiter rateLimiter = new RedisRateLimiter();
        String key = "user:123"; // 限流的对象（如用户ID）

        for (int i = 0; i < 10; i++) {
            if (rateLimiter.producer(key)) {
                System.out.println("Request " + (i + 1) + " is allowed");
            } else {
                System.out.println("Request " + (i + 1) + " is denied due to rate limit");
            }

            try {
                Thread.sleep(500); // 模拟请求间隔
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
