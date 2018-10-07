package com.example.demo.delegatetothread;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
class DelegateThreadLifecycleImpl implements DelegateThreadLifecycle, DelegateExecutorProvider, ApplicationListener<ContextRefreshedEvent> {

    private static final ThreadLocal<Map<String, ExecutorService>> THREAD_MAP_HOLDER = new InheritableThreadLocal<>();
    private Supplier<Map<String, ExecutorService>> threadMapFactory;

    @Override
    public void start() {
        // create the thread map eagerly instead of lazily to avoid a synchronized block around a lazy init later on
        THREAD_MAP_HOLDER.set(threadMapFactory.get());
    }

    @Override
    public void stop() {
        THREAD_MAP_HOLDER.get().values().forEach(ExecutorService::shutdownNow);
        THREAD_MAP_HOLDER.remove();
    }

    @Override
    public Executor getExecutorFor(String name) {
        Map<String, ExecutorService> map = THREAD_MAP_HOLDER.get();
        return map.get(name);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.threadMapFactory = createThreadMapFactory(event);
    }

    private Supplier<Map<String, ExecutorService>> createThreadMapFactory(ContextRefreshedEvent event) {
        Set<String> threadNames = Stream.of(event.getApplicationContext().getBeanDefinitionNames())
                .map(event.getApplicationContext()::getBean)
                .map(AopUtils::getTargetClass) // instead of Object::getClass in case it's a proxy
                .flatMap(cls -> Stream.of(cls.getDeclaredMethods()))
                .map(m -> m.getAnnotation(DelegateToThread.class))
                .filter(Objects::nonNull)
                .map(DelegateToThread::value)
                .distinct()
                .collect(Collectors.toSet());
        
        return () -> threadNames.stream()
                .collect(Collectors.toMap(s -> s, DelegateThreadLifecycleImpl::executor));
    }
    
    private static ExecutorService executor(String name) {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread currentThread = Thread.currentThread();
                return new Thread(currentThread.getThreadGroup(), r, currentThread.getName() + "-" + name);
            }
        });
    }
}