

# 概述 

Vert.x的执行单元叫verticle。即程序的入口

verticle分两种，一种是基于EventLoop的适合I/O密集型的，还有一种是适合CPU密集型的worker verticle。而verticle之间相互通信只能通过Eventbus，可以支持point to point 的通信，也可以支持publish & subscribe通信方式。


Vert.x 通过开箱即用的方式提供了一个简单便捷的、可扩展的、类似 Actor Model 的部署和并发模型机制。
您可以用此模型机制来保管您自己的代码组件。

这个模型是可选的，如果您不想这样做，Vert.x 不会强迫您用这种方式创建您的应用程序。

这个模型不能说是严格的 Actor 模式的实现，但它确实有相似之处，特别是在并发、扩展性和部署等方面。

要使用该模型，您需要将您的代码组织成一系列的 Verticle。

Verticle 是由 Vert.x 部署和运行的代码块。默认情况一个 Vert.x 实例维护了N（默认情况下N = CPU核数 x 2）个 Event Loop 线程。Verticle 实例可使用任意 Vert.x 支持的编程语言编写，而且一个简单的应用程序也可以包含多种语言编写的 Verticle。

您可以将 Verticle 想成 Actor Model 中的 Actor。

一个应用程序通常是由在同一个 Vert.x 实例中同时运行的许多 Verticle 实例组合而成。不同的 Verticle 实例通过向 Event Bus 上发送消息来相互通信。


# Actor

基于Actor的消息传递


# 3 编写 Verticle

规则

    Verticle 的实现类必须实现 Verticle 接口。
    从抽象类 AbstractVerticle 继承更简单。

例子

    public class MyVerticle extends AbstractVerticle {
    
      // Called when verticle is deployed
      // Verticle部署时调用
      public void start() {
      }
    
      // Optional - called when verticle is undeployed
      // 可选 - Verticle撤销时调用
      public void stop() {
      }
    
    }
    
方法
    
    当 Vert.x 部署 Verticle 时，它的 start 方法将被调用，这个方法执行完成后 Verticle 就变成已启动状态。



# Verticle 异步启动和停止

有些时候您的 Verticle 启动会耗费一些时间，您想要在这个过程做一些事，并且您做的这些事并不想等到Verticle部署完成过后再发生。如：您想在 start 方法中部署其他的 Verticle。

您不能在您的 start 方法中阻塞等待其他的 Verticle 部署完成，这样做会破坏 黄金法则。

所以您要怎么做？

您可以实现 异步版本 的 start 方法来做这个事。这个版本的方法会以一个 Future 作参数被调用。方法执行完时，Verticle 实例并没有部署好（状态不是 deployed）。
稍后，您完成了所有您需要做的事（如：启动其他Verticle），您就可以调用 Future 的 complete（或 fail ）方法来标记启动完成或失败了。


public class MyVerticle extends AbstractVerticle {

  public void start(Future<Void> startFuture) {
    // 现在部署其他的一些verticle
    vertx.deployVerticle("com.foo.OtherVerticle", res -> {
      if (res.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(res.cause());
      }
    });
  }
}

# 5 Verticle 种类


  这儿有三种不同类型的 Verticle：
  
  Stardand Verticle：这是最常用的一类 Verticle —— 它们永远运行在 Event Loop 线程上。稍后的章节我们会讨论更多。
  Worker Verticle：这类 Verticle 会运行在 Worker Pool 中的线程上。一个实例绝对不会被多个线程同时执行。
  Multi-Threaded Worker Verticle：这类 Verticle 也会运行在 Worker Pool 中的线程上。一个实例可以由多个线程同时执行（译者注：因此需要开发者自己确保线程安全）。
  
  
## Standard Verticle


   当 Standard Verticle 被创建时，它会被分派给一个 Event Loop 线程，并在这个 Event Loop 中执行它的 start 方法。
   当您在一个 Event Loop 上调用了 Core API 中的方法并传入了处理器时，
   Vert.x 将保证用与调用该方法时相同的 Event Loop 来执行这些处理器。
   
   这意味着我们可以保证您的 Verticle 实例中 所有的代码都是在相同Event Loop中执行（只要您不创建自己的线程并调用它！）
   同样意味着您可以将您的应用中的所有代码用单线程方式编写，让 Vert.x 去考虑线程和扩展问题。
   您不用再考虑 synchronized 和 volatile 的问题，也可以避免传统的多线程应用经常会遇到的竞态条件和死锁的问题。

