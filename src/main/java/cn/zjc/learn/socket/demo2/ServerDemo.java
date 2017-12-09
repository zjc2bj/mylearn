package cn.zjc.learn.socket.demo2;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.zjc.learn.util.DateUtil;

/**
 * TODO ServerSocket源码阅读
 * 
 * @author zhujunchao
 *
 */
@SuppressWarnings("resource")
public class ServerDemo {

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket(8080);
		// serverSocket.setSoTimeout(10000);
		println("new server");

		Socket socket = serverSocket.accept();
		println("server accept");

		final InputStream is = socket.getInputStream();
		final OutputStream os = socket.getOutputStream();

		// 读。。。
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					println("before read");

					byte[] buffer = new byte[2048];
					is.read(buffer);
					println("<<========" + new String(buffer));
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

					os.write("server1".getBytes());
					os.flush();
					println("========>>" + "server1");

					Thread.sleep(5000);
					os.write("server2".getBytes());
					os.flush();
					println("========>>" + "server2");

					Thread.sleep(5000);
					os.write("server3".getBytes());
					os.flush();
					println("========>>" + "server3");

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
