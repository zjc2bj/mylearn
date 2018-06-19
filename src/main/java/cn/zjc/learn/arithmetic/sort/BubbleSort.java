package cn.zjc.learn.arithmetic.sort;

public class BubbleSort {
	/**
	 * <pre>
	 * 冒泡排序 两两比较，如果前面比后面大，则交换。 
	 * 小的上浮，大的下沉 
	 * 第一轮循环完成之后，最大的数在最后，第二轮之后，第二大的数在倒数第二 
	 * TODO时间复杂度
	 * </pre>
	 * @author zhujunchao
	 */
	private static void bubbleSort2(int[] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a.length - i - 1; j++) {// 一个循环 最大值放置到末尾 -1是两两比较 只能取到第一个数字位置（第二个会+1）
				if (a[j] > a[j + 1]) {
					int temp = a[j];
					a[j] = a[j + 1];
					a[j + 1] = temp;
				}
			}
			println(a);
		}
	}

	public static void println(int[] a) {
		for (int value : a) {
			System.out.print(value);
		}
		System.out.println();
	}

	public static void main(String[] args) {
		int[] a1 = { 8, 2, 3, 1, 6, 4, 5, 7 };
		bubbleSort2(a1);
		for (int a : a1) {
			System.out.print(a);
		}
	}
}
