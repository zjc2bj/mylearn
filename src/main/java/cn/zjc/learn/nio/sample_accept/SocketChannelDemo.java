package cn.zjc.learn.nio.sample_accept;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketChannelDemo {
	public static void main(String[] args) throws Exception {
		demo1();
	}

	
	public static void demo1() throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));

		String newData = "has a new request...";
		ByteBuffer bufWrite = ByteBuffer.allocate(1024);
		bufWrite.clear();
		bufWrite.put(newData.getBytes());
		bufWrite.flip();
		
		while (bufWrite.hasRemaining()) {
			socketChannel.write(bufWrite);
		}
		//正常流程（阻塞模式） 只能先写 再读，否则 读没数据进来则阻塞
		ByteBuffer bufRead = ByteBuffer.allocate(1024);
		int bytesRead = socketChannel.read(bufRead);
		
		while (bytesRead != -1) {
			bufRead.flip();
			while (bufRead.hasRemaining()) {
				System.out.print((char) bufRead.get());
			}

			bufRead.clear();
			bytesRead = socketChannel.read(bufRead);
		}
		socketChannel.close();
	}
	
	public static void demo2() throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));

		String newData = "has a new request...";
		ByteBuffer bufWrite = ByteBuffer.allocate(1024);
		bufWrite.clear();
		bufWrite.put(newData.getBytes());
		bufWrite.flip();

		socketChannel.configureBlocking(false);
		ByteBuffer bufRead = ByteBuffer.allocate(1024);
		int bytesRead = socketChannel.read(bufRead);//没有数据 则返回0
		//非阻塞模式 可以先读  再写，循环读即可
		while (bufWrite.hasRemaining()) {
			socketChannel.write(bufWrite);
		}
		
		while (bytesRead != -1) {
			bufRead.flip();
			while (bufRead.hasRemaining()) {
				System.out.print((char) bufRead.get());
			}

			bufRead.clear();
			bytesRead = socketChannel.read(bufRead);
		}
		socketChannel.close();
	}
}
