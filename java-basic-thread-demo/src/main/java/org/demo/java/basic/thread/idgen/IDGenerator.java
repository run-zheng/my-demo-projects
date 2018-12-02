package org.demo.java.basic.thread.idgen;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSON;

public class IDGenerator {

	public static void main(String[] args) throws InterruptedException {
		RuleService ruleService = new RuleService(); 
		RuleQuery ruleQuery = new RuleQuery(); 
		ruleQuery.setKey("Hello world!");
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10); 
		
		
		long start = System.currentTimeMillis(); 
		CountDownLatch countDown = new CountDownLatch(10);
		for(int h = 0; h < 100; h++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					RuleSequence sequence = null; 
					for(int i = 0; i < 100; i++) {
						sequence = ruleService.nextSequence(ruleQuery);
						sequence.getSequence();
					}
					countDown.countDown();
				}
			});
		}
		countDown.await(); 
		long end = System.currentTimeMillis(); 
		System.out.println(atomicSequence.get() + "   " + (end - start));
		
	}
	
	public static class RuleServiceException extends RuntimeException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 910438428646805365L;
		public RuleServiceException() {
			super();
		}
		public RuleServiceException(String msg) {
			super(msg);
		}
	}
	
	public static class RuleService {
		private ConcurrentHashMap<String, RuleResource> RULE_MAP = new ConcurrentHashMap<>(); 
		
		public RuleSequence nextSequence(RuleQuery ruleQuery) {
			RuleResource ruleResource = getRuleResource(ruleQuery);
			RuleSequence sequence =  ruleResource.nextSequence(ruleQuery); 
			return sequence; 
		}
		
		public RuleResource getRuleResource(RuleQuery ruleQuery) {
			RuleResource ruleResource = RULE_MAP.get(ruleQuery.getKey()); 
			if(ruleResource == null) {
				RuleResourceConfig config = getResourceConfig(ruleQuery); 
				RuleResource newRuleResource = new RuleResource(config); 
				ruleResource = RULE_MAP.putIfAbsent(ruleQuery.getKey(), newRuleResource) ; 
				if(ruleResource == null) {
					ruleResource = newRuleResource;
					ruleResource.init(); 
				}
			}
			return ruleResource; 
		}
		
		public RuleResourceConfig getResourceConfig(RuleQuery ruleQuery) {
			RuleResourceConfig config = new RuleResourceConfig(); 
			config.setKey(ruleQuery.getKey());
			config.setGenTaskSize(6);
			config.setGenThreadPoolSize(3);
			config.setSequenceCapacity(400);
			config.setSequenceBatchGenSize(200);
			config.setSequenceloadFactor(0.75f);
			config.setGenThreadPoolMinSize(1);
			return config; 
		}
	}
	
	public static class RuleResourceConfig {
		private String key; 
		private int sequenceBatchGenSize; //批次生成数量
		private int sequenceCapacity; //整体容量
		private float sequenceloadFactor ; //扩容指数   默认0.75f
		private int genTaskSize;   //生产者队列长度
		private int genThreadPoolMinSize; //最小生产者线程池大小
		private int genThreadPoolSize; //生产者线程池大小
		
		public int getGenThreadPoolMinSize() {
			return genThreadPoolMinSize;
		}
		public void setGenThreadPoolMinSize(int genThreadPoolMinSize) {
			this.genThreadPoolMinSize = genThreadPoolMinSize;
		}
		public int getSequenceBatchGenSize() {
			return sequenceBatchGenSize;
		}
		public void setSequenceBatchGenSize(int sequenceBatchGenSize) {
			this.sequenceBatchGenSize = sequenceBatchGenSize;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public int getSequenceCapacity() {
			return sequenceCapacity;
		}
		public void setSequenceCapacity(int sequenceCapacity) {
			this.sequenceCapacity = sequenceCapacity;
		}
		public float getSequenceloadFactor() {
			return sequenceloadFactor;
		}
		public void setSequenceloadFactor(float sequenceloadFactor) {
			this.sequenceloadFactor = sequenceloadFactor;
		}
		
		public int getLowLoadFactorCapacity() {
			return (int)(getSequenceCapacity() * (1 - getSequenceloadFactor()));
		}
		
		public int getGenTaskSize() {
			return genTaskSize;
		}
		public void setGenTaskSize(int genTaskSize) {
			this.genTaskSize = genTaskSize;
		}
		public int getGenThreadPoolSize() {
			return genThreadPoolSize;
		}
		public void setGenThreadPoolSize(int genThreadPoolSize) {
			this.genThreadPoolSize = genThreadPoolSize;
		}
	}
	
	public static class RuleResource {
		private volatile RuleResourceConfig config; 
		private LinkedBlockingQueue<RuleSequence> sequenceQueue; 
		private PriorityBlockingQueue<Runnable> genTaskQueue; 
	    private ThreadPoolExecutor genTaskExecutor; 
		private volatile int initState = 0;
		
	    private ReentrantLock lock = new ReentrantLock(); 
	    private ReentrantLock taskAddLock = new ReentrantLock(); 
	    private AtomicLong taskOrder = new AtomicLong(0L); 
	    
	    public RuleResource(RuleResourceConfig config) {
	    	this.config = config; 
	    }
		public void init() {
			if(initState == 0) {
				try {
					lock.lock();
					if(initState == 0) {
						//初始化序列号队列
						sequenceQueue = new LinkedBlockingQueue<>(config.getSequenceCapacity()); 
						//初始化生成任务队列
						genTaskQueue = new PriorityBlockingQueue<>(config.getGenTaskSize()); 
						int poolMinSize = getPoolMinSize();
						genTaskExecutor = new ThreadPoolExecutor(poolMinSize, 
								config.getGenThreadPoolSize(), 0L, TimeUnit.MILLISECONDS,
								genTaskQueue, Executors.defaultThreadFactory(), 
								new DiscardPolicy());
						//更新初始化状态
						initState = 1; 
					}else {
						return; 
					}
				}finally{
					lock.unlock(); 
				}
			}else {
				return ; 
			}
		}
		
		private int getPoolMinSize() {
			int poolSize = config.getGenThreadPoolMinSize();  
			//计算poolSize: 
			//  如果最最小线程数未设置， 那么取min(线程次线程数一半, CPU核数),  如果poolSize<=0 取1 
			//  如果poolsize > 最大线程数，缩小成最大线程数
			if(poolSize <= 0 ) {
				int availProcessors = Runtime.getRuntime().availableProcessors(); 
				int halfOfTask = ((int)(config.getGenThreadPoolSize() / 2));
				poolSize = halfOfTask; 
				if(halfOfTask > availProcessors ) {
					poolSize = availProcessors; 
				}
				if(poolSize <= 0 ) {
					poolSize = 1; 
				}
			}
			if(poolSize > config.getGenThreadPoolSize()) {
				poolSize = config.getGenThreadPoolSize();
			}
			return poolSize;
		}

		public RuleSequence nextSequence(RuleQuery ruleQuery) {
			checkInitState();
			
			ensureCapacity(ruleQuery);

			RuleSequence sequence;
			try {
				sequence = sequenceQueue.take();
				System.out.println("Thread: " + Thread.currentThread().getName()+ " take a sequence:  " + sequence.getSequence() + " queque size: " + sequenceQueue.size() + 
						" task queue size: " + genTaskQueue.size() + " config: " + JSON.toJSONString(config));
				return sequence;
			} catch (InterruptedException e) {
				throw new RuleServiceException("获取序列号失败");
			} 
			//return null; 
		}
		
		private void ensureCapacity(RuleQuery ruleQuery) {
			if(sequenceQueue.size() <= config.getLowLoadFactorCapacity()) {
				if(genTaskQueue.size() <= config.getGenTaskSize()) {
					try{
						taskAddLock.lock();
						if(sequenceQueue.size() <= config.getLowLoadFactorCapacity() && genTaskQueue.size() <= config.getGenTaskSize()) {
							RuleSequenceGenTask genTask = new RuleSequenceGenTask(this, ruleQuery, taskOrder.getAndIncrement()); 
							System.out.println("gen task: " + JSON.toJSONString(ruleQuery) + " sequence queue size: " + sequenceQueue.size() + 
									" capacity: " + config.getSequenceCapacity() + " loadFactor: " + config.getSequenceloadFactor() + 
									" extend capacity: " + (config.getSequenceCapacity() * (1 - config.getSequenceloadFactor())) + 
									" gen task queue size: " + genTaskQueue.size());
							genTaskExecutor.execute(genTask); 
						}
					}finally {
						taskAddLock.unlock();
					}
					
				}
			}
		}
		private void checkInitState() {
			if(this.initState <= 0 ) {
				if(this.config == null) {
					throw new IllegalStateException("未设置Config");
				}else {
					init();
				}
			}
		} 

		public void addSequence(RuleQuery ruleQuery, RuleSequence sequence) {
			try {
				this.sequenceQueue.put(sequence);
				/*System.out.println("Thread: " + Thread.currentThread().getName() + " product sequence: " + 
						sequence.getSequence() + " sequence queue size: " + this.sequenceQueue.size()
						+ " task queue size: " + this.genTaskQueue.size());*/
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class RuleSequence{
		private Long sequence ; 
		public Long getSequence() {
			return sequence;
		}
		public void setSequence(Long sequence) {
			this.sequence = sequence;
		}
	}
	

	private static AtomicLong atomicSequence = new AtomicLong(0);
	private static ReentrantLock sequenceLog = new ReentrantLock(); 
	
	public static class RuleSequenceGenTask implements Runnable, Comparable<RuleSequenceGenTask>{
		private RuleQuery ruleQuery; 
		private RuleResource ruleResource; 
		
		private static Semaphore semaphore = new Semaphore(1);
		
		private long order; 
		
		public RuleSequenceGenTask(RuleResource ruleResource, RuleQuery ruleQuery, long order) {
			this.ruleQuery = ruleQuery; 
			this.ruleResource = ruleResource; 
			this.order = order; 
		}
		@Override
        public int compareTo(RuleSequenceGenTask o) {
            if(this.order>o.order){
                return -1;
            }else if(this.order<o.order){
                return 1;
            }
            return 0;
        }

		@Override
		public void run() {
			//TODO: query sequence by ruleQuery 
			/*try {
				int sleep = Math.abs(new Random(System.currentTimeMillis()%300).nextInt())%300;
				//System.out.println("time to sleep: " + sleep );
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
			int count = 200;
			long oldSequence = 0; 
			long newSequence = 0; 
			try {
				sequenceLog.lock();
				try {
					int sleep = Math.abs(new Random(System.currentTimeMillis()%300).nextInt())%300;
					//System.out.println("time to sleep: " + sleep );
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				oldSequence = atomicSequence.get(); 
				newSequence = atomicSequence.addAndGet(count);
				System.out.println("Thread: " + Thread.currentThread().getName()+" add sequence to queue: " + count + " atomic old sequence: " + oldSequence + " new sequence: " + newSequence+ " sequence queue:"+ruleResource.sequenceQueue.size());
				System.out.println("Thread: " + Thread.currentThread().getName()+" add sequence to queue end: " + count + " atomic old sequence: " + oldSequence + " new sequence: " + newSequence+ " sequence queue:"+ruleResource.sequenceQueue.size());
			}catch(Exception e){
				e.printStackTrace();
			}finally {
				sequenceLog.unlock();
			}
			RuleSequence sequence = null; 
			for(long i = oldSequence; i < newSequence; i++) { 
				sequence = new RuleSequence(); 
				sequence.setSequence(i);
				this.ruleResource.addSequence(this.ruleQuery, sequence);
			}
		}
	}
	
	public static class RuleQuery {
		private String key ; 
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
	}
}
