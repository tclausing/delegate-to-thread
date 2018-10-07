package com.example.demo.delegatetothread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DelegateThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegateThread.class);

    @FunctionalInterface
    public interface RunnableWithThrowable {
        void run() throws Throwable;
    }

    @FunctionalInterface
    public interface CallableWithThrowable<T> {
        T call() throws Throwable;
    }

    private class Task<T> {
        String name;
        CallableWithThrowable<T> callable;
        CompletableFuture<T> future;
        String producer;

        void run() {
            try {
                T result = callable.call();
                LOGGER.debug("consumer thread completing task [{}] for [{}]", name, producer);
                future.complete(result);
            } catch (Throwable e) {
                LOGGER.debug("consumer thread completing task [{}] for [{}] exceptionally: {}", name, producer, e.getClass().getSimpleName());
                future.completeExceptionally(e);
            }
        }
    }

    private ConcurrentLinkedQueue<Task<?>> q = new ConcurrentLinkedQueue<>();

    public DelegateThread(String name) {
        super(name);
        start();
    }

    public CompletableFuture<Void> submitRunnable(String taskName, RunnableWithThrowable runnable) {
        return submitCallable(taskName, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> CompletableFuture<T> submitCallable(String taskName, CallableWithThrowable<T> callable) {
        Task<T> item = new Task<>();
        item.name = taskName;
        item.callable = callable;
        item.future = new CompletableFuture<>();
        item.producer = Thread.currentThread().getName();
        
        LOGGER.debug("producer thread [{}] submitting task [{}]", item.producer, item.name);
        
        synchronized (q) {
            q.add(item);
            q.notify();
        }
        
        return item.future;
    }

    @Override
    public void run() {
        Task<?> task;
        
        while (true) {
            synchronized (q) {
                
                task = q.poll();
    
                if (task == null) {
                    LOGGER.debug("consumer thread waiting for task");
                    try {
                        q.wait();
                    } catch (InterruptedException e) {
                        LOGGER.debug("consumer thread stopping");
                        return; // expected exit point
                    }
                    task = q.poll();
                }
            }

            LOGGER.debug("consumer thread running task [{}] for [{}]", task.name, task.producer);
            task.run();
        }
    }
}