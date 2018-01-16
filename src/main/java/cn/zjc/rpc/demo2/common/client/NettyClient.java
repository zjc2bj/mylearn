package cn.zjc.rpc.demo2.common.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zjc.rpc.demo2.common.DefaultFuture;
import cn.zjc.rpc.demo2.common.coder.Json2ResponseDecoder;
import cn.zjc.rpc.demo2.common.coder.Object2JsonEncoder;
import cn.zjc.rpc.demo2.common.vo.Request;

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
public class NettyClient {
	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private final ChannelHandler handler;
	private org.jboss.netty.channel.Channel channel;

	public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

	public NettyClient(String host, int port, ChannelHandler handler) throws Exception {
		this.handler = handler;
		connect(host, port);
	}

	public void connect(String host, int port) throws Exception {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), DEFAULT_IO_THREADS));

		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("tcpNoDelay", true);
		// bootstrap.setOption("connectTimeoutMillis", 1000);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();

				// 收消息 head-->tail执行pipeline 过滤非ChannelUpstreamHandler实现
				// 发消息 tail-->head执行pipeline 过滤非ChannelDownstreamHandler实现
				pipeline.addLast("stringDecoder", new StringDecoder());
				pipeline.addLast("byteBufferEncoder", new StringEncoder());
				pipeline.addLast("jsonDecoder", new Json2ResponseDecoder());
				pipeline.addLast("jsonEncoder", new Object2JsonEncoder());
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

	public DefaultFuture sendMessage(Object message, boolean isAsync) throws Exception {
		boolean success = true;
		int timeout = 0;
		DefaultFuture defaultFuture = null;
		try {
			Request request = new Request();
			request.setVersion("2.0.0");
			request.setData(message);
			ChannelFuture future = channel.write(request);
			if (isAsync) {
				timeout = 1000;
				success = future.await(timeout);
			}
			Throwable cause = future.getCause();
			if (cause != null) {
				throw cause;
			}

			defaultFuture = new DefaultFuture(channel, request, timeout);
		} catch (Throwable e) {
			throw new RuntimeException(
					"Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(),
					e);
		}

		if (!success) {
			throw new RuntimeException("Failed to send message " + message + " to " + getRemoteAddress() + "in timeout("
					+ timeout + "ms) limit");
		}
		return defaultFuture;
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) channel.getLocalAddress();
	}

	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	public String getUrl() {
		return channel.getRemoteAddress().toString();
	}
}
