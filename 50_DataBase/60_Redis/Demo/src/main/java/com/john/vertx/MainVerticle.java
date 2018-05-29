package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {


        RedisOptions config = new RedisOptions();
        config.setHost("127.0.0.1");
        config.setPort(6379);
        config.setSelect(1);

        RedisClient redisClient = RedisClient.create(vertx, config);

        redisClient.set("testk", "testv", rs -> {
            if (rs.failed()) {
                System.out.println(rs.cause().getMessage());
                System.out.println(rs.cause().getCause());
            }else {
                System.out.println(rs.result());
            }
        });


        redisClient.get("testk",rs->{
            if (rs.succeeded()) {
                System.out.println(rs.result());
            }else {
                System.out.println(rs.cause().getMessage());
                System.out.println(rs.cause().getCause());
            }
        });

    }
}
