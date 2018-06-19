package cn.zjc.learn.arithmetic.sort;

/**
 * Created by chayang on 2017/9/13.
 */
public class MergeSort {
    /**
     * 合并的时候，先把数组中的内容复制出去，然后再一次比较两个新数组，插入原来的数组
     */
    private static void merge(int[] a, int low, int middle, int high) {

        int[] first = new int[middle - low + 1];
        int[] second = new int[high - middle];

        for (int i = low; i <= middle; i++) {
            first[i - low] = a[i];
        }
        for (int i = middle + 1; i <= high; i++) {
            second[i - middle - 1] = a[i];
        }

        //之后对两个数组遍历，依次插入原来的数组
        int i = 0;
        int j = 0;
        int r = low;
        while (i < first.length && j < second.length) {
            if (first[i] < second[j]) {
                a[r] = first[i];
                i++;
                r++;
            } else {
                a[r] = second[j];
                j++;
                r++;
            }
        }
        while (i < first.length) {
            a[r] = first[i];
            i++;
            r++;
        }
        while (j < second.length) {
            a[r] = second[j];
            j++;
            r++;
        }
    }

    private static void mergeSort(int[] a, int low, int high) {
        if (low >= high) {
            return;
        }
        int middle = (low + high) / 2;
        mergeSort(a, low, middle);
        mergeSort(a, middle + 1, high);
        merge(a, low, middle, high);
    }

    public static void main(String[] args) {
        int[] a1 = {8, 2, 3, 1, 6, 4, 5, 7};
        mergeSort(a1, 0, a1.length -1);
        for (int a : a1) {
            System.out.print(a);
        }
    }


}
