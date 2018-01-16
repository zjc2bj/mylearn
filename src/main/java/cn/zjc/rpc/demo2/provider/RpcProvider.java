package cn.zjc.rpc.demo2.provider;

import cn.zjc.rpc.demo2.api.HelloService;
import cn.zjc.rpc.demo2.common.Protocol;

public class RpcProvider {
	public static void main(String[] args) throws Exception {  
        HelloService service = new HelloServiceImpl();  
        new Protocol().export(service, HelloService.class);
    }  
}
