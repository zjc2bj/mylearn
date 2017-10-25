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
		System.out.println(1 << 5);
		System.out.println("############");
		System.out.println(1 | 4 | 8 | 16 |32);
		System.out.println("############");
		System.out.println(61 & 3);//不包含
		System.out.println(61 & 7);//包含
		System.out.println(61 & 16);//包含
	}
}
