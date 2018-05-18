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


git rm -r --cached  40_Web\Demo\20_webclient\.gradle\4.4.1\fileChanges\last-build.bin
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/fileContent/annotation-processors.bin
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/fileContent/fileContent.lock
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/fileHashes/fileHashes.bin
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/fileHashes/fileHashes.lock
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/fileHashes/resourceHashesCache.bin
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/taskHistory/taskHistory.bin
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/4.4.1/taskHistory/taskHistory.lock
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/buildOutputCleanup/buildOutputCleanup.lock
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/buildOutputCleanup/cache.properties
git rm -r --cached   40_Web/Demo/20_webclient/.gradle/buildOutputCleanup/outputFiles.bin
git rm -r --cached   40_Web/Demo/20_webclient/.idea/modules/10_web.iml
git rm -r --cached   40_Web/Demo/20_webclient/.idea/modules/10_web_main.iml
git rm -r --cached   40_Web/Demo/20_webclient/.idea/modules/10_web_test.iml
git rm -r --cached   40_Web/Demo/30_webproxy/.idea/libraries/Gradle__io_netty_netty_handler_proxy_4_1_19_Final.xml
