package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@SpringBootApplication
@EnableAsync
public class DelegateToThreadApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(DelegateToThreadApplication.class, args);
	}

	@Autowired
	List<HandlerInterceptorAdapter> interceptors;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		interceptors.forEach(i -> registry.addInterceptor(i));
	}

	@Autowired
	public void setThreadContextInheritable(RequestContextFilter requestContextFilter, DispatcherServlet dispatcherServlet) {
		requestContextFilter.setThreadContextInheritable(true);
		dispatcherServlet.setThreadContextInheritable(true);
	}
}
