package cn.zjc.learn.concurrent.ReentrantLock;

public class ReentrantLockTest {
	public static volatile boolean thread1Unlock = false;
	public static volatile boolean thread2Unlock = false;
	public static volatile boolean thread3Unlock = false;
	
	public static void main(String[] args) throws Exception {
		ReentrantLock lock = new ReentrantLock(true);
		
		new Thread(()->{
			System.out.println(lock.getHoldCount());
			lock.lock();
			while(!thread1Unlock) {
				try {
					Thread.sleep(10000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lock.unlock();
			
			thread2Unlock = true;
		}).start();
		
		Thread.sleep(10000l);
		new Thread(()->{
			System.out.println(lock.getHoldCount());
			lock.lock();
			
			while(!thread2Unlock) {
				try {
					Thread.sleep(10000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lock.unlock();
			
			thread3Unlock = true;
		}).start();
		
		Thread.sleep(10000l);
		new Thread(()->{
			System.out.println(lock.getHoldCount());
			lock.lock();
			
			while(!thread3Unlock) {
				try {
					Thread.sleep(10000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lock.unlock();
		}).start();
		
		Thread.sleep(30000l);
		thread1Unlock = true;
	}
}
