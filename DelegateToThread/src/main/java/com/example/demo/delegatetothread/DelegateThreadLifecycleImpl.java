package com.example.demo.delegatetothread;


import static com.example.demo.functional.Unchecked.unchecked;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
class DelegateThreadLifecycleImpl implements DelegateThreadLifecycle, DelegateThreadProvider {

    private static final ThreadLocal<Map<String, DelegateThread>> THREAD_MAP_HOLDER = new InheritableThreadLocal<>();

    private Supplier<Map<String, DelegateThread>> threadMapFactory;

    @Override
    public void start() {
        // create the thread map eagerly instead of lazily to avoid a synchronized block around a lazy init later on
        THREAD_MAP_HOLDER.set(threadMapFactory.get());
    }

    @Override
    public void stop() {
        THREAD_MAP_HOLDER.get().values().forEach(DelegateThread::interrupt);
        THREAD_MAP_HOLDER.remove();
    }

    @Override
    public DelegateThread getDelegateThreadFor(String name) {
        Map<String, DelegateThread> map = THREAD_MAP_HOLDER.get();
        return map.get(name);
    }

    @Autowired
    public void setAppContext(ApplicationContext ctx) {
        this.threadMapFactory = createThreadMapFactory(ctx);
    }

    private Supplier<Map<String, DelegateThread>> createThreadMapFactory(ApplicationContext applicationContext) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
        Set<String> threadNames = Stream.of(registry.getBeanDefinitionNames())
                .map(registry::getBeanDefinition)
                .map(BeanDefinition::getBeanClassName)
                .filter(Objects::nonNull)
                .map(unchecked(Class::forName))
                .flatMap(cls -> Stream.of(cls.getMethods()))
                .map(m -> m.getAnnotation(DelegateToThread.class))
                .filter(Objects::nonNull)
                .map(DelegateToThread::value).distinct().collect(Collectors.toSet());

        return () -> threadNames.stream().collect(Collectors.toMap(s -> s, DelegateThread::new));
    }
}