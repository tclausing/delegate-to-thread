package com.example.demo.delegatetothread;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
class DelegateThreadLifecycleImpl implements DelegateThreadLifecycle, DelegateThreadProvider, ApplicationListener<ContextRefreshedEvent> {

	private static final ThreadLocal<Map<String, DelegateThread>> threadMapHolder = new InheritableThreadLocal<>();
	private Supplier<Map<String, DelegateThread>> threadMapFactory;

	@Override
	public void start() {
		// create the thread map eagerly instead of lazily to avoid a synchronized block around a lazy init later on
		threadMapHolder.set(threadMapFactory.get());
	}

	@Override
	public void stop() {
		threadMapHolder.get().values().forEach(DelegateThread::interrupt);
		threadMapHolder.remove();
	}

	@Override
	public DelegateThread getDelegateThreadFor(String name) {
		Map<String, DelegateThread> map = threadMapHolder.get();
		DelegateThread thread = map.get(name);
		if (thread == null) {
			thread = new DelegateThread(name);
			map.put(name, thread);
		}
		return thread;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.threadMapFactory = createThreadMapFactory(event);
	}

	private Supplier<Map<String, DelegateThread>> createThreadMapFactory(ContextRefreshedEvent event) {
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
				.collect(Collectors.toMap(s -> s, DelegateThread::new));
	}
}