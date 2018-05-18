package com.john.vertx;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;


public class HttpProxyHandler implements Handler<RoutingContext> {

    private HttpClient httpClient;

    private String url = "http://www.sit1.bwoilmarine.com/api/im/contact/194574432205800473";

    private String repLen ="";

    public HttpProxyHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void handle(RoutingContext rct) {

        HttpServerRequest request = rct.request();
        HttpServerResponse response = rct.response();

        response.putHeader("Content-Type", "application/json;charset=UTF-8");
        response.setChunked(true);

        HttpClientRequest proxyRequest = httpClient.requestAbs(HttpMethod.GET, url).setTimeout(2000);

        proxyRequest.handler(proxyResponse -> {
            Pump respPump = Pump.pump(proxyResponse, response);
            respPump.start();
            proxyResponse.endHandler(res -> {
                response.end();
                repLen = proxyResponse.getHeader("Content-Length");
            });
        });

        proxyRequest.setChunked(true);
        Pump reqPump = Pump.pump(request, proxyRequest);
        reqPump.start();
        request.endHandler(end -> {
            proxyRequest.end();
        });
    }

}
