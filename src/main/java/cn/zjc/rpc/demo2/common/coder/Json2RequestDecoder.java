package cn.zjc.rpc.demo2.common.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.zjc.rpc.demo2.common.vo.Request;
import cn.zjc.rpc.demo2.common.vo.RpcRequest;

public class Json2RequestDecoder extends OneToOneDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		System.out.println("RequestDecoder ....");
		if (!(msg instanceof String)) {
			return msg;
		}
		Request request = JSONObject.parseObject((String) msg, Request.class);
		RpcRequest rpcRequest = JSONObject.parseObject(request.getData().toString(), RpcRequest.class);
		request.setData(rpcRequest);
		return request;
	}
	
	public static void main(String[] args) {
		String req = "{\"data\":{\"attachments\":{\"path\":\"cn.zjc.rpc.demo2.api.HelloService\"},\"parameterTypes\":[\"java.lang.String\"],\"methodName\":\"hello\",\"arguments\":[\" I am client !!!\"]},\"id\":0,\"version\":\"2.0.0\"}";
		Request request = JSONObject.parseObject( req, Request.class);
		System.out.println(request);
	}
	
	public String encode(Request request) {
		return JSON.toJSONString(request);
	}

}
