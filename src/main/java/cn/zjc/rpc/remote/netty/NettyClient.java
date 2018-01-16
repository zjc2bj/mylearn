package cn.zjc.rpc.remote.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *	--建立连接
 *	--发送消息
 *		同步发送--需要获取返回结果
 *		异步发送--
 * </pre>
 * 
 * @author zhujunchao
 */
public class NettyClient{
	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private final ChannelHandler handler;
	private org.jboss.netty.channel.Channel channel;

	public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

	// 因ChannelFactory的关闭有DirectMemory泄露，采用静态化规避
	// https://issues.jboss.org/browse/NETTY-424

	public NettyClient(String host, int port, ChannelHandler handler) throws Exception {
		this.handler = handler;
		connect(host, port);
	}

	public void connect(String host, int port) throws Exception {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), DEFAULT_IO_THREADS));
		// config
		// @see org.jboss.netty.channel.socket.SocketChannelConfig
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("tcpNoDelay", true);
		// bootstrap.setOption("connectTimeoutMillis", 1000);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();
				// pipeline.addLast("decoder", adapter.getDecoder());
				// pipeline.addLast("encoder", adapter.getEncoder());
				// pipeline.addLast("lineBased", new LineBasedFrameDecoder(200));
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("handler", handler);
				return pipeline;
			}
		});
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		try {
			boolean ret = future.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
			if (ret && future.isSuccess()) {
				Channel newChannel = future.getChannel();
				newChannel.setInterestOps(Channel.OP_READ_WRITE);
				channel = newChannel;
			} else {
				channel = null;
				throw new RuntimeException("client failed to connect to server " + getRemoteAddress()
						+ ", error message is:" + future.getCause().getMessage(), future.getCause());
			}
		} finally {
		}
	}

	public void sendMessage(Object message, boolean isAsync) throws Exception {
		boolean success = true;
		int timeout = 0;
		try {
			ChannelBuffer time = ChannelBuffers.buffer(((String) message).length());
			time.writeBytes(((String) message).getBytes());
			ChannelFuture future = channel.write(time);
			if (isAsync) {
				timeout = 1000;
				success = future.await(timeout);
			}
			Throwable cause = future.getCause();
			if (cause != null) {
				throw cause;
			}

			// future.addListener(new ChannelFutureListener() {
			// public void operationComplete(ChannelFuture future) {
			// Channel ch = future.getChannel();
			// ch.close();
			// }
			// });
		} catch (Throwable e) {
			throw new RuntimeException(
					"Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(),
					e);
		}

		if (!success) {
			throw new RuntimeException("Failed to send message " + message + " to " + getRemoteAddress() + "in timeout("
					+ timeout + "ms) limit");
		}
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) channel.getLocalAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	public static void main(String[] args) {
		try {
			NettyClient nettyClient = new NettyClient("localhost", 8080, new NettyClientHandler());
			for (int i = 0; i < 10; i++) {
				nettyClient.sendMessage("client send times " + i, true);
				Thread.sleep(5);// 如果不加sleep 或sleep时间太短 会产生沾包 多次请求 会当成一次请求
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUrl() {
		return channel.getRemoteAddress().toString();
	}
}
