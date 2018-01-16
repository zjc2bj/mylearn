package cn.zjc.rpc.demo2.common.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.zjc.rpc.demo2.common.vo.RpcResult;

public class ServerInvoker<T> {
	private final T proxy;

	private final Class<T> type;

	public ServerInvoker(T proxy, Class<T> type) {
		this.proxy = proxy;
		this.type = type;
	}

	/**执行服务端对应的方法*/
	public RpcResult invoke(String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Exception {
		try {
			return new RpcResult(doInvoke(proxy, methodName, parameterTypes, arguments));
		} catch (InvocationTargetException e) {
			return new RpcResult(e.getTargetException());
		} catch (Throwable e) {
			throw new RuntimeException(
					"Failed to invoke remote proxy method " + methodName + ", cause: " + e.getMessage(), e);
		}
	}

	private Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable {
		Method method = proxy.getClass().getMethod(methodName, parameterTypes);
		return method.invoke(proxy, arguments);
	}

	public String toString() {
		return type.getTypeName();
	}

}
