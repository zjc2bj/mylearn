package cn.zjc.learn.arithmetic.sort;

/**
 * 快速排序
 * 
 * <pre>
 * 采用分而治之的思想 取头元素为参照，第一次排序后，分成两部分，前面的都比参照数小，后面的都比参照数大
 * 以此类推，对参照数前后的两部分继续进行快速排序
 * </pre>
 * 
 * {@link https://visualgo.net/zh/sorting}
 * 
 * @author zhujunchao
 */
public class QuickSort {
	/**
	 * <pre>
	 * 1.选取第一个数字start 
	 * 2.余下的数字 与start比较 如果小于 则替换到start后面 并记录位置index（分割点） 每替换一次 index+1 
	 * 3.如果分割点位置>start 则说明有小于的值 替换start和index的值 --此时 index前的只 都小于index的值 。。。
	 * 4.从index截取两块 分别递归
	 * </pre>
	 * 
	 */
	private static void quickSort(int[] a, int start, int end) {
		if (start >= end) {
			return;
		}
		int splitValue = a[start];
		int index = start;
		for (int i = start + 1; i <= end; i++) {// 含头和尾
			if (a[i] < splitValue) {
				index++;
				int temp = a[index];
				a[index] = a[i];
				a[i] = temp;
			}
		}
		if (index > start) {// index位置元素 和 start互换
			int temp = a[index];
			a[index] = a[start];
			a[start] = temp;
		}
		// 从index部分 再拆分数组
		quickSort(a, start, index - 1);
		quickSort(a, index + 1, end);
	}

	public static void quickSort2(int[] arr, int start, int end) {
		if (arr == null || arr.length <= 1) {
			return;
		}
		if (start >= end) {
			return;
		}
		int splitValue = arr[start];
		int splitIndex = start;// 大小分界点
		for (int i = start + 1; i <= end; i++) {
			if (arr[i] < splitValue) {
				splitIndex++;
				swap(arr, i, splitIndex);
			}
		}
		if(splitIndex > start) {
			swap(arr,splitIndex,start);
		}
		quickSort2(arr,start,splitIndex-1);
		quickSort2(arr,splitIndex+1,end);
	}

	private static void swap(int[] arr, int i, int splitIndex) {
		int temp = arr[i];
		arr[i] = arr[splitIndex];
		arr[splitIndex] = temp;
	}

	public static void main(String[] args) {
		int[] a1 = { 4, 8, 2, 6, 1, 3, 5, 7 };
		quickSort2(a1, 0, 7);
		for (int a : a1) {
			System.out.print(a);
		}
	}
}
