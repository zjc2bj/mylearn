package cn.zjc.learn.arithmetic.interview;

import java.util.Arrays;

/**
 * 字符 a b c 排列组合
 * 
 * @author zhujunchao
 *
 */
public class CharDemo {

	public static void main(String[] args) {
		String[] params = new String[] { "a", "b", "c" };
		permutationAndCombination(params, 0);
	}

	public static void permutationAndCombination(String[] params, int level) {
		for (int i = level; i < params.length; i++) {
			String[] swap = swap(params, level, i);
			print(swap);
			if (level < swap.length - 1) {
				permutationAndCombination(swap, level + 1);
			}
		}
	}

	public static String[] swap(String[] params, int idx1, int idx2) {
		params = Arrays.copyOf(params, params.length);
		String temp = params[idx1];
		params[idx1] = params[idx2];
		params[idx2] = temp;
		return params;
	}

	public static void print(String[] params) {
		for (String string : params) {
			System.out.print(string);
		}
		System.out.println();
	}
}
