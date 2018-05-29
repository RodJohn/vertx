package com.john.vertx;

import io.vertx.core.AbstractVerticle;

public class MyFirstVerticle extends AbstractVerticle {

    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end(" Just a Demo !");
                }).listen(8080);
    }
}
