

# 概述

Event Bus 是 Vert.x 的神经系统。

每一个 Vert.x 实例都有一个单独的 Event Bus 实例。您可以通过 Vertx 实例的 eventBus 方法来获得对应的 EventBus 实例。

您的应用中的不同部分通过 Event Bus 相互通信，无论它们使用哪一种语言实现，无论它们在同一个 Vert.x 实例中或在不同的 Vert.x 实例中。

甚至可以通过桥接的方式允许在浏览器中运行的客户端JavaScript在相同的Event Bus上相互通信。

Event Bus可形成跨越多个服务器节点和多个浏览器的点对点的分布式消息系统。

Event Bus支持发布/订阅、点对点、请求/响应的消息通信方式。

Event Bus的API很简单。基本上只涉及注册处理器、撤销处理器和发送和发布消息。



# 

寻址
消息会被 Event Bus 发送到一个 地址(address)。

同任何花哨的寻址方案相比，Vert.x的地址格式并不麻烦。Vert.x中的地址是一个简单的字符串，任意字符串都合法。当然，使用某种模式来命名仍然是明智的。如：使用点号来划分命名空间。

一些合法的地址形如：europe.news.feed1、acme.games.pacman、sausages和X。

处理器
消息在处理器（Handler）中被接收。您可以在某个地址上注册一个处理器来接收消息。

同一个地址可以注册许多不同的处理器，一个处理器也可以注册在多个不同的地址上。

发布/订阅消息
Event Bus支持 发布消息 功能。

消息将被发布到一个地址中，发布意味着会将信息传递给 所有 注册在该地址上的处理器。这和 发布/订阅模式 很类似。

点对点模式/请求-响应模式
Event Bus也支持 点对点消息模式。

消息将被发送到一个地址中，Vert.x将会把消息分发到某个注册在该地址上的处理器。若这个地址上有不止一个注册过的处理器，它将使用 不严格的轮询算法 选择其中一个。

点对点消息传递模式下，可在消息发送的时候指定一个应答处理器（可选）。

当接收者收到消息并且已经被处理时，它可以选择性决定回复该消息，若选择回复则绑定的应答处理器将会被调用。当发送者收到回复消息时，它也可以回复，这个过程可以不断重复。通过这种方式可以允许在两个不同的 Verticle 之间设置一个对话窗口。这种消息模式被称作 请求-响应 模式。

尽力传输
Vert.x会尽它最大努力去传递消息，并且不会主动丢弃消息。这种方式称为 尽力传输(Best-effort delivery)。

但是，当 Event Bus 中的全部或部分发生故障时，则可能会丢失消息。

若您的应用关心丢失的消息，您应该编写具有幂等性的处理器，并且您的发送者可以在恢复后重试。

译者注：RPC通信通常情况下有三种语义：at least once、at most once 和 exactly once。不同语义情况下要考虑的情况不同。

消息类型
Vert.x 默认允许任何基本/简单类型、String 或 Buffer 作为消息发送。不过在 Vert.x 中的通常做法是使用 JSON 格式来发送消息。

JSON 对于 Vert.x 支持的所有语言都是非常容易创建、读取和解析的，因此它已经成为了Vert.x中的通用语(lingua franca)。但是若您不想用 JSON，我们并不强制您使用它。

Event Bus 非常灵活，它支持在 Event Bus 中发送任意对象。您可以通过为您想要发送的对象自定义一个 MessageCodec 来实现。


# 


获取Event Bus
您可以通过下面的代码获取 Event Bus 的引用：

EventBus eb = vertx.eventBus();
对于每一个 Vert.x 实例来说它是单例的。

注册处理器
最简单的注册处理器的方式是使用 consumer 方法，这儿有个例子：

EventBus eb = vertx.eventBus();

eb.consumer("news.uk.sport", message -> {
  System.out.println("I have received a message: " + message.body());
});
当一个消息达到您的处理器，该处理器会以 message 为参数被调用。

调用 consumer 方法会返回一个 MessageConsumer 对象。该对象随后可用于撤销处理器、或将处理器用作流式处理。

您也可以不设置处理器而使用 consumer 方法直接返回一个 MessageConsumer，之后再来设置处理器。如：

EventBus eb = vertx.eventBus();

MessageConsumer<String> consumer = eb.consumer("news.uk.sport");
consumer.handler(message -> {
  System.out.println("I have received a message: " + message.body());
});
在集群模式下的Event Bus上注册处理器时，注册信息会花费一些时间才能传播到集群中的所有节点。

若您希望在完成注册后收到通知，您可以在 MessageConsumer 对象上注册一个 completion handler。

