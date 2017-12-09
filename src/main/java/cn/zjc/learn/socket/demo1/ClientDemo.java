package cn.zjc.learn.socket.demo1;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientDemo {

	public static void main(String[] args) throws Exception {
		System.out.println("time=" + System.currentTimeMillis()/1000 + " client init...");
		Socket socket = new Socket("127.0.0.1", 8080);
		System.out.println("time=" + System.currentTimeMillis()/1000 + " client connect!!!");

		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();
		Thread.sleep(5000);
		os.write("times 1".getBytes());
		os.flush();
		
		Thread.sleep(5000);
		os.write("times 2".getBytes());
		os.flush();

		Thread.sleep(5000);
		os.write("times 3".getBytes());
		os.flush();

		System.out.println("time=" + System.currentTimeMillis()/1000 + " before read");
		int i = -1;
		while ((i = is.read()) != -1) {
			System.out.print((char) i);
		}
		System.out.println();
		System.out.println("time=" + System.currentTimeMillis()/1000 + " after read");
		
		Thread.sleep(5000);

		System.out.println("time=" + System.currentTimeMillis()/1000 + " before second write");
		os.write("im server".getBytes());
		os.flush();
		System.out.println("time=" + System.currentTimeMillis()/1000 + " after second  write");
	}
}
