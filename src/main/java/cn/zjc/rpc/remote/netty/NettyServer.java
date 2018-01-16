package cn.zjc.rpc.remote.netty;

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

public class NettyServer{
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
		ExecutorService boss = Executors.newCachedThreadPool();
		ExecutorService worker = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, DEFAULT_IO_THREADS);
		bootstrap = new ServerBootstrap(channelFactory);

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
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		channel = bootstrap.bind(new InetSocketAddress(port));
		System.out.println("NettyServer start...");
	}

	public static void main(String[] args) {
		try {
			NettyServer nettyServer = new NettyServer(8080, new NettyServerHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
