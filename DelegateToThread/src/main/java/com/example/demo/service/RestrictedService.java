package com.example.demo.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.delegatetothread.DelegateToThread;

@Component
class RestrictedService {

	@Autowired
	private HttpServletRequest request; // prove out thread inheritance

	@DelegateToThread("workA")
	public int workA(String callingThread) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return Integer.parseInt(request.getParameter("a"));
	}

	@DelegateToThread("workB")
	public int workB(String callingThread) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return Integer.parseInt(request.getParameter("a"));
	}

}