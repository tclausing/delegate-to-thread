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

## Threads wait when entering @DelegateToThread methods

In the given lifecycle, threads will wait per the specified thread name given to @DelegateToThread. All executions of the annotated method in a given lifecycle will occur on a single thread, therefor sequentially, before the calling thread(s) resume.

```
2018-09-29 03:49:49.559 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:49.559 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:49.560 DEBUG 6192 --- [TaskExecutor-25] c.e.d.delegatetothread.DelegateThread    : producer thread submitting task
2018-09-29 03:49:49.560 DEBUG 6192 --- [TaskExecutor-27] c.e.d.delegatetothread.DelegateThread    : producer thread submitting task
2018-09-29 03:49:49.560 DEBUG 6192 --- [TaskExecutor-26] c.e.d.delegatetothread.DelegateThread    : producer thread waiting to submit task
2018-09-29 03:49:49.560 DEBUG 6192 --- [TaskExecutor-28] c.e.d.delegatetothread.DelegateThread    : producer thread waiting to submit task
2018-09-29 03:49:49.560 DEBUG 6192 --- [    work-thread] c.e.demo.service.RestrictedService       : task in progress for SimpleAsyncTaskExecutor-25
2018-09-29 03:49:49.560 DEBUG 6192 --- [moreWork-thread] c.e.demo.service.RestrictedService       : task in progress for SimpleAsyncTaskExecutor-27
2018-09-29 03:49:50.560 DEBUG 6192 --- [    work-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-29 03:49:50.560 DEBUG 6192 --- [moreWork-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-29 03:49:50.561 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-29 03:49:50.561 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-29 03:49:50.561 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:50.561 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:50.561 DEBUG 6192 --- [TaskExecutor-25] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-29 03:49:50.561 DEBUG 6192 --- [TaskExecutor-27] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-29 03:49:50.561 DEBUG 6192 --- [TaskExecutor-26] c.e.d.delegatetothread.DelegateThread    : producer thread submitting task
2018-09-29 03:49:50.562 DEBUG 6192 --- [TaskExecutor-28] c.e.d.delegatetothread.DelegateThread    : producer thread submitting task
2018-09-29 03:49:50.562 DEBUG 6192 --- [    work-thread] c.e.demo.service.RestrictedService       : task in progress for SimpleAsyncTaskExecutor-26
2018-09-29 03:49:50.562 DEBUG 6192 --- [moreWork-thread] c.e.demo.service.RestrictedService       : task in progress for SimpleAsyncTaskExecutor-28
2018-09-29 03:49:51.563 DEBUG 6192 --- [moreWork-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-29 03:49:51.563 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-29 03:49:51.563 DEBUG 6192 --- [    work-thread] c.e.d.d.DelegateToThreadAspect           : consumer thread notifying task done
2018-09-29 03:49:51.563 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:51.563 DEBUG 6192 --- [TaskExecutor-28] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-29 03:49:51.563 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread notifying ready for next task
2018-09-29 03:49:51.563 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread waiting for task
2018-09-29 03:49:51.563 DEBUG 6192 --- [TaskExecutor-26] c.e.d.d.DelegateToThreadAspect           : producer thread resumed
2018-09-29 03:49:51.564 DEBUG 6192 --- [    work-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread stopping
2018-09-29 03:49:51.564 DEBUG 6192 --- [moreWork-thread] c.e.d.delegatetothread.DelegateThread    : consumer thread stopping
```
