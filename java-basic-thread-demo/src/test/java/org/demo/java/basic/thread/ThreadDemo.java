package org.demo.java.basic.thread;

import lombok.extern.slf4j.Slf4j;

public class ThreadDemo {

	public static void main(String[] args) throws InterruptedException {
		Thread thread1 = new ThreadTarget();
		thread1.start();
	}

	/**
	 * 实现线程方式：1、继承Thread类
	 */
	@Slf4j
	public static class ThreadTarget extends Thread {
		@Override
		public void run() {
			while (true) {
				log.info("Thread extentions: " + System.currentTimeMillis());
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
