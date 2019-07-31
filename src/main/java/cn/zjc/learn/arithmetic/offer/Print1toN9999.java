package cn.zjc.learn.arithmetic.offer;

/**
 * 从1打印到最大的n位数 如 n=3 则打印 1...999
 * 
 * @author zhujunchao02
 */
public class Print1toN9999 {

	public static void main(String[] args) {
		int n = 3;
		
		int[] arr = new int[n];
		print(arr, n-1);
	}

	public static void print(int[] arr, int arrIndex) {
		for (int i = 0; i <= 9; i++) {
			arr[arrIndex] = i;
			if(arrIndex == 0) {
				println(arr);
			}else {
				print(arr, arrIndex - 1);
			}
		}
	}

	private static void println(int[] arr) {
		for (int i = arr.length; i > 0; i--) {
			System.out.print(arr[i-1]);
		}
		System.out.println();

	}

}
