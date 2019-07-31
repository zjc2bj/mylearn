package cn.zjc.learn.concurrent.ReadWriteLock;

import java.util.concurrent.locks.Lock;

public class ReentrantReadWriteLockTest {
	public static volatile boolean thread1Unlock = false;
	public static volatile boolean thread2Unlock = false;
	public static volatile boolean thread3Unlock = false;

	public static void main(String[] args) throws Exception {
		final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
		final Lock read = rwl.readLock();
		final Lock write = rwl.writeLock();

		// 读锁
		new Thread(() -> {
			read.lock();
			while (!thread1Unlock) {
				try {
					Thread.sleep(10000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			read.unlock();

			thread2Unlock = true;
		},"t1-r1").start();

		// 读锁
		Thread.sleep(10000l);
		new Thread(() -> {
			read.lock();
			while (!thread2Unlock) {
				try {
					Thread.sleep(10000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			read.unlock();
		},"t2-r2").start();
		
		// 写锁
		Thread.sleep(10000l);
		new Thread(() -> {
			write.lock();
			write.unlock();
		},"t3-w1").start();

		// 读锁
		Thread.sleep(10000l);
		new Thread(() -> {
			read.lock();
			read.unlock();
		},"t4-r3").start();

		Thread.sleep(30000l);
		thread1Unlock = true;
	}
}
