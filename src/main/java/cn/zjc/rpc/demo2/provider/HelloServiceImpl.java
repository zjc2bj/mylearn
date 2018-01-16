package cn.zjc.rpc.demo2.provider;

import cn.zjc.rpc.demo2.api.HelloService;

public class HelloServiceImpl implements HelloService {
	public String hello(String name) {
		System.out.println("=============>> Hello " + name);
		return "Hello by server!!!";
	}
}
