package com.example.demo.service;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.delegatetothread.DelegateToThread;

@Component
class RestrictedService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestrictedService.class);

	@Autowired
	private HttpServletRequest request; // prove out thread inheritance

	@DelegateToThread("work-thread")
	public int work(String callingThreadId) {
		LOGGER.debug("task in progress for {}", callingThreadId);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return Integer.parseInt(request.getParameter("a"));
	}

	@DelegateToThread("moreWork-thread")
	public int moreWork(String callingThreadId) {
		LOGGER.debug("task in progress for {}", callingThreadId);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return Integer.parseInt(request.getParameter("a"));
	}

}