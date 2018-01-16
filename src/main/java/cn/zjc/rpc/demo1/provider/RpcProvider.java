package cn.zjc.rpc.demo1.provider;

import cn.zjc.rpc.demo1.api.HelloService;
import cn.zjc.rpc.demo1.api.RpcFramework;

public class RpcProvider {
	public static void main(String[] args) throws Exception {  
        HelloService service = new HelloServiceImpl();  
        RpcFramework.export(service, 1234);  
    }  
}
