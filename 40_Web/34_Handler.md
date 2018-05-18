


# 上下文数据

  在请求的生命周期中，
  您可以通过路由上下文 RoutingContext 来维护任何您希望在处理器之间共享的数据。
  
# 处理请求

处理请求消息体
您可以使用消息体处理器 BodyHandler 来获取请求的消息体，限制消息体大小，或者处理文件上传。

您需要保证消息体处理器能够匹配到所有您需要这个功能的请求。

由于它需要在所有异步执行之前处理请求的消息体，因此这个处理器要尽可能早地设置到 router 上。

router.route().handler(BodyHandler.create());
获取请求的消息体
如果您知道消息体的类型是 JSON，您可以使用 getBodyAsJson；如果您知道它的类型是字符串，您可以使用 getBodyAsString；否则可以通过 getBody 作为 Buffer 来处理。


# 会话

  