package cn.zjc.rpc.demo2.common.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import cn.zjc.rpc.demo2.common.coder.Json2RequestDecoder;
import cn.zjc.rpc.demo2.common.coder.Object2JsonEncoder;

public class NettyServer  {
	public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
	private ServerBootstrap bootstrap;
	private org.jboss.netty.channel.Channel channel;
	private final ChannelHandler handler;
	private int port;

	public NettyServer(int port, ChannelHandler handler) {
		this.port = port;
		this.handler = handler;
		try {
			open(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open(int port) throws Exception {
		System.out.println("NettyServer start....");

		ExecutorService boss = Executors.newCachedThreadPool();
		ExecutorService worker = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, DEFAULT_IO_THREADS);
		bootstrap = new ServerBootstrap(channelFactory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();
				
				//收消息 head-->tail执行pipeline 过滤非ChannelUpstreamHandler实现
				//发消息 tail-->head执行pipeline 过滤非ChannelDownstreamHandler实现
				pipeline.addLast("stringDecoder", new StringDecoder());
				pipeline.addLast("byteBufferEncoder", new StringEncoder());
				pipeline.addLast("object2JsonEncoder", new Object2JsonEncoder());
				pipeline.addLast("requestDecoder", new Json2RequestDecoder());
				pipeline.addLast("handler", handler);
				return pipeline;
			}
		});
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		channel = bootstrap.bind(new InetSocketAddress(port));
		System.out.println("NettyServer has started!!!");
	}
}
