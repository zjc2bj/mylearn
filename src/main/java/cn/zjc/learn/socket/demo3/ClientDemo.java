package cn.zjc.learn.socket.demo3;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import cn.zjc.learn.util.DateUtil;

/**
 * 
 * @author zhujunchao
 * @see SocketOutputStream
 */
@SuppressWarnings("resource")
public class ClientDemo {

	public static void main(String[] args) throws Exception {
		println("client init...");
		Socket socket = new Socket("127.0.0.1", 8080);
		println("client connect!!!");

		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		Thread.sleep(5000);

		// 读。。。
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					println("before read");

					int i = -1;
					while ((i = is.read()) != -1) {
						System.out.print((char) i);
					}
					
					println("after read");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		// 写。。。
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					println("before write");

					os.write("client1".getBytes());
					os.flush();
					println("========>>" + "client1");

					Thread.sleep(5000);
					os.write("client2".getBytes());
					os.flush();
					println("========>>" + "client2");

					Thread.sleep(5000);
					os.write("client3".getBytes());
					os.flush();
					println("========>>" + "client3");

					println("after write");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static void println(String s) {
		System.out.println("[" + DateUtil.getHMSStr() + "] " + s);
	}
}
