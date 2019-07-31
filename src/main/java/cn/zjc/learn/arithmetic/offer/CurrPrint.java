package cn.zjc.learn.arithmetic.offer;

/**
 * 交替打印
 * 
 * @author zhujunchao02
 */
public class CurrPrint {
	public static int i = 0;
	public static final Object monitor = new Object();

	public static void main(String[] args) {

		new Thread(() -> {
			while (i < 100)
				synchronized (monitor) {
					if (i % 3 == 0) {
						System.out.println("线程1: " + i++);
					} else {
						monitor.notifyAll();
						try {
							monitor.wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
		}).start();
		new Thread(() -> {
			while (i < 100)
				synchronized (monitor) {
					if (i % 3 == 1) {
						System.out.println("线程2: " + i++);
					} else {
						monitor.notifyAll();
						try {
							monitor.wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
		}).start();
		new Thread(() -> {
			while (i < 100)
				synchronized (monitor) {
					if (i % 3 == 2) {
						System.out.println("线程3: " + i++);
					} else {
						monitor.notifyAll();
						try {
							monitor.wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
		}).start();
	}
}
