


# 创建



## 依赖

    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>vertx-core</artifactId>
      <version>3.4.2</version>
    </dependency>

## 创建实例

默认

    Vertx vertx = Vertx.vertx();

可配置化
    
    Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));
    
集群模式

        
# 特点


事件

    Vert.x 的 API 大部分都是事件驱动的
    我们提供处理器给Vert.x API来处理事件
    当Vert.x有一个事件要传给您的处理器时，它会异步地调用这个处理器
    
    
    

非阻塞

     除了很少的特例（如以 "Sync" 结尾的某些文件系统操作），Vert.x中的所有API都不会阻塞调用线程。
     如果可以立即提供结果，它将立即返回，否则您需要提供一个处理器（Handler）来接收稍后回调的事件。
     因为Vert.x API不会阻塞线程，所以通过Vert.x您可以只使用少量的线程来处理大量的并发。   
    
    在上述所有情况下，当您的线程在等待处理结果时它不能做任何事，此时这些线程并无实际用处。
    这意味着如果您使用阻塞式API处理大量并发，您需要大量线程来防止应用程序逐步停止运转。
    所需的内存（例如它们的栈）和上下文切换都是线程的开销。
    这意味着，阻塞式的方式对于现代应用程序所需要的并发级别来说是难于扩展的。
    
    


# Reactor 模式

    当有事件时 Vert.x 会将事件传给处理器来处理。
    在多数情况下，Vert.x使用被称为 Event Loop 的线程来调用您的处理器。
    由于Vert.x或应用程序的代码块中没有阻塞，Event Loop 可以在事件到达时快速地分发到不同的处理器中。
    由于没有阻塞，Event Loop 可在短时间内分发大量的事件。例如，一个单独的 Event Loop 可以非常迅速地处理数千个 HTTP 请求。
    
    
    在一个标准的反应器实现中，有 一个独立的 Event Loop 会循环执行，处理所有到达的事件并传递给处理器处理。
    单一线程的问题在于它在任意时刻只能运行在一个核上。如果您希望单线程反应器应用（如您的 Node.js 应用）扩展到多核服务器上，则需要启动并且管理多个不同的进程。
    Vert.x的工作方式有所不同。每个 Vertx 实例维护的是 多个Event Loop 线程。默认情况下，我们会根据机器上可用的核数量来设置 Event Loop 的数量，您亦可自行设置。
    这意味着 Vertx 进程能够在您的服务器上扩展，与 Node.js 不同。
    我们将这种模式称为 Multi-Reactor 模式（多反应器模式），区别于单线程的 Reactor 模式（反应器模式）。
    

# 不要阻塞Event Loop

    尽管我们已经知道，Vert.x 的 API 都是非阻塞式的并且不会阻塞 Event Loop，但是这并不能帮您避免在您自己的处理器中阻塞 Event Loop 的情况发生。
    如果这样做，该 Event Loop 在被阻塞时就不能做任何事情。如果您阻塞了 Vertx 实例中的所有 Event Loop，那么您的应用就会完全停止！
    如果上述任何一种情况停止了 Event Loop 并占用了 显著执行时间



# 运行阻塞式代码

如之前讨论，您不能在 Event Loop 中直接调用阻塞式操作，因为这样做会阻止 Event Loop 执行其他有用的任务。那您该怎么做？

可以通过调用 executeBlocking 方法来指定阻塞式代码的执行以及阻塞式代码执行后处理结果的异步回调。


    vertx.executeBlocking(future -> {
      // 调用一些需要耗费显著执行时间返回结果的阻塞式API
      String result = someAPI.blockingMethod("hello");
      future.complete(result);
    }, res -> {
      System.out.println("The result is: " + res.result());
    });
    
    
    
默认情况下，如果 executeBlocking 在同一个上下文环境中（如：同一个 Verticle 实例）被调用了多次，那么这些不同的 executeBlocking 代码块会 顺序执行（一个接一个）。



##  Worker Pool 

若您不需要关心您调用 executeBlocking 的顺序，可以将 ordered 参数的值设为 false。这样任何 executeBlocking 都会在 Worker Pool 中并行执行。

另外一种运行阻塞式代码的方法是使用 Worker Verticle。

一个 Worker Verticle 始终会使用 Worker Pool 中的某个线程来执行。


# Future

Vert.x 中的 Future 可以用来协调多个异步操作的结果。
它支持并发组合（并行执行多个异步调用）和顺序组合（依次执行异步调用）。


## 并发合并

all

CompositeFuture.all 方法接受多个 Future 对象作为参数（最多6个，或者传入 List）。
当所有的 Future 都成功完成，该方法将返回一个 成功的 Future；
当任一个 Future 执行失败，则返回一个 失败的 Future：
    
    
    Future<HttpServer> httpServerFuture = Future.future();
    httpServer.listen(httpServerFuture.completer());
    
    Future<NetServer> netServerFuture = Future.future();
    netServer.listen(netServerFuture.completer());
    
    CompositeFuture.all(httpServerFuture, netServerFuture).setHandler(ar -> {
      if (ar.succeeded()) {
        // 所有服务器启动完成
      } else {
        // 有一个服务器启动失败
      }
    });    
    

所有被合并的 Future 中的操作同时运行。当组合的处理操作完成时，该方法返回的 Future 上绑定的处理器（Handler）会被调用。
当一个操作失败（其中的某一个 Future 的状态被标记成失败），则返回的 Future 会被标记为失败。
当所有的操作都成功时，返回的 Future 将会成功完成。  


any


any 方法的合并会等待第一个成功执行的Future。
CompositeFuture.any 方法接受多个 Future 作为参数（最多6个，或传入 List）。
当任意一个 Future 成功得到结果，则该 Future 成功；当所有的 Future 都执行失败，则该 Future 失败。 


compose


    compose 方法作用于顺序组合 Future。
    
    FileSystem fs = vertx.fileSystem();
    Future<Void> startFuture = Future.future();
    
    Future<Void> fut1 = Future.future();
    fs.createFile("/foo", fut1.completer());
    
    fut1.compose(v -> {
      // fut1中文件创建完成后执行
      Future<Void> fut2 = Future.future();
      fs.writeFile("/foo", Buffer.buffer(), fut2.completer());
      return fut2;
    }).compose(v -> {
      // fut2文件写入完成后执行
      fs.move("/foo", "/bar", startFuture.completer());
    },
      // 如果任何一步失败，将startFuture标记成failed
      startFuture); 
    
    
这里例子中，有三个操作被串起来了：

一个文件被创建（fut1）
一些东西被写入到文件（fut2）
文件被移走（startFuture）
如果这三个步骤全部成功，则最终的 Future（startFuture）会是成功的；其中任何一步失败，则最终 Future 就是失败的。

例子中使用了：

compose(mapper)：当前 Future 完成时，执行相关代码，并返回 Future。当返回的 Future 完成时，组合完成。
compose(handler, next)：当前 Future 完成时，执行相关代码，并完成下一个 Future 的处理。
对于第二个例子，处理器需要完成 next future，以此来汇报处理成功或者失败。

您可以使用 completer 方法来串起一个带操作结果的或失败的 Future ，它可使您避免用传统方式编写代码：如果成功则完成 Future，否则就标记为失败。（译者注：3.4.0 以后不需要再使用 completer 方法）



