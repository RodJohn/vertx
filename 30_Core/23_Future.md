

# Future

Vert.x 中的 Future 可以用来协调多个异步操作的结果。
它支持并发组合（并行执行多个异步调用）和顺序组合（依次执行异步调用）。



Future<Todo> update(String todoId, Todo newTodo);

@Override
public Future<Todo> update(String todoId, Todo newTodo) {
  return this.getCertain(todoId).compose(old -> { // (1)
    if (old.isPresent()) {
      Todo fnTodo = old.get().merge(newTodo);
      return this.insert(fnTodo)
        .map(r -> r ? fnTodo : null); // (2)
    } else {
      return Future.succeededFuture(); // (3)
    }
  });
}


private void handleUpdateTodo(RoutingContext context) {
  try {
    String todoID = context.request().getParam("todoId");
    final Todo newTodo = new Todo(context.getBodyAsString());
    // handle error
    if (todoID == null) {
      sendError(400, context.response());
      return;
    }
    service.update(todoID, newTodo)
      .setHandler(resultHandler(context, res -> {
        if (res == null)
          notFound(context);
        else {
          final String encoded = Json.encodePrettily(res);
          context.response()
            .putHeader("content-type", "application/json")
            .end(encoded);
        }
      }));
  } catch (DecodeException e) {
    badRequest(context);
  }

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



