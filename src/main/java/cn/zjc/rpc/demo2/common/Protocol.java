package cn.zjc.rpc.demo2.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.zjc.rpc.demo2.common.client.ClientInvoker;
import cn.zjc.rpc.demo2.common.client.NettyClient;
import cn.zjc.rpc.demo2.common.coder.HeaderHandler;
import cn.zjc.rpc.demo2.common.server.NettyServer;
import cn.zjc.rpc.demo2.common.server.ServerInvoker;

public class Protocol {
	private final Map<String, NettyServer> serverMap = new ConcurrentHashMap<String, NettyServer>(); // <host:port,Exchanger>
	public final static Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>();

//	private ServerHandler serverHandler = new ServerHandler() {
//		/** 读消息 */
//		@Override
//		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//			// 处理消息--调用后台
//			System.out.println("server received...." + e.getMessage());
//
//			if (e.getMessage() instanceof RpcRequest) {
//				RpcRequest request = (RpcRequest) e.getMessage();
//				System.out.println("server received = " + JSON.toJSONString(request));
//				String path = request.getAttachments().get(Constants.PATH_KEY);
//				Exporter<?> exporter = exporterMap.get(path);
//				ServerInvoker<?> invoker = exporter.getInvoker();
//				RpcResult rpcResult = invoker.invoke(request.getMethodName(), request.getParameterTypes(), request.getArguments());
//				
//				ctx.getChannel().write(rpcResult);
//			}else {
//				super.messageReceived(ctx, e);
//			}
//		}
//	};

	public <T> Exporter<T> export(T instance, Class<T> type) throws Exception {
		ServerInvoker<T> invoker = new ServerInvoker<T>(instance, type);
		String key = invoker.toString();
		Exporter<T> exporter = new Exporter<>(invoker);
		exporterMap.put(key, exporter);

		openServer(8080);
		return exporter;
	}

	public <T> ClientInvoker<T> refer(Class<T> type) throws Exception {
		return new ClientInvoker<>(new NettyClient("localhost", 8080, new HeaderHandler()), type);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxy(final ClientInvoker<T> invoker) {
		Class<?>[] interfaces = new Class<?>[] { invoker.getInterface() };
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String methodName = method.getName();
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (method.getDeclaringClass() == Object.class) {
							return method.invoke(invoker, args);
						}
						if ("toString".equals(methodName) && parameterTypes.length == 0) {
							return invoker.toString();
						}
						if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
							return invoker.hashCode();
						}
						if ("equals".equals(methodName) && parameterTypes.length == 1) {
							return invoker.equals(args[0]);
						}
						return invoker.doInvoke(methodName, parameterTypes, args);
					}
				});
	}

	private void openServer(int port) {
		// find server.
		// client 也可以暴露一个只有server可以调用的服务。
		NettyServer server = serverMap.get(port + "");
		if (server == null) {
			serverMap.put(port + "", new NettyServer(port, new HeaderHandler()));
		}
	}
}
