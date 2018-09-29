package com.example.demo.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.example.demo.delegatetothread.DelegateThreadLifecycle;

@Component
class DelegateThreadLifecycleInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private DelegateThreadLifecycle delegateThreadLifecycle;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		delegateThreadLifecycle.start();
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		delegateThreadLifecycle.stop();
	}
}