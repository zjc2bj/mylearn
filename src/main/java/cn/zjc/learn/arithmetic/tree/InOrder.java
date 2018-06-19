package cn.zjc.learn.arithmetic.tree;

import java.util.Stack;

/**
 * Created by chayang on 2017/9/19.
 * 中序遍历二叉树
 */
public class InOrder {

    /**
     * 中序遍历二叉树，非递归
     * 算法思路：
     * 一直遍历左子树，不停的入栈，一直到左子树为空
     * 然后节点出栈，打印，再查找节点右子树
     * 对右子树的子节点，依次进行同样的遍历
     *
     * 和先序遍历基本一样，区别在于：
     * 先序遍历时，是入栈的时候打印，而中序是出栈的时候打印
     */
    private static void inOrder(Node root) {
        if (root == null) {
            return;
        }
        Stack<Node> stack = new Stack<Node>();
        Node node = root;
        while (!stack.isEmpty() || node != null) {
            if (node != null) {
                stack.push(node);
                node = node.getLeftChild();
            } else {
                node = stack.pop();
                System.out.print(node.getData() + ", ");
                node = node.getRightChild();
            }
        }
    }

    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        System.out.println("中序递归遍历:");
        myBinaryTree.inOrder(myBinaryTree.getRoot());
        System.out.println();
        System.out.println("中序非递归遍历:");
        inOrder(myBinaryTree.getRoot());
    }
}
