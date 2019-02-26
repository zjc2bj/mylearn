package cn.zjc.learn.concurrent.DelayQueue;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {
	public static void main(String[] args) {
		DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
		delayQueue.put(new DelayedTask("1", 1L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("3", 3L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("3", 3L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("3", 3L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("3", 3L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("5", 5L, TimeUnit.SECONDS));
		delayQueue.put(new DelayedTask("10", 10L, TimeUnit.SECONDS));
		
		System.out.println("queue put done");

	    while(!delayQueue.isEmpty()) {
	        try {
	        	DelayedTask task = delayQueue.take();
	            System.out.println(task.name + ":" + System.currentTimeMillis());
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	}
}