consumer.completionHandler(res -> {
  if (res.succeeded()) {
    System.out.println("The handler registration has reached all nodes");
  } else {
    System.out.println("Registration failed!");
  }
});
注销处理器
您可以通过 unregister() 方法来注销处理器。

若您在集群模式下的 Event Bus 中撤销处理器，则同样会花费一些时间在节点中传播。若您想在完成后收到通知，可以使用unregister(handler) 方法注册处理器：

consumer.unregister(res -> {
  if (res.succeeded()) {
    System.out.println("The handler un-registration has reached all nodes");
  } else {
    System.out.println("Un-registration failed!");
  }
});
发布消息
发布消息很简单，只需使用 publish 方法指定一个地址去发布即可。

eventBus.publish("news.uk.sport", "Yay! Someone kicked a ball");
这个消息将会传递给所有在地址 news.uk.sport 上注册过的处理器。


# 

发送消息
与发布消息的不同之处在于，发送(send)的消息只会传递给在该地址注册的其中一个处理器，这就是点对点模式。Vert.x 使用不严格的轮询算法来选择绑定的处理器。

您可以使用 send 方法来发送消息：

eventBus.send("news.uk.sport", "Yay! Someone kicked a ball");
设置消息头
在 Event Bus 上发送的消息可包含头信息。这可通过在发送或发布时提供的 DeliveryOptions 来指定。例如：

DeliveryOptions options = new DeliveryOptions();
options.addHeader("some-header", "some-value");
eventBus.send("news.uk.sport", "Yay! Someone kicked a ball", options);
消息顺序
Vert.x将按照特定发送者发送消息的顺序来传递消息给特定处理器。

消息对象
您在消息处理器中接收到的对象的类型是 Message。

消息的 body 对应发送或发布的对象。消息的头信息可以通过 headers 方法获取。

应答消息/发送回复
当使用 send 方法发送消息时，Event Bus会尝试将消息传递到注册在Event Bus上的 MessageConsumer中。在某些情况下，发送者需要知道消费者何时收到消息并 处理 了消息。

消费者可以通过调用 reply 方法来应答这个消息。

当这种情况发生时，它会将消息回复给发送者并且在发送者中调用应答处理器来处理回复的消息。

看这个例子会更清楚：

接收者：

MessageConsumer<String> consumer = eventBus.consumer("news.uk.sport");
consumer.handler(message -> {
  System.out.println("I have received a message: " + message.body());
  message.reply("how interesting!");
});
发送者：

eventBus.send("news.uk.sport", "Yay! Someone kicked a ball across a patch of grass", ar -> {
  if (ar.succeeded()) {
    System.out.println("Received reply: " + ar.result().body());
  }
});
在应答的消息体中可以包含有用的信息。

关于 处理中 的含义实际上是由应用程序来定义的。这完全取决于消费者如何执行，Event Bus 对此并不关心。

一些例子：

一个简单地实现了返回当天时间的服务，在应答的消息里会包含当天时间信息。
一个实现了持久化队列的消息消费者，当消息成功持久化到存储时，可以使用true来应答消息，或false表示失败。
一个处理订单的消息消费者也许会用true确认这个订单已经成功处理并且可以从数据库中删除。
带超时的发送
当发送带有应答处理器的消息时，可以在 DeliveryOptions 中指定一个超时时间。如果在这个时间之内没有收到应答，则会以失败为参数调用应答处理器。默认超时是 30 秒。

发送失败
消息发送可能会因为其他原因失败，包括：

没有可用的处理器来接收消息
接收者调用了 fail 方法显式声明失败
发生这些情况时，应答处理器将会以这些失败为参数被调用。

消息编解码器
您可以在 Event Bus 中发送任何对象，只要你为这个对象类型注册一个编解码器 MessageCodec。消息编解码器有一个名称，您需要在发送或发布消息时通过 DeliveryOptions 来指定：

eventBus.registerCodec(myCodec);

DeliveryOptions options = new DeliveryOptions().setCodecName(myCodec.name());

eventBus.send("orders", new MyPOJO(), options);
若您总是希望某个类使用将特定的编解码器，那么您可以为这个类注册默认编解码器。这样您就不需要在每次发送的时候使用 DeliveryOptions 来指定了：

eventBus.registerDefaultCodec(MyPOJO.class, myCodec);

eventBus.send("orders", new MyPOJO());
您可以通过 unregisterCodec 方法注销某个消息编解码器。

消息编解码器的编码和解码不一定使用同一个类型。例如您可以编写一个编解码器来发送 MyPOJO 类的对象，但是当消息发送给处理器后解码成 MyOtherPOJO 对象。



# 集群




Vert.x TCP EventBus Bridge
https://www.jianshu.com/p/92b37ed44f95
