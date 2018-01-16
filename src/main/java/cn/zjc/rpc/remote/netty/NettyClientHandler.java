package cn.zjc.rpc.remote.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * 用户端返回结果
 * 
 * @author zhujunchao
 *
 */
public class NettyClientHandler extends SimpleChannelHandler {
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// 处理消息--调用后台
		System.out.println("client received....");
		Channel channel = ctx.getChannel();
		System.out.println(ctx.getAttachment());
		System.out.println(e.getRemoteAddress());

		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
		while (buf.readable()) {
			System.out.print((char) buf.readByte());
		}
		System.out.println();
	}
	
	@Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.println("NettyClientHandler.......");
		System.out.println("NettyClientHandler....... send message = "+e.getMessage());
//		Channels.write(ctx, e.getFuture(),e.getMessage());
       super.writeRequested(ctx, e);
    }
}
