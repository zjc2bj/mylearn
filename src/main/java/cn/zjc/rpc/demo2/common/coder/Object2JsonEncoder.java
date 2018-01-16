package cn.zjc.rpc.demo2.common.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.alibaba.fastjson.JSON;

/**
 * Object转化为JSON
 * @author zhujunchao
 *
 */
public class Object2JsonEncoder extends OneToOneEncoder{

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		System.out.println("message to json...");
		if(msg instanceof String){
			return msg;
		}
		return JSON.toJSONString(msg);
	}
}