## Worker Verticle


   Worker Verticle 和 Standard Verticle 很像，但它并不是由一个 Event Loop 来执行，而是由Vert.x中的 Worker Pool 中的线程执行。
   
   Worker Verticle 被设计来调用阻塞式代码，它不会阻塞任何 Event Loop。
   
   如果您不想使用 Worker Verticle 来运行阻塞式代码，您还可以在一个Event Loop中直接使用 内联阻塞式代码。
   
   若您想要将 Verticle 部署成一个 Worker Verticle，您可以通过 setWorker 方法来设置：
   
   DeploymentOptions options = new DeploymentOptions().setWorker(true);
   vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", options);
   Worker Verticle 实例绝对不会在 Vert.x 中被多个线程同时执行，但它可以在不同时间由不同线程执行。
   
## Multi-threaded Worker Verticle

   一个 Multi-threaded Worker Verticle 近似于普通的 Worker Verticle，但是它可以由不同的线程同时执行。
   
   警告：Multi-threaded Worker Verticle 是一个高级功能，大部分应用程序不会需要它。由于这些 Verticle 是并发的，您必须小心地使用标准的Java多线程技术来保持 Verticle 的状态一致性。
   
      
# 7 部署

## 编程方式部署Verticle

方法

    vertx.deployVerticle()

调试

    main方法    
    


## Verticle名称到Factory的映射规则
   当使用名称部署Verticle时，会通过名称来选择一个用于实例化 Verticle 的 Verticle Factory。
   
   Verticle 名称可以有一个前缀 —— 使用字符串紧跟着一个冒号，它用于查找存在的Factory，参考例子。
   
   js:foo.js // 使用JavaScript的Factory
   groovy:com.mycompany.SomeGroovyCompiledVerticle // 用Groovy的Factory
   service:com.mycompany:myorderservice // 用Service的Factory
   如果不指定前缀，Vert.x将根据提供名字后缀来查找对应Factory，如：
   
   foo.js // 将使用JavaScript的Factory
   SomeScript.groovy // 将使用Groovy的Factory
   若前缀后缀都没指定，Vert.x将假定这个名字是一个Java 全限定类名（FQCN）然后尝试实例化它。
   
   如何定位Verticle Factory？
   大部分Verticle Factory会从 classpath 中加载，并在 Vert.x 启动时注册。
   
   您同样可以使用编程的方式去注册或注销Verticle Factory：通过 registerVerticleFactory 方法和 unregisterVerticleFactory 方法。


## 等待部署完成

   Verticle的部署是异步方式，可能在 deploy 方法调用返回后一段时间才会完成部署。
   
   如果您想要在部署完成时被通知则可以指定一个完成处理器：
   
   vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", res -> {
     if (res.succeeded()) {
       System.out.println("Deployment id is: " + res.result());
     } else {
       System.out.println("Deployment failed!");
     }
   });
   如果部署成功，这个完成处理器的结果中将会包含部署ID的字符串。这个部署 ID可以在之后您想要撤销它时使用。   
   


## 撤销Verticle

   我们可以通过 undeploy 方法来撤销部署好的 Verticle。
   
   撤销操作也是异步的，因此若您想要在撤销完成过后收到通知则可以指定另一个完成处理器：
   
   vertx.undeploy(deploymentID, res -> {
     if (res.succeeded()) {
       System.out.println("Undeployed ok");
     } else {
       System.out.println("Undeploy failed!");
     }
   });
   
   
## 设置 Verticle 实例数

   当使用名称部署一个 Verticle 时，您可以指定您想要部署的 Verticle 实例的数量。
   
   DeploymentOptions options = new DeploymentOptions().setInstances(16);
   vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", options);
   这个功能对于跨多核扩展时很有用。例如，您有一个实现了Web服务器的Verticle需要部署在多核的机器上，您可以部署多个实例来利用所有的核。
   

## 向 Verticle 传入配置

   可在部署时传给 Verticle 一个 JSON 格式的配置
   
   JsonObject config = new JsonObject().put("name", "tim").put("directory", "/blah");
   DeploymentOptions options = new DeploymentOptions().setConfig(config);
   vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", options);
   传入之后，这个配置可以通过 Context 对象或使用 config 方法访问。
   
   这个配置会以 JSON 对象（JsonObject）的形式返回，因此您可以用下边代码读取数据：
   
   System.out.println("Configuration: " + config().getString("name"));   


