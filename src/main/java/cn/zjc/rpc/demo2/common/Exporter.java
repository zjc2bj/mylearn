package cn.zjc.rpc.demo2.common;

import cn.zjc.rpc.demo2.common.server.ServerInvoker;

public class Exporter<T> {
	private final ServerInvoker<T> invoker;

	public Exporter(ServerInvoker<T> invoker) {
		this.invoker = invoker;
	}

	public ServerInvoker<T> getInvoker() {
		return invoker;
	}
}
