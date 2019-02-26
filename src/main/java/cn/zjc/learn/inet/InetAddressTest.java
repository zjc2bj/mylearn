package cn.zjc.learn.inet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.fastjson.JSON;

public class InetAddressTest {

	public static void main(String[] args) {
		try {
			InetAddress byName = InetAddress.getByName("www.baidu.com");
			System.out.println(JSON.toJSON(byName));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
