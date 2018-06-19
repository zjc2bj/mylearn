package cn.zjc.learn.arithmetic.sort;

/**
 * Created by chayang on 2017/9/12.
 */
public class SelectionSort {
	/**
	 * 选择排序，也叫简单选择排序 每次从待排序的记录中选择最小的，与第一个位置交换 需要记录每次遍历的最小值和最小值的位置
	 */
	private static void selectionSort(int[] a) {
		if (a.length <= 1) {
			return;
		}
		int position;// position记录最小值的位置
		for (int i = 0; i < a.length; i++) {
			position = i;
			for (int j = i + 1; j < a.length; j++) {
				if (a[j] < a[position]) {
					position = j;
				}
			}
			if (position != i) {
				int temp = a[i];
				a[i] = a[position];
				a[position] = temp;
			}
		}
	}

	/**
	 * 选择排序
	 * 
	 * @param a
	 */
	private static void selectionSort2(int[] a) {
		for (int i = 0; i < a.length; i++) {
			int min = a[i];
			int posix = -1;
			for (int j = i; j < a.length; j++) {
				if (a[j] < min) {
					min = a[j];
					posix = j;
				}
			}
			if (posix > i) {
				int temp = a[posix];
				a[posix] = a[i];
				a[i] = temp;
			}
		}
	}

	public static void main(String[] args) {
		int[] a1 = { 5, 4, 3, 2, 1 };
		selectionSort(a1);
		for (int a : a1) {
			System.out.print(a);
		}
		System.out.println();
		int[] a2 = new int[] { 2, 3, 7, 1, 4, 6, 5 };
		selectionSort2(a2);
		for (int a : a2) {
			System.out.print(a);
		}

	}
}
