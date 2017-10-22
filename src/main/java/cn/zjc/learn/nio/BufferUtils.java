package cn.zjc.learn.nio;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BufferUtils {

	public static String toString(ByteBuffer bf) throws Exception {
		StringBuffer buffer = new StringBuffer();
		Class<Buffer> class1 = (Class<Buffer>) bf.getClass().getSuperclass().getSuperclass();
		Field[] declaredFields = class1.getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.getName().equals("address"))
				continue;
			field.setAccessible(true);
			Object value = field.get(bf);
			buffer.append(field.getName()).append(":").append(value).append(",");
		}
		buffer.append("remain:" + bf.remaining());
		return buffer.toString();
	}

	public static void print(ByteBuffer bf, String pre) throws Exception {
		System.out.println((pre == null ? "" : pre) + toString(bf));
	}

}
