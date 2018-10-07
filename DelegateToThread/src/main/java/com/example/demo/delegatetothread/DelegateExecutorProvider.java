package com.example.demo.delegatetothread;

import java.util.concurrent.Executor;

public interface DelegateExecutorProvider {

	Executor getExecutorFor(String name);

}