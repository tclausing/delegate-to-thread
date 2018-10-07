package com.example.demo.delegatetothread;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
class DelegateToThreadAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegateToThreadAspect.class);

    @Autowired
    private DelegateExecutorProvider delegateExecutorProvider;

    @Around("@annotation(com.example.demo.delegatetothread.DelegateToThread)")
    public Object around(ProceedingJoinPoint joinPoint) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String threadName = method.getAnnotation(DelegateToThread.class).value();
        String producer = Thread.currentThread().getName();
        Executor executor = delegateExecutorProvider.getExecutorFor(threadName);
        CompletableFuture<Object> future = new CompletableFuture<>();

        LOGGER.debug("producer thread [{}] submitting task [{}]", producer, method.getName());
        
        executor.execute(() -> {
            LOGGER.debug("consumer thread running task [{}] for [{}]", method.getName(), producer);
            try {
                Object result = joinPoint.proceed();
                LOGGER.debug("consumer thread completing task [{}] for [{}]", method.getName(), producer);
                future.complete(result);
            } catch (Throwable e) {
                LOGGER.debug("consumer thread completing task [{}] for [{}] exceptionally: {}", method.getName(), producer, e.getClass().getSimpleName());
                future.completeExceptionally(e);
            }
        });

        Object result = get(future);
        LOGGER.debug("producer thread [{}] resumed", Thread.currentThread().getName());
        return result;
    }

    public Object get(CompletableFuture<Object> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            Rethrower.rethrow(e.getCause());
            throw new IllegalStateException("Should never get here", e);
        } catch (InterruptedException e) {
            throw new DelegateThreadRuntimeException(e);
        }
    }

    
    // from org.springframework.transaction.aspectj.AbstractTransactionAspect:
    /**
     * Ugly but safe workaround: We need to be able to propagate checked exceptions,
     * despite AspectJ around advice supporting specifically declared exceptions only.
     */
    private static class Rethrower {

        public static void rethrow(final Throwable exception) {
            class CheckedExceptionRethrower<T extends Throwable> {
                @SuppressWarnings("unchecked")
                private void rethrow(Throwable exception) throws T {
                    throw (T) exception;
                }
            }
            new CheckedExceptionRethrower<RuntimeException>().rethrow(exception);
        }
    }
}