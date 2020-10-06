# DoobieLeakTest

Test case to replicate a connection leak in Doobie 0.9.

The leak appears to occur when using `.stream.transact` in a transaction that is cancelled mid-flight (i.e. due to HTTP4s request timeout)

To replicate

`sbt run`

After several seconds, the Hikari leak detector should report a connection leak. (It'll take at least 5 seconds).

```
15:32:10.886 [HikariPool-1 housekeeper] WARN  com.zaxxer.hikari.pool.ProxyLeakTask {} - Connection leak detection triggered for conn0: url=jdbc:h2:mem:test user=SA on thread pool-13-thread-1, stack trace follows
java.lang.Exception: Apparent connection leak detected
	at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:128)
	at doobie.util.transactor$Transactor$fromDataSource$FromDataSourceUnapplied.$anonfun$apply$14(transactor.scala:280)
	at cats.effect.internals.IORunLoop$.cats$effect$internals$IORunLoop$$loop(IORunLoop.scala:87)
	at cats.effect.internals.IORunLoop$.startCancelable(IORunLoop.scala:41)
	at cats.effect.internals.IOBracket$BracketStart.run(IOBracket.scala:90)
	at cats.effect.internals.Trampoline.cats$effect$internals$Trampoline$$immediateLoop(Trampoline.scala:67)
	at cats.effect.internals.Trampoline.startLoop(Trampoline.scala:35)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.super$startLoop(TrampolineEC.scala:90)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.$anonfun$startLoop$1(TrampolineEC.scala:90)
	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)
	at scala.concurrent.BlockContext$.withBlockContext(BlockContext.scala:85)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.startLoop(TrampolineEC.scala:90)
	at cats.effect.internals.Trampoline.execute(Trampoline.scala:43)
	at cats.effect.internals.TrampolineEC.execute(TrampolineEC.scala:42)
	at cats.effect.internals.IOBracket$BracketStart.apply(IOBracket.scala:70)
	at cats.effect.internals.IOBracket$BracketStart.apply(IOBracket.scala:50)
	at cats.effect.internals.IORunLoop$.cats$effect$internals$IORunLoop$$loop(IORunLoop.scala:141)
	at cats.effect.internals.IORunLoop$RestartCallback.signal(IORunLoop.scala:366)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:387)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:330)
	at cats.effect.internals.IOShift$Tick.run(IOShift.scala:36)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1135)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:844)
```
  
Using JConsole, you can browser to `com.zaxxer.hikari.pool.HikariPool` attributes and verify that `ActiveConnections` is set to `1` due to the leaked connection not being closed.

# Notes

Tested using Azul JDK 10 on a Macbook Pro (2.9 ghz).

This is likely a race condition - it's occuring in production randomly, but not consistently. I stripped it back to the simplest test case I could, 
and this example is able to replicate the leak reliably for me, on my machine. However since it's possibly a race condition the timing may act differently 
on different machines. If you can't replicate, try fiddling with the 10ms delay.



