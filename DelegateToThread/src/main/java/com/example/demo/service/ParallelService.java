package com.example.demo.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ParallelService {

	@Autowired
	private RestrictedService restrictedService;

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
}