package cn.zjc.learn.nio;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelDemo {
	public static void main(String[] args) throws Exception {
		URL resource = FileChannelDemo.class.getClassLoader().getResource("data/nio-data.txt");
		RandomAccessFile aFile = new RandomAccessFile(resource.getPath(), "rw");
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer buf = ByteBuffer.allocate(15);
		int bytesRead = inChannel.read(buf);
		BufferUtils.print(buf,"read: ");
		
		while (bytesRead != -1) {
			System.out.println("Read " + bytesRead);
			buf.flip();
			BufferUtils.print(buf,"flip: ");


			while (buf.hasRemaining()) {
				System.out.print((char) buf.get()+"===== ");
				BufferUtils.print(buf,"get: ");
			}

			buf.clear();
			BufferUtils.print(buf,"clear: ");

			bytesRead = inChannel.read(buf);
			BufferUtils.print(buf,"reRead: ");

		}
		aFile.close();
	}
}
