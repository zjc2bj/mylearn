package cn.zjc.learn.lang;

/**
 * 可用于组合状态,如支付方式
 */
public class WeiYu {
	public static void main(String[] args) {
		System.out.println(1 << 0);
		System.out.println(1 << 2);
		System.out.println(1 << 3);
		System.out.println(1 << 4);
		System.out.println(1 | 4 | 8 | 16);
		
		System.out.println(24 & 4);//不包含
		System.out.println(24 & 8);//包含
		System.out.println(24 & 16);//包含
	}
}
