package cn.zjc.rpc.demo2.common.vo;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class RpcRequest {

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Map<String, String> attachments;

	public RpcRequest() {
	}

	public RpcRequest(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		this.arguments = arguments == null ? new Object[0] : arguments;
		this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Map<String, String> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, String> attachments) {
		this.attachments = attachments;
	}
	
	public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        attachments.put(key, value);
    }
	
	public static void main(String[] args) {
		RpcRequest request = new RpcRequest();
		request.setMethodName("sayHello");
		Class clazz = Integer.class;
		request.setParameterTypes(new Class<?>[] {clazz});
		request.setArguments(new Integer[] {10});
		request.setAttachments(new HashMap<>());
		
		String jsonString = JSON.toJSONString(request);
		System.out.println(jsonString);
		RpcRequest req2 = JSONObject.parseObject(jsonString,RpcRequest.class);
		System.out.println(req2);
	}
}
