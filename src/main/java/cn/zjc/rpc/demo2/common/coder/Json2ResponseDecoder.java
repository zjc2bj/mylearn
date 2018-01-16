package cn.zjc.rpc.demo2.common.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.alibaba.fastjson.JSONObject;

import cn.zjc.rpc.demo2.common.vo.Response;
import cn.zjc.rpc.demo2.common.vo.RpcRequest;
import cn.zjc.rpc.demo2.common.vo.RpcResult;

public class Json2ResponseDecoder extends OneToOneDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		System.out.println("ResponseDecoder ....");
		if (!(msg instanceof String)) {
			return msg;
		}
		Response res = JSONObject.parseObject((String) msg, Response.class);
		RpcResult rpcResult = JSONObject.parseObject(res.getResult().toString(), RpcResult.class);
		res.setResult(rpcResult);
		return res;
	}
}
