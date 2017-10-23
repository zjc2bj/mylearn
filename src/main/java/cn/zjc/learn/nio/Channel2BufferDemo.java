package cn.zjc.learn.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Channel2BufferDemo {
	public static void main(String[] args) throws Exception {
		testReadArray();
	}

	private static void testRead() throws FileNotFoundException, IOException, Exception {
		URL resource = Channel2BufferDemo.class.getClassLoader().getResource("data/nio-data.txt");
		RandomAccessFile aFile = new RandomAccessFile(resource.getPath(), "rw");
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer buf = ByteBuffer.allocate(15);
		int bytesRead = inChannel.read(buf);
		BufferUtils.print(buf, "read: ");

		while (bytesRead != -1) {
			System.out.println("Read " + bytesRead);
			buf.flip();
			BufferUtils.print(buf, "flip: ");

			while (buf.hasRemaining()) {
				System.out.print((char) buf.get() + "===== ");
				BufferUtils.print(buf, "get: ");
			}

			buf.clear();
			BufferUtils.print(buf, "clear: ");

			bytesRead = inChannel.read(buf);
			BufferUtils.print(buf, "reRead: ");

		}
		aFile.close();
	}

	private static void testReadArray() throws FileNotFoundException, IOException, Exception {
		URL resource = Channel2BufferDemo.class.getClassLoader().getResource("data/nio-data-head-body.txt");
		RandomAccessFile aFile = new RandomAccessFile(resource.getPath(), "rw");
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer header = ByteBuffer.allocate(37);
		ByteBuffer body = ByteBuffer.allocate(1024);

		ByteBuffer[] bufferArray = { header, body };
		long bytesRead = inChannel.read(bufferArray);

		if (bytesRead != -1) {
			System.out.println("Read " + bytesRead);
			header.flip();
			BufferUtils.print(header, "flip: ");

			while (header.hasRemaining()) {
				System.out.print((char) header.get());
			}

			System.out.println("/r/n============================");
			body.flip();
			BufferUtils.print(body, "flip: ");

			while (body.hasRemaining()) {
				System.out.print((char) body.get());
			}

		}
		aFile.close();
	}
}
