package cn.zjc.rpc.remote.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * 处理服务端接收请求
 * 
 * @author zhujunchao
 *
 */
public class NettyServerHandler extends SimpleChannelHandler {
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// 处理消息--调用后台
		System.out.println("server received....");

		// ChannelBuffer buf = (ChannelBuffer) e.getMessage();
		// while (buf.readable()) {
		// System.out.print((char) buf.readByte());
		// }

		String message = (String) e.getMessage();
		System.out.println("server received = " + message);

		Channel channel = ctx.getChannel();
		ChannelBuffer time = ChannelBuffers.buffer(32);
		time.writeBytes((System.currentTimeMillis() + "").getBytes());
		ChannelFuture f = channel.write(time);
		
		// channel.write(e.getMessage());
		// f.addListener(new ChannelFutureListener() {
		// public void operationComplete(ChannelFuture future) {
		// Channel ch = future.getChannel();
		// ch.close();
		// }
		// });
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		System.out.println(" NettyServerHandler.channelConnected...");
		ctx.sendUpstream(e);
	}
}
