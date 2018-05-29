package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MainStart extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(MainVerticle.class.getName());
    }
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName());
    }
}
