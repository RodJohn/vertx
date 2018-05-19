
# 概述

作用

    Vert.x Web 基于 Vert.x Core, 提供更好的封装  

依赖

    dependencies {
      compile 'io.vertx:vertx-web:3.4.2'
    }


# 工作流程

Server

    Server用于监听指定端口,
    并将请求传递给Router

Router

    Router 接收来自Server的请求，
    并按顺序查找首个匹配该请求的Route，
    然后将请求传递给这个 Route。
    
Route(*)

    Route用handler异步处理请求
    然后结束响应或者把请求传递给下一个匹配的Route。


# 示例

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






  