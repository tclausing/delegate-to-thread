# delegate-to-thread
Demonstration of wait/notify to make certain methods delegate their execution to designated threads

## Define delegate threads lifecycle

```java
delegateThreadLifecycle.start();

// multithreaded work that calls restricted methods

delegateThreadLifecycle.stop();
```

## Methods annotated with @DelegateToThread to restrict execution

```java
class RestrictedService {

    @DelegateToThread("workA")
    public int workA() {
        // ...
    }

    @DelegateToThread("workB")
    public int workB() {
        // ...
    }
}
```

## Async threads call restricted methods

```java
    @Async
    public CompletableFuture<Integer> workA() {
        int result = restrictedService.workA();
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Integer> workB() {
        int result = restrictedService.workB();
        return CompletableFuture.completedFuture(result);
    }
```

## Threads wait after entering @DelegateToThread methods

Executions of annotated methods in a given lifecycle will occur on dedicated threads, respectively, as specified by the parameter to @DelegateToThread. The calling thread will wait until the delegate thread accepts and performs the task before continuing.

```
2018-10-07 02:39:37.369 --- [ SimpleAsyncTaskExecutor-3] : producer thread [SimpleAsyncTaskExecutor-3] submitting task [workB]
2018-10-07 02:39:37.369 --- [ SimpleAsyncTaskExecutor-4] : producer thread [SimpleAsyncTaskExecutor-4] submitting task [workB]
2018-10-07 02:39:37.369 --- [ SimpleAsyncTaskExecutor-1] : producer thread [SimpleAsyncTaskExecutor-1] submitting task [workA]
2018-10-07 02:39:37.369 --- [http-nio-8080-exec-4-workA] : consumer thread running task [workA] for [SimpleAsyncTaskExecutor-1]
2018-10-07 02:39:37.369 --- [ SimpleAsyncTaskExecutor-2] : producer thread [SimpleAsyncTaskExecutor-2] submitting task [workA]
2018-10-07 02:39:37.369 --- [http-nio-8080-exec-4-workB] : consumer thread running task [workB] for [SimpleAsyncTaskExecutor-3]
2018-10-07 02:39:38.369 --- [http-nio-8080-exec-4-workA] : consumer thread completing task [workA] for [SimpleAsyncTaskExecutor-1]
2018-10-07 02:39:38.369 --- [http-nio-8080-exec-4-workB] : consumer thread completing task [workB] for [SimpleAsyncTaskExecutor-3]
2018-10-07 02:39:38.369 --- [http-nio-8080-exec-4-workA] : consumer thread running task [workA] for [SimpleAsyncTaskExecutor-2]
2018-10-07 02:39:38.369 --- [http-nio-8080-exec-4-workB] : consumer thread running task [workB] for [SimpleAsyncTaskExecutor-4]
2018-10-07 02:39:38.369 --- [ SimpleAsyncTaskExecutor-1] : producer thread [SimpleAsyncTaskExecutor-1] resumed
2018-10-07 02:39:38.369 --- [ SimpleAsyncTaskExecutor-3] : producer thread [SimpleAsyncTaskExecutor-3] resumed
2018-10-07 02:39:39.372 --- [http-nio-8080-exec-4-workA] : consumer thread completing task [workA] for [SimpleAsyncTaskExecutor-2]
2018-10-07 02:39:39.372 --- [http-nio-8080-exec-4-workB] : consumer thread completing task [workB] for [SimpleAsyncTaskExecutor-4]
2018-10-07 02:39:39.372 --- [ SimpleAsyncTaskExecutor-2] : producer thread [SimpleAsyncTaskExecutor-2] resumed
2018-10-07 02:39:39.372 --- [ SimpleAsyncTaskExecutor-4] : producer thread [SimpleAsyncTaskExecutor-4] resumed
```
