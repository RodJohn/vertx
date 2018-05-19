package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {

        //Server
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setPort(8080);
        HttpServer server = vertx.createHttpServer(httpServerOptions);

        //Router
        Router router = Router.router(vertx);
        server.requestHandler(router::accept);

        //Route
        Route route = router.route();
        route.handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");
            response.end("Hello World from Vert.x-Web!");
        });

    }
}
