package org.demo.java.basic.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Test
	public void contextLoads() throws InterruptedException {
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(); 
		CountDownLatch c = new CountDownLatch(2); 
		Thread t1 = new Thread(() -> {
			for(int i = 0; i < 1000; i++) {
				try {
					queue.put(i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}); 
		t1.start();
		
		Thread t2 = new Thread(() -> {
			for(int i = 0; i < 500; i++) {
				try {
					System.out.println(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			c.countDown();
		}); 
		t2.start();
		
		Thread t3 = new Thread(() -> {
			for(int i = 0; i < 500; i++) {
				try {
					System.out.println(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			c.countDown();
		}); 
		t3.start();
		
		c.await();
	}

}