## Verticle 隔离组


   默认情况，当Vert.x部署Verticle时它会调用当前类加载器来加载类，而不会创建一个新的。大多数情况下，这是最简单、最清晰和最干净。
   
   但是在某些情况下，您可能需要部署一个Verticle，它包含的类要与应用程序中其他类隔离开来。比如您想要在一个Vert.x实例中部署两个同名不同版本的Verticle，或者不同的Verticle使用了同一个jar包的不同版本。
   
   当使用隔离组时，您需要用 setIsolatedClassed 方法来提供一个您想隔离的类名列表。列表项可以是一个Java 限定类全名，如 com.mycompany.myproject.engine.MyClass；也可以是包含通配符的可匹配某个包或子包的任何类，例如 com.mycompany.myproject.* 将会匹配所有 com.mycompany.myproject 包或任意子包中的任意类名。
   
   请注意仅仅只有匹配的类会被隔离，其他任意类会被当前类加载器加载。
   
   若您想要加载的类和资源不存在于主类路径（main classpath），您可使用 setExtraClasspath 方法将额外的类路径添加到这里。
   
   警告：谨慎使用此功能，类加载器可能会导致您的应用难于调试，变得一团乱麻（can of worms）。
   
   以下是使用隔离组隔离 Verticle 的部署例子：
   
   DeploymentOptions options = new DeploymentOptions().setIsolationGroup("mygroup");
   options.setIsolatedClasses(Arrays.asList("com.mycompany.myverticle.*",
                      "com.mycompany.somepkg.SomeClass", "org.somelibrary.*"));
   vertx.deployVerticle("com.mycompany.myverticle.VerticleClass", options);
      
 
## 高可用性

   Verticle可以启用高可用方式（HA）部署。在这种方式下，当其中一个部署在 Vert.x 实例中的 Verticle 突然挂掉，这个 Verticle 可以在集群环境中的另一个 Vert.x 实例中重新部署。
   
   若要启用高可用方式运行一个 Verticle，仅需要追加 -ha 参数：
   
   vertx run my-verticle.js -ha
   当启用高可用方式时，不需要追加 -cluster 参数。
   
   关于高可用的功能和配置的更多细节可参考 高可用和故障转移 章节。
   
    
   
## 从命令行运行Verticle
   您可以在 Maven 或 Gradle 项目中以正常方式添加 Vert.x Core 为依赖，在项目中直接使用 Vert.x。
   
   但是，您也可以从命令行直接运行 Vert.x 的 Verticle。
   
   为此，您需要下载并安装 Vert.x 的发行版，并且将安装的 bin 目录添加到您的 PATH 环境变量中，还要确保您的 PATH 中设置了Java 8的JDK环境。
   
   请注意：JDK需要支持Java代码的运行时编译（on the fly compilation）。
   
   现在您可以使用 vertx run 命令运行Verticle了，这儿是一些例子：
   
   # 运行JavaScript的Verticle
   vertx run my_verticle.js
   
   # 运行Ruby的Verticle
   vertx run a_n_other_verticle.rb
   
   # 使用集群模式运行Groovy的Verticle
   vertx run FooVerticle.groovy -cluster
   您甚至可以不必编译 Java 源代码，直接运行它：
   
   vertx run SomeJavaSourceFile.java
   Vert.x 将在运行它之前对 Java 源代码文件执行运行时编译，这对于快速原型制作和演示很有用。不需要设置 Maven 或 Gradle 就能跑起来！
   
   欲了解有关在命令行执行 vertx 可用的各种选项完整信息，可以直接在命令行键入 vertx 查看帮助。
   

# 8 执行

