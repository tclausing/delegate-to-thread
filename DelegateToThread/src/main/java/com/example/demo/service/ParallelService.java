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
    public CompletableFuture<Integer> workA() {
        int result = restrictedService.workA(Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Integer> workB() {
        int result = restrictedService.workB(Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }
}