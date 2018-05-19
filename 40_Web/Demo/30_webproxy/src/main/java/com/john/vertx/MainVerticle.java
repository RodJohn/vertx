package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {

        HttpServer server = vertx.createHttpServer();

        HttpClient httpClient = vertx.createHttpClient();

        Router router = Router.router(vertx);

        router.route().order(1).handler(new HttpProxyHandler(httpClient));
//        router.route().handler(routingContext -> {
//            HttpServerResponse response = routingContext.response();
//            response.putHeader("content-type", "text/plain");
//            response.end("Hello World from Vert.x-Web!");
//        });

        server.requestHandler(router::accept).listen(8090);




    }

}