https://vertxchina.github.io/vertx-translation-chinese/start/SimpleStart.html   
   
   
# 8 Context 对象
  当 Vert.x 传递一个事件给处理器或者调用 Verticle 的 start 或 stop 方法时，它会关联一个 Context 对象来执行。通常来说这个 Context 会是一个 Event Loop Context，它绑定到了一个特定的 Event Loop 线程上。所以在该 Context 上执行的操作总是在同一个 Event Loop 线程中。对于运行内联的阻塞代码的 Worker Verticle 来说，会关联一个 Worker Context，并且所有的操作运都会运行在 Worker 线程池的线程上。
  
  译者注：每个 Verticle 在部署的时候都会被分配一个 Context（根据配置不同，可以是Event Loop Context 或者 Worker Context），之后此 Verticle 上所有的普通代码都会在此 Context 上执行（即对应的 Event Loop 或Worker 线程）。一个 Context 对应一个 Event Loop 线程（或 Worker 线程），但一个 Event Loop 可能对应多个 Context。
  
  您可以通过 getOrCreateContext 方法获取 Context 实例：
  
  Context context = vertx.getOrCreateContext();
  若已经有一个 Context 和当前线程关联，那么它直接重用这个 Context 对象，如果没有则创建一个新的。您可以检查获取的 Context 的类型：
  
  Context context = vertx.getOrCreateContext();
  if (context.isEventLoopContext()) {
    System.out.println("Context attached to Event Loop");
  } else if (context.isWorkerContext()) {
    System.out.println("Context attached to Worker Thread");
  } else if (context.isMultiThreadedWorkerContext()) {
    System.out.println("Context attached to Worker Thread - multi threaded worker");
  } else if (! Context.isOnVertxThread()) {
    System.out.println("Context not attached to a thread managed by vert.x");
  }
  当您获取了这个 Context 对象，您就可以在 Context 中异步执行代码了。换句话说，您提交的任务将会在同一个 Context 中运行：
  
  vertx.getOrCreateContext().runOnContext(v -> {
    System.out.println("This will be executed asynchronously in the same context");
  });
  当在同一个 Context 中运行了多个处理函数时，可能需要在它们之间共享数据。 Context 对象提供了存储和读取共享数据的方法。举例来说，它允许您将数据传递到 runOnContext 方法运行的某些操作中：
  
  final Context context = vertx.getOrCreateContext();
  context.put("data", "hello");
  context.runOnContext((v) -> {
    String hello = context.get("data");
  });
  您还可以通过 config 方法访问 Verticle 的配置信息。查看 向 Verticle 传入配置 章节了解更多配置信息
  
  
  
# 9 定时器

执行周期性/延迟性操作
在 Vert.x 中，想要延迟之后执行或定期执行操作很常见。

在 Standard Verticle 中您不能直接让线程休眠以引入延迟，因为它会阻塞 Event Loop 线程。取而代之是使用 Vert.x 定时器。定时器可以是一次性或周期性的，两者我们都会讨论到。

一次性计时器
一次性计时器会在一定延迟后调用一个 Event Handler，以毫秒为单位计时。

您可以通过 setTimer 方法传递延迟时间和一个处理器来设置计时器的触发。

long timerID = vertx.setTimer(1000, id -> {
  System.out.println("And one second later this is printed");
});

System.out.println("First this is printed");
返回值是一个唯一的计时器id，该id可用于之后取消该计时器，这个计时器id会传入给处理器。

周期性计时器
您同样可以使用 setPeriodic 方法设置一个周期性触发的计时器。第一次触发之前同样会有一段设置的延时时间。

setPeriodic 方法的返回值也是一个唯一的计时器id，若之后该计时器需要取消则使用该id。传给处理器的参数也是这个唯一的计时器id。

请记住这个计时器将会定期触发。如果您的定时任务会花费大量的时间，则您的计时器事件可能会连续执行甚至发生更坏的情况：重叠。这种情况，您应考虑使用 setTimer 方法，当任务执行完成时设置下一个计时器。

long timerID = vertx.setPeriodic(1000, id -> {
  System.out.println("And every second this is printed");
});

System.out.println("First this is printed");
取消计时器
指定一个计时器id并调用 cancelTimer 方法来取消一个周期性计时器。如：

vertx.cancelTimer(timerID);
Verticle 中自动清除定时器
如果您在 Verticle 中创建了计时器，当这个 Verticle 被撤销时这个计时器会被自动关闭。

Verticle Worker Pool
Verticle 使用 Vert.x 中的 Worker Pool 来执行阻塞式行为，例如 executeBlocking 或 Worker Verticle。

可以在部署配置项中指定不同的Worker 线程池：

vertx.deployVerticle("the-verticle", new DeploymentOptions().setWorkerPoolName("the-specific-pool"));
Ev

           