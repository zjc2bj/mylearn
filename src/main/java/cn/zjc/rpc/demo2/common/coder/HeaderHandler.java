package cn.zjc.rpc.demo2.common.coder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.zjc.rpc.demo2.common.Constants;
import cn.zjc.rpc.demo2.common.DefaultFuture;
import cn.zjc.rpc.demo2.common.Exporter;
import cn.zjc.rpc.demo2.common.Protocol;
import cn.zjc.rpc.demo2.common.server.ServerInvoker;
import cn.zjc.rpc.demo2.common.vo.Request;
import cn.zjc.rpc.demo2.common.vo.Response;
import cn.zjc.rpc.demo2.common.vo.RpcRequest;
import cn.zjc.rpc.demo2.common.vo.RpcResult;

public class HeaderHandler extends SimpleChannelHandler {

	/**
	 * Invoked when a message object (e.g: {@link ChannelBuffer}) was received from
	 * a remote peer.
	 */
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object message = e.getMessage();
		try {
			if (message instanceof Request) {
				Request request = (Request) message;
				System.out.println("server received = " + JSON.toJSONString(request));
				Response res = new Response(request.getId(), request.getVersion());

				Object data = request.getData();
				RpcRequest rpcRequest = (RpcRequest) data;

				String path = rpcRequest.getAttachments().get(Constants.PATH_KEY);
				Exporter<?> exporter = Protocol.exporterMap.get(path);
				ServerInvoker<?> invoker = exporter.getInvoker();
				RpcResult rpcResult = invoker.invoke(rpcRequest.getMethodName(), rpcRequest.getParameterTypes(),
						rpcRequest.getArguments());

				res.setStatus(Response.OK);
				res.setResult(rpcResult);

				ctx.getChannel().write(res);
			} else if (message instanceof Response) {
				Response response = (Response) message;
				DefaultFuture.received(ctx.getChannel(), response);
			} else if (message instanceof String) {
				// if (isClientSide(channel)) {
				// Exception e = new Exception("Dubbo client can not supported string message: "
				// + message
				// + " in channel: " + channel + ", url: " + channel.getUrl());
				// logger.error(e.getMessage(), e);
				// } else {
				// String echo = handler.telnet(channel, (String) message);
				// if (echo != null && echo.length() > 0) {
				// channel.send(echo);
				// }
				// }
			} else {
				// handler.received(exchangeChannel, message);
			}
		} finally {

		}
		// ctx.sendUpstream(e);
	}

	/**
	 * Invoked when {@link Channel#write(Object)} is called.
	 */
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.println(" writeRequested........");
		ctx.sendDownstream(e);
	}
}
