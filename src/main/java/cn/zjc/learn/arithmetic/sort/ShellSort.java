package cn.zjc.learn.arithmetic.sort;

/**
 * Created by chayang on 2017/9/13.
 */
public class ShellSort {
    /**
     * 希尔排序，也叫缩小增量排序
     * 对步长组成的子序列直接插入排序，步长依次减小，直到为1
     */

    /**
     * 算法思路：
     * 分为三个for循环
     * 第一层for循环，是确定步长的，最初步长为数组长度的1/2，之后每次变为原来的1/2
     * 第二层for循环，对从步长开始的元素开始，直到数组最后元素，依次进行直接插入排序
     * 第三层for循环，是在直接插入排序时，对元素前面的元素进行遍历
     */
    private static void shellSort(int[] a) {
        for (int h = a.length / 2; h > 0; h = h / 2) {
            for (int i = h; i < a.length; i++) {
                int temp = a[i];
                int j;
                for (j = i - h; j >= 0; j = j - h) {
                    if (a[j] > temp) {
                        a[j + h] = a[j];
                    } else {
                        break;
                    }
                }
                a[j + h] = temp;
            }
        }
    }

    public static void main(String[] args) {
        int[] a1 = {8, 2, 3, 1, 6, 4, 5, 7};
        shellSort(a1);
        for (int a : a1) {
            System.out.print(a);
        }
    }
}
