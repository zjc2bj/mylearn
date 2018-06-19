package cn.zjc.learn.arithmetic.tree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chayang on 2017/10/25.
 * 求二叉树的宽度
 */
public class TreeWidth {

    public static int getTreeWidth(Node root) {
        if (root == null) {
            return 0;
        }
        int maxWidth = 1;//记录最大宽度
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(root);
        int depth = 0;
        while (!queue.isEmpty()) {
            depth++;
            int length = queue.size();
            int currentWidth = 0;
            while (length > 0) {
                Node node = queue.poll();
                if (node.getLeftChild() != null) {
                    queue.add(node.getLeftChild());
                    currentWidth++;
                }
                if (node.getRightChild() != null) {
                    queue.add(node.getRightChild());
                    currentWidth++;
                }
                length--;
            }
            if (currentWidth > maxWidth) {
                maxWidth = currentWidth;
            }
        }
        System.out.println("深度：" + depth);
        return maxWidth;
    }

    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        int width = getTreeWidth(myBinaryTree.getRoot());
        System.out.println(width);
    }

}
