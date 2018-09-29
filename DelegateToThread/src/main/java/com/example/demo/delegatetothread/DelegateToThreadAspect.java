package com.example.demo.delegatetothread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
	private DelegateThreadProvider delegateThreadProvider;

	@Around("@annotation(com.example.demo.delegatetothread.DelegateToThread)")
	public Object around(ProceedingJoinPoint joinPoint) {

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String threadName = signature.getMethod().getAnnotation(DelegateToThread.class).value();
		DelegateThread delegateThread = delegateThreadProvider.getDelegateThreadFor(threadName);
		CompletableFuture<Object> future = new CompletableFuture<>();

		delegateThread.submit(() -> {
			Object result;
			try {
				result = joinPoint.proceed();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}

			LOGGER.debug("consumer thread notifying task done");
			future.complete(result);
		});

		Object result;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		LOGGER.debug("producer thread resumed");
		return result;
	}
}