package cn.zjc.learn.nio;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SelectorDemo {

	/**
	 * 基本语法
	 */
	public void learn1() throws IOException, ClosedChannelException {
		Selector selector = Selector.open();

		// 将Channel和Selector配合使用，必须将channel注册到selector上
		// 与Selector一起使用时，Channel必须处于非阻塞模式下
		SocketChannel channel = null;
		channel.configureBlocking(false);

		// register()方法的第二个参数。这是一个“interest集合”
		// int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		// selectionKey代表了注册到该Selector的通道
		SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);

		// =======================selectionKey==================================
		// 检测channel中什么事件或操作已经就绪
		int interestSet = selectionKey.interestOps();
		int readySet = selectionKey.readyOps();
		selectionKey.isAcceptable();
		selectionKey.isConnectable();
		selectionKey.isReadable();
		selectionKey.isWritable();
		selectionKey.channel();
		selectionKey.selector();
		selectionKey.attach(null);// 将一个对象或者更多信息附着到SelectionKey上

		// =======================selector==================================
		selector.select();// 阻塞到至少有一个通道在你注册的事件上就绪了
		selector.selectNow();// 不会阻塞，不管什么通道就绪都立刻返回

		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
		while (keyIterator.hasNext()) {
			SelectionKey key = keyIterator.next();
			if (key.isAcceptable()) {
				// a connection was accepted by a ServerSocketChannel.
			} else if (key.isConnectable()) {
				// a connection was established with a remote server.
			} else if (key.isReadable()) {
				// a channel is ready for reading
			} else if (key.isWritable()) {
				// a channel is ready for writing
			}
			keyIterator.remove();
		}

		selector.select();// 阻塞
		selector.wakeup();// 唤醒阻塞

		// 用完Selector后调用其close()方法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实例无效。通道本身并不会关闭。
		selector.close();
	}

	/**
	 * 完整流程示例
	 */
	public void learn2() throws Exception {
		SocketChannel channel = null;

		Selector selector = Selector.open();
		channel.configureBlocking(false);
		SelectionKey chanelKey = channel.register(selector, SelectionKey.OP_READ);
		while (true) {
			int readyChannels = selector.select();
			if (readyChannels == 0)
				continue;
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				if (key.isAcceptable()) {
					// a connection was accepted by a ServerSocketChannel.
				} else if (key.isConnectable()) {
					// a connection was established with a remote server.
				} else if (key.isReadable()) {
					// a channel is ready for reading
				} else if (key.isWritable()) {
					// a channel is ready for writing
				}
				keyIterator.remove();
			}
		}
	}
}
