package cn.zjc.learn.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlDemo {

	public static void main(String[] args) throws UnsupportedEncodingException {
		String s = "%E6%9C%8D%E5%8A%A1%E5%90%8D%E7%A7%B0%2F%E6%9C%8D%E5%8A%A1%E5%9B%BE%E6%96%87%E6%8F%8F%E8%BF%B0%2F%E6%9C%8D%E5%8A%A1%E7%AE%80%E4%BB%8B%E8%A6%81%E6%B1%82%E6%98%AF%E4%BB%80%E4%B9%88%EF%BC%9F";
		String decode = URLDecoder.decode(s, "utf-8");
		System.out.println(decode);
		String req = "服务名称/服务图文描述/服务简介要求是什么？";
		String encode = URLEncoder.encode(req, "utf-8");
		System.out.println(encode.equalsIgnoreCase(s));
	}
}
