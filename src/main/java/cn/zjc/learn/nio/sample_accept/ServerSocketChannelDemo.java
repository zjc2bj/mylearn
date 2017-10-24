package cn.zjc.learn.nio.sample_accept;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelDemo {
	public static void main(String[] args) throws Exception {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(9999));
		
		//设置阻塞 则accept直接返回
		// serverSocketChannel.configureBlocking(false);

		while (true) {
			Thread.sleep(200);
			System.out.println("accept...");
			SocketChannel socketChannel = serverSocketChannel.accept();
			System.out.println("accepted!!!");
			if (socketChannel != null) {
				String newData = "New String to write to file..." + System.currentTimeMillis();
				ByteBuffer buf = ByteBuffer.allocate(1024);
				buf.clear();
				buf.put(newData.getBytes());
				buf.flip();

				while (buf.hasRemaining()) {
					socketChannel.write(buf);
				}
				System.out.println("write################");
			}
		}
	}
}
