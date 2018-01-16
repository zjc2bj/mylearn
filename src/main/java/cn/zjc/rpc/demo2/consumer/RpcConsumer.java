package cn.zjc.rpc.demo2.consumer;

import cn.zjc.rpc.demo2.api.HelloService;
import cn.zjc.rpc.demo2.common.Protocol;
import cn.zjc.rpc.demo2.common.client.ClientInvoker;

public class RpcConsumer {
	public static void main(String[] args) throws Exception {  
		Protocol protocol = new Protocol();
		ClientInvoker<HelloService> refer = protocol.refer(HelloService.class );
		HelloService proxy = protocol.getProxy(refer);
		String hello = proxy.hello(" I am client !!!");
		System.out.println(" client rsp = "+ hello);
    }  
}
