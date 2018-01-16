package cn.zjc.rpc.remote.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class StringDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		System.out.println("decode ....");
		StringBuffer strBuff = new StringBuffer();
		while (buffer.readable()) {
			strBuff.append((char) buffer.readByte());
		}
		System.out.println("decode result=" + strBuff);
		return strBuff.toString();
	}

}
