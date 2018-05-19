
# Route

    Route是处理的核心

# 1 路径匹配


## 1.2 精确匹配

作用

    匹配指定的 URI
    忽略/
    
举例

    router.route("/some/path/");
    
    
    /some/path
    /some/path/
    /some/path//
    

## 1.3 前缀匹配

作用

    拥有指定前缀的
    
举例

    router.route("/some/path*");
    
    
    /some/path
    /some/path/
    /some/path/subdir


## 1.4 路径占位符


## 1.6 子路由

作用

    路由嵌套
    路径叠加
    
示例

    Router mainRouter = Router.router(vertx);       
    Router restAPI = Router.router(vertx);       
    mainRouter.mountSubRouter("/productsAPI", restAPI);
    restAPI.get("/products/:productID").handler(rc -> {});
    
    访问路径
    /productsAPI/products/product1234


# 3 路由执行

## 过程

    当一个请求到达时，Router 会一步一步检查每一个 Route 是否匹配，如果匹配则对应的处理器会被调用。
    如果处理器随后调用了 next，则下一个匹配的 Route 对应的处理器（如果有）会被调用，以此类推。

路由顺序
    
    默认的路由的匹配顺序与添加到 Router 的顺序一致。
    如果您想覆盖路由默认的顺序，您可以通过 order 方法为每一个路由指定一个 integer 值。



## 路由控制

作用

    response().end()
    
    routingContext.next();

示例

    Route route1 = router.route("/some/path/").handler(routingContext -> {
        HttpServerResponse response = routingContext.response();
        response.setChunked(true);
        response.write("route1\n");
        routingContext.next();
    });

    Route route2 = router.route("/some/path/").handler(routingContext -> {
        routingContext.response().write("route2\n");
        routingContext.next();
    });

    Route route3 = router.route("/some/path/").handler(routingContext -> {
        routingContext.response().write("route3").end();
    });




## 转发


    router.get("/some/path").handler(routingContext -> {
      routingContext.reroute("/some/path/B");
    });


# 特殊

## 默认的 404 处理器
    
    如果没有为请求匹配到任何路由，Vert.x Web 会声明一个 404 错误。
    
    这可以被您自己实现的处理器处理，或者被我们提供的专用错误处理器（failureHandler）处理。
    如果没有提供错误处理器，Vert.x Web 会发送一个基本的 404 (Not Found) 响应。

##  错误处理
    
    和设置处理器处理请求一样，您可以设置处理器处理路由过程中的失败。
    
    失败处理器和普通的处理器具有完全一样的路由匹配规则。
    
    例如您可以提供一个失败处理器只处理在某个路径上发生的失败，或某个 HTTP 方法。
    
    这允许您在应用的不同部分设置不同的失败处理器。

# 启用和停用 Route

您可以通过 disable 方法来停用一个 Route。停用的 Route 在匹配时会被忽略。

您可以用 enable 方法来重新启用它。


