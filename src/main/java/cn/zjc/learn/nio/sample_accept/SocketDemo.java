package cn.zjc.learn.nio.sample_accept;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.Socket;

public class SocketDemo {
	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("127.0.0.1", 9999);
		InputStream is = socket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] b = new byte[1024];
		bis.read(b);
		for (byte c : b) {
			System.out.print((char)c);
		}
	}
}
