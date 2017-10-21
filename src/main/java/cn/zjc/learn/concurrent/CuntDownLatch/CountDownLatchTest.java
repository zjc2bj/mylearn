package cn.zjc.learn.concurrent.CuntDownLatch;

public class CountDownLatchTest {
	public static void main(String[] args) throws Exception {
		demo1();
	}

	private static void demo1() throws Exception {
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				System.out.println("countDown1...");
				countDownLatch.countDown();
				System.out.println("countDown2...");
				countDownLatch.countDown();
				System.out.println("countDown end...");
			}
		}, "test1").start();

		System.out.println("await before...");
		countDownLatch.await();
		System.out.println("await end...");
	}

	private static void demo2() throws InterruptedException {
		final CountDownLatch countDownLatch = new CountDownLatch(3);
		new Thread(new Runnable() {
			public void run() {
				System.out.println("countDown...");
				countDownLatch.countDown();
			}
		}, "test1").start();
		new Thread(new Runnable() {
			public void run() {
				System.out.println("countDown...");
				countDownLatch.countDown();
			}
		}, "test2").start();
		new Thread(new Runnable() {
			public void run() {
				System.out.println("countDown...");
				countDownLatch.countDown();
			}
		}, "test3").start();

		System.out.println("await before...");
		countDownLatch.await();
		System.out.println("await end...");
	}
}
