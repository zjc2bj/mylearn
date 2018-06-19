package cn.zjc.learn.arithmetic.sort;

/**
 * <pre>
 * 插入排序，也叫直接插入排序
 * 每次从待排序的序列中，选择第一个，然后插入前面的有序序列中
 * 比较过程中，要挪动数组，不然以后插入的时候还需要再次挪动
 * </pre>
 * 
 * @author zhujunchao
 */
public class InsertionSort {
	/**
	 * 从第一个开始 每插入一个值 和前面所有的进行比较 若小于 则替换
	 */
	private static void insertionSort(int[] a) {
		if (a == null || a.length <= 1) {
			return;
		}

		for (int i = 1; i < a.length; i++) {// 遍历 插入的值 一个个插入
			for (int j = i; j > 0; j--) {
				// 插入的值 需要和此节点之前的所有元素 依次进行比较
				if (a[j] < a[j - 1]) {// 小于则替换
					int temp = a[j - 1];
					a[j - 1] = a[j];
					a[j] = temp;
				} else {
					break;// 大于则终止当前循环 继续执行下一个节点插入(因为前面的数组已排序)
				}
			}
		}
	}

	public static void main(String[] args) {
		int[] a1 = { 8, 2, 3, 1, 6, 4, 5, 7 };
		insertionSort(a1);
		for (int a : a1) {
			System.out.print(a);
		}
	}
}
