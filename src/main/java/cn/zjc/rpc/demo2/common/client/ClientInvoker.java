package cn.zjc.rpc.demo2.common.client;

import cn.zjc.rpc.demo2.common.vo.Response;
import cn.zjc.rpc.demo2.common.vo.RpcRequest;
import cn.zjc.rpc.demo2.common.vo.RpcResult;

public class ClientInvoker<T> {
	private NettyClient client;
	private Class<T> type;

	public ClientInvoker(NettyClient client, Class<T> serviceType) {
		this.client = client;
		this.type = serviceType;
	}

	public Object doInvoke(String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable {
		try {
			boolean isAsync = false;
			boolean isOneway = false;
			int timeout = 1000;
			RpcRequest request = new RpcRequest(methodName, parameterTypes, arguments, null);
			request.setAttachment("path",getInterface().getName());
			if (isOneway) {
				// RpcContext.getContext().setFuture(null);
				// return new RpcResult();
			} else if (isAsync) {// 异步调用
				// ResponseFuture future = currentClient.request(inv, timeout);
				// RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
				// return new RpcResult();
			} else {// 同步调用
				// RpcContext.getContext().setFuture(null);
				// return (Result) currentClient.request(inv, timeout).get();
			}
			Response response = (Response) client.sendMessage(request, isOneway).get();
			RpcResult result = (RpcResult) response.getResult();
			
			if(result.getException() != null) {
				throw result.getException();
			}
			return result.getValue();
		} catch (Exception e) {
			throw new RuntimeException("Invoke remote method timeout. method: " + methodName + ", provider: "
					+ client.getUrl() + ", cause: " + e.getMessage(), e);
		}
	}

	public Class<T> getInterface() {
		return type;
	}
}
