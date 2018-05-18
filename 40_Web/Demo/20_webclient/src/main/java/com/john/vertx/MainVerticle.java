package com.john.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class MainVerticle extends AbstractVerticle {

    private String url = "http://www.sit1.bwoilmarine.com/api/im/contact/194574432205800473";


    @Override
    public void start() throws Exception {

        HttpClient httpClient = vertx.createHttpClient();
        httpClient.get(8080,"127.0.0.1","/").handler(res->{
            System.out.println(res.statusCode());
            res.bodyHandler(body->{
                System.out.println(body.toString());
            });
            System.out.println(res.headers().entries());
        }).end();

//        WebClient
//                .create(vertx)
//                .get(8080,"127.0.0.1","/")
//                .send(ar -> {
//                    if (ar.succeeded()) {
//                        HttpResponse<Buffer> response = ar.result();
//                        System.out.println(response.statusCode());
//                        System.out.println(response.bodyAsString());
//                        System.out.println(response.headers().entries());
//                    } else {
//                        System.out.println("Something went wrong " + ar.cause().getMessage());
//                    }
//                });

//        200
//        Hello World from Vert.x-Web!
//        [content-type=text/plain, Content-Length=28]

    }
}
