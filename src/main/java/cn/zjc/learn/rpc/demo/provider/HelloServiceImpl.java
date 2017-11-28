package cn.zjc.learn.rpc.demo.provider;

import cn.zjc.learn.rpc.demo.api.HelloService;

public class HelloServiceImpl implements HelloService {
	public String hello(String name) {
		return "Hello " + name;
	}
}
