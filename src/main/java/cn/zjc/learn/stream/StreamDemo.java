package cn.zjc.learn.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamDemo {

	// 构造流的几种常见方法
	public void create() {
		// 1. Individual values
		Stream<String> stream = Stream.of("a", "b", "c");
		// 2. Arrays
		String[] strArray = new String[] { "a", "b", "c" };
		stream = Stream.of(strArray);
		stream = Arrays.stream(strArray);
		// 3. Collections
		List<String> list = Arrays.asList(strArray);
		stream = list.stream();

		// 数值流的构造
		IntStream.of(new int[] { 1, 2, 3 }).forEach(System.out::println);
		IntStream.range(1, 3).forEach(System.out::println);
		IntStream.rangeClosed(1, 3).forEach(System.out::println);

	}

	// 流转换为其它数据结构
	public void convert() {
		Stream<String> stream = Stream.of("a", "b", "c");
		// 1. Array
		String[] strArray1 = stream.toArray(String[]::new);
		// 2. Collection
		List<String> list1 = stream.collect(Collectors.toList());
		List<String> list2 = stream.collect(Collectors.toCollection(ArrayList::new));
		Set<String> set1 = stream.collect(Collectors.toSet());
		Stack stack1 = stream.collect(Collectors.toCollection(Stack::new));
		// 3. String
		String str = stream.collect(Collectors.joining()).toString();
	}

	// 平方数
	private void demo2() {
		List<Integer> nums = Arrays.asList(1, 2, 3, 4);
		List<Integer> squareNums = nums.stream().map(n -> n * n).collect(Collectors.toList());
	}

	// 一对多
	public void demo3() {
		Stream<List<Integer>> inputStream = Stream.of(Arrays.asList(1), Arrays.asList(2, 3), Arrays.asList(4, 5, 6));
		Stream<Integer> outputStream = inputStream.flatMap((childList) -> childList.stream());
	}

	// 把整篇文章中的全部单词挑出来
	private void demo4() {
		// List<String> output =
		// reader.lines().flatMap(line -> Stream.of(line.split(REGEXP)))
		// .filter(word -> word.length() > 0).collect(Collectors.toList());
	}
	
	// peek 对每个元素执行操作并返回一个新的 Stream
	private void demoPeek() {
		Stream.of("one", "two", "three", "four")
		 .filter(e -> e.length() > 3)
		 .peek(e -> System.out.println("Filtered value: " + e))
		 .map(String::toUpperCase)
		 .peek(e -> System.out.println("Mapped value: " + e))
		 .collect(Collectors.toList());
	}

	private void demo5() {
		String strA = " abcd ", strB = null;
		print(strA);
		print("");
		print(strB);
		getLength(strA);
		getLength("");
		getLength(strB);
	}

	public static void print(String text) {
		// Java 8
		Optional.ofNullable(text).ifPresent(System.out::println);
		// Pre-Java 8
		if (text != null) {
			System.out.println(text);
		}
	}

	public static int getLength(String text) {
		// Java 8
		return Optional.ofNullable(text).map(String::length).orElse(-1);
		// Pre-Java 8
		// return if (text != null) ? text.length() : -1;
	}

	// 1. 排序、取值
	public void demo1() {
	}
}
