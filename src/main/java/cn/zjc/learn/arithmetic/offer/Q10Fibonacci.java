package cn.zjc.learn.arithmetic.offer;

/**
 * 数列
 * 
 * <pre>
 * f(n) = 	0				n=0
 * f(n) = 	1				n=1
 * f(n) = 	f(n-1)+f(n-2) 	n>1
 * </pre>
 */
public class Q10Fibonacci {

	// 递归
	public int recursion(int n) {
		if(n==0)
			return 0;
		if(n==1)
			return 1;
		return recursion(n-1) + recursion(n-2);
	}
	
	// 非递归 记录计算过的值 从1到n计算
	public int Loop(int n) {
		if(n==0)
			return 0;
		if(n==1)
			return 1;
		
		int n_2 = 0;
		int n_1 = 1;
		int curr = 0;
		for(int i=2;i<=n;i++) {
			curr = n_1 + n_2;
			n_2 = n_1; // f(n-1)的值副给f(n-2)
			n_1 = curr; // 计算结果 赋给 f(n-1)
		}
		return curr;
	}
}
