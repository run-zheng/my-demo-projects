package org.demo.java.basic.thread;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadDemo {

	public static void main(String[] args) throws InterruptedException {
		extendsThread();
		implementsRunnable();
		useFutureTaskGetThreadExecuteResult();
		useTimerExecuteScheduleTask();
		useExecutorFramework();
	}

	private static void useExecutorFramework() throws InterruptedException {
		/**
		 * 实现多线程的方式： 5、java.util.concurrent包提供的Executor框架,创建线程池，执行任务
		 */
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		//通过execute执行实现Runnable接口的任务
		for(int i = 0; i < 100; i++) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("ThreadPool execute task: " 
							+ Thread.currentThread().getName()
							+ "  " + System.currentTimeMillis());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		//也可以通过submit实现Callable接口的任务，以便获取线程执行结果
		Future<Integer> fiboResult = executorService.submit(new Fibonacci(10)); 
		try {
			System.out.println("ThreadPool submit task: "+fiboResult.get());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static void useTimerExecuteScheduleTask() {
		/**
		 * 实现线程方式：4、调度执行任务
		 */
		Timer timer = new Timer(); 
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("TimerTask execute: " + System.currentTimeMillis());
			}
		}, 5000, 3000);
	}

	private static void useFutureTaskGetThreadExecuteResult() throws InterruptedException {
		/**
		 * 实现线程方式： 3、实现Callable接口，配合FutureTask，获取线程执行结果
		 */
		FutureTask<Integer> task = new FutureTask<>(new Fibonacci(10));
		Thread thread4 = new Thread(task);
		thread4.start();
		try {
			Integer result = task.get();
			System.out.println("FutureTask result: " + result);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static class Fibonacci implements Callable<Integer> {
		public int n;

		public Fibonacci(int n) {
			assert (n > 0);
			this.n = n;
		}

		@Override
		public Integer call() throws Exception {
			return calcFibonacci(n);
		}

		public int calcFibonacci(int n) {
			if (n == 1 || n == 2) {
				return 1;
			}
			return calcFibonacci(n - 1) + calcFibonacci(n - 2);
		}
	}

	private static void extendsThread() {
		Thread thread1 = new ThreadTarget();
		thread1.start();
	}

	private static void implementsRunnable() {
		/**
		 * 实现线程方式： 2、实现Runnable接口
		 */
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					log.info("Runnable implements 1: " + System.currentTimeMillis());
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread2.start();
		/**
		 * 实现线程方式： jdk1.8之后，可以更简单的写法
		 */
		Thread thread3 = new Thread(() -> {
			while (true) {
				log.info("Runnable implements 2: " + System.currentTimeMillis());
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread3.start();
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
