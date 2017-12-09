package cn.zjc.learn.socket.demo1;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO ServerSocket源码阅读
 * 
 * @author zhujunchao
 *
 */
public class ServerDemo {

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket(8080);
		serverSocket.setSoTimeout(10000);
		System.out.println("time=" + System.currentTimeMillis() / 1000 + " new server");
		Socket socket = serverSocket.accept();
		System.out.println("time=" + System.currentTimeMillis() / 1000 + " server accept");

		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();

		System.out.println("time=" + System.currentTimeMillis() / 1000 + " before read");
		int i = -1;

		// while((i = is.read()) != -1) {
		// System.out.print((char)i);
		// }

//		for (int j = 0; j < 22; j++) {
//			System.out.println("time=" + System.currentTimeMillis() / 1000 + "; " + j + "=" + is.read() + "");
//		}

		 byte[] buffer = new byte[2048];
		 i = is.read(buffer);
		 System.out.println(new String(buffer));

		System.out.println();
		System.out.println("time=" + System.currentTimeMillis() / 1000 + " after read");

		Thread.sleep(5000);

		System.out.println("time=" + System.currentTimeMillis() / 1000 + " before write");
		os.write("im server".getBytes());
		os.flush();
		System.out.println("time=" + System.currentTimeMillis() / 1000 + " after write");
		i = -1;
		while ((i = is.read()) != -1) {
			System.out.print((char) i);
		}
		System.out.println();
		System.out.println("time=" + System.currentTimeMillis() / 1000 + " second read!!!");
	}
}
