

# 依赖

Vert.x Web Client（Web客户端）是一个异步的 HTTP 和 HTTP/2 客户端。


制作Web Client的目的并非为了替换Vert.x Core中的 HttpClient，而是基于该客户端，扩展并保留其便利的设置和特性，例如请求连接池（Pooling），HTTP/2的支持，流水线／管线的支持等。当您需要对 HTTP 请求和响应做细微粒度控制时，您应当使用 HttpClient。



# 依赖

dependencies {
  compile 'io.vertx:vertx-web-client:3.4.2'
}

# 创建

WebClient client = WebClient.create(vertx);




# 简单使用

// 发送GET请求
client
  .get(8080, "myserver.mycompany.com", "/some-uri")
  .send(ar -> {
    if (ar.succeeded()) {
      // 获取响应
      HttpResponse<Buffer> response = ar.result();

      System.out.println("Received response with status code" + response.statusCode());
    } else {
      System.out.println("Something went wrong " + ar.cause().getMessage());
    }
  });


您可用以下链式方式向请求URI添加查询参数

client
  .get(8080, "myserver.mycompany.com", "/some-uri")
  .addQueryParam("param", "param_value")
  .send(ar -> {});
  
  
# 请求体

如需要发送请求体，可使用相同的API并在最后加上 sendXXX 方法发送相应的请求体。

例如用 sendBuffer 方法发送一个缓冲体：

client
  .post(8080, "myserver.mycompany.com", "/some-uri")
  .sendBuffer(buffer, ar -> {
    if (ar.succeeded()) {
      // Ok
    }
  });
  
# json


JSON体
有时您需要在请求体中使用JSON格式，可使用 sendJsonObject 方法发送 JsonObject：

client
  .post(8080, "myserver.mycompany.com", "/some-uri")
  .sendJsonObject(new JsonObject()
    .put("firstName", "Dale")
    .put("lastName", "Cooper"), ar -> {
    if (ar.succeeded()) {
      // Ok
    }
  });
在Java，Groovy以及Kotlin语言中，您亦可使用 sendJson 方法发送POJO（Plain Old Java Object），该方法会自动调用 Json.encode 方法将 POJO 映射为 JSON：

client
  .post(8080, "myserver.mycompany.com", "/some-uri")
  .sendJson(new User("Dale", "Cooper"), ar -> {
    if (ar.succeeded()) {
      // Ok
    }
  });  
  
  
  
# 请求头

您可使用以下方式填充请求头：

HttpRequest<Buffer> request = client.get(8080, "myserver.mycompany.com", "/some-uri");
MultiMap headers = request.headers();
headers.set("content-type", "application/json");
headers.set("other-header", "foo");
此处 Headers 是一个 MultiMap 对象，提供了增加、设置以及删除头属性操作的入口。HTTP头的某些特定属性允许设置多个值。

您亦可通过 putHeader 方法写入头属性：

HttpRequest<Buffer> request = client.get(8080, "myserver.mycompany.com", "/some-uri");
request.putHeader("content-type", "application/json");
request.putHeader("other-header", "foo");
  
  
# HTTP


Web Client请求发送之后，返回的结果将会被包装在异步结果 HttpResponse 中。


## 

client
  .get(8080, "myserver.mycompany.com", "/some-uri")
  .as(BodyCodec.jsonObject())
  .send(ar -> {
    if (ar.succeeded()) {
      HttpResponse<JsonObject> response = ar.result();

      JsonObject body = response.body();

      System.out.println("Received response with status code" + response.statusCode() + " with body " + body);
    } else {
      System.out.println("Something went wrong " + ar.cause().getMessage());
    }
  });
  
  
  
## 

  
    
  
  
  