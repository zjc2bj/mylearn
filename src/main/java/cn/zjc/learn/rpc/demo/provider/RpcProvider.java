package cn.zjc.learn.rpc.demo.provider;

import cn.zjc.learn.rpc.demo.api.HelloService;
import cn.zjc.learn.rpc.demo.api.RpcFramework;

public class RpcProvider {
	public static void main(String[] args) throws Exception {  
        HelloService service = new HelloServiceImpl();  
        RpcFramework.export(service, 1234);  
    }  
}
