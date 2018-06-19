package cn.zjc.learn.arithmetic.tree;

import java.util.Stack;

/**
 * Created by chayang on 2017/9/19.
 * 后序遍历二叉树
 */
public class PostOrder {

    /**
     * 后序遍历二叉树，非递归
     *
     * 算法思路：
     * 后序遍历的顺序为：左子树，右子树，根节点
     * 访问的时候，还是要一直循环遍历左子树，所以还是需要用栈实现，并且还是要从跟节点开始不断的入栈
     * 每次一个节点入栈后，就需要继续遍历这个节点的左右孩子了，但是这个节点要在左右孩子出栈之后才能出栈
     * 所以每次获得这个节点的左右孩子节点时，不能pop该节点，要peek该节点
     *
     * 出栈的时候，如果某节点的左子树出栈打印，那么当前栈顶就是该节点
     * 不过此时该节点不能出栈，因为该节点的右子树还没遍历
     * 所以要再用后序遍历的方式访问这个节点的右子树
     *
     * 需要用last记录上次出栈的节点
     * 在判断的时候：
     * 如果last等于node的左孩子，则说明左孩子已经出栈打印了，那么node也曾经入栈过了，不需要再入栈了
     * 同理，如果last等于node的右孩子，说明右孩子已经出栈打印了，那么左孩子也打印过了，node也曾经入栈了
     *
     * 参考：http://www.cnblogs.com/mukekeheart/p/5694560.html
     *      http://blog.csdn.net/sjf0115/article/details/8645991
     *
     */
    private static void postOrder(Node root) {
        if (root == null) {
            return;
        }
        Stack<Node> stack = new Stack<Node>();
        stack.push(root);
        Node node;
        Node last = null;
        while (!stack.isEmpty()) {
            node = stack.peek();
            if (node.getLeftChild() != null && node.getLeftChild() != last && node.getRightChild() != last) {
                stack.push(node.getLeftChild());
            } else if (node.getRightChild() != null && node.getRightChild() != last) {
                stack.push(node.getRightChild());
            } else {
                last = stack.pop();
                System.out.print(last.getData() + ", ");
            }
        }
    }

    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        System.out.println("后序递归遍历:");
        myBinaryTree.postOrder(myBinaryTree.getRoot());
        System.out.println();
        System.out.println("后序非递归遍历:");
        postOrder(myBinaryTree.getRoot());
    }
}
