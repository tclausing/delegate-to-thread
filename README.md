# delegate-to-thread
Demonstration of wait/notify to make certain methods delegate their execution to designated threads

## Define delegate thread lifecycle

```java
delegateThreadLifecycle.start();
// multithreaded work which calls restricted methods
delegateThreadLifecycle.stop();
```

## Methods annotated with @DelegateToThread

```java
class RestrictedService {

	@DelegateToThread("work-thread")
	public int work(String callingThreadId) {
		// ...
	}

	@DelegateToThread("moreWork-thread")
	public int moreWork(String callingThreadId) {
		// ...
	}
}
```

## Async threads call restricted methods

```java
	@Async
	public CompletableFuture<Integer> work() {
		int result = restrictedService.work(Thread.currentThread().getName());
		return CompletableFuture.completedFuture(result);
	}

	@Async
	public CompletableFuture<Integer> moreWork() {
		int result = restrictedService.moreWork(Thread.currentThread().getName());
		return CompletableFuture.completedFuture(result);
	}
```

## Threads wait after entering @DelegateToThread methods

Executions of annotated methods in a given lifecycle will occur on dedicated threads, respectively, as specified by the parameter to @DelegateToThread. The calling thread will wait until the delegate thread accepts and performs the task before continuing.

```
2018-09-30 16:53:01.220 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:01.220 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:01.220 DEBUG 11320 --- [TaskExecutor-86] c.e.d.delegatetothread.DelegateThread    : producer thread submitting workA task
2018-09-30 16:53:01.220 DEBUG 11320 --- [TaskExecutor-88] c.e.d.delegatetothread.DelegateThread    : producer thread submitting workB task
2018-09-30 16:53:01.221 DEBUG 11320 --- [TaskExecutor-85] c.e.d.delegatetothread.DelegateThread    : producer thread waiting to submit workA task
2018-09-30 16:53:01.221 DEBUG 11320 --- [TaskExecutor-87] c.e.d.delegatetothread.DelegateThread    : producer thread waiting to submit workB task
2018-09-30 16:53:01.221 DEBUG 11320 --- [   workA-thread] c.e.demo.service.RestrictedService       : workA task in progress for SimpleAsyncTaskExecutor-86
2018-09-30 16:53:01.221 DEBUG 11320 --- [   workB-thread] c.e.demo.service.RestrictedService       : workB task in progress for SimpleAsyncTaskExecutor-88
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workA-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workB-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:02.221 DEBUG 11320 --- [TaskExecutor-86] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-30 16:53:02.221 DEBUG 11320 --- [TaskExecutor-88] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-30 16:53:02.221 DEBUG 11320 --- [TaskExecutor-85] c.e.d.delegatetothread.DelegateThread    : producer thread submitting workA task
2018-09-30 16:53:02.221 DEBUG 11320 --- [TaskExecutor-87] c.e.d.delegatetothread.DelegateThread    : producer thread submitting workB task
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workA-thread] c.e.demo.service.RestrictedService       : workA task in progress for SimpleAsyncTaskExecutor-85
2018-09-30 16:53:02.221 DEBUG 11320 --- [   workB-thread] c.e.demo.service.RestrictedService       : workB task in progress for SimpleAsyncTaskExecutor-87
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workA-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workB-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:03.222 DEBUG 11320 --- [TaskExecutor-85] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-30 16:53:03.222 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-30 16:53:03.222 DEBUG 11320 --- [TaskExecutor-87] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-30 16:53:03.224 DEBUG 11320 --- [   workA-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread stopping
2018-09-30 16:53:03.224 DEBUG 11320 --- [   workB-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread stopping
```
