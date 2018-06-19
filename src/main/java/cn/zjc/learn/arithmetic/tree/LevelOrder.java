package cn.zjc.learn.arithmetic.tree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chayang on 2017/9/17.
 * 层次遍历二叉树
 */
public class LevelOrder {

    public static void levelOrder(Node root) {
        if (root == null) {
            return;
        }
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            System.out.print(node.getData() + ", ");
            if (node.getLeftChild() != null) {
                queue.add(node.getLeftChild());
            }
            if (node.getRightChild() != null) {
                queue.add(node.getRightChild());
            }
        }
    }

    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        levelOrder(myBinaryTree.getRoot());
    }
}
