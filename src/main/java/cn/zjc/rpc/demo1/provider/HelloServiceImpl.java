package cn.zjc.rpc.demo1.provider;

import cn.zjc.rpc.demo1.api.HelloService;

public class HelloServiceImpl implements HelloService {
	public String hello(String name) {
		return "Hello " + name;
	}
}
