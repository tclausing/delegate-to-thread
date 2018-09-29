package com.example.demo.delegatetothread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DelegateThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(DelegateThread.class);

	private Runnable runnable = null;

	public DelegateThread(String name) {
		super(name);
		start();
	}

	public void submit(Runnable runnable) {
		synchronized (this) {
			while (this.runnable != null) {
				try {
					LOGGER.debug("producer thread waiting to submit task");
					wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			LOGGER.debug("producer thread submitting task");
			this.runnable = runnable;
			notify();
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			while (true) {
				while (runnable == null) {
					try {
						LOGGER.debug("consumer thread waiting for task");
						wait();
					} catch (InterruptedException e) {
						LOGGER.debug("consumer thread stopping");
						return; // expected exit point
					}
				}

				runnable.run();

				LOGGER.debug("consumer thread notifying ready for next task");
				runnable = null;
				notify();
			}
		}
	}
}