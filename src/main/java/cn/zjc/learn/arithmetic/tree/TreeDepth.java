package cn.zjc.learn.arithmetic.tree;

/**
 * Created by chayang on 2017/10/25.
 * 求二叉树深度
 */
public class TreeDepth {

    public static int getDepth(Node node) {
        if (node == null) {
            return 0;
        }
        int leftDepth = getDepth(node.getLeftChild());
        int rightDepth = getDepth(node.getRightChild());
        return leftDepth > rightDepth ? leftDepth + 1 : rightDepth + 1;

    }

    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        int depth = getDepth(myBinaryTree.getRoot());
        System.out.println(depth);

    }
}
