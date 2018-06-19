package cn.zjc.learn.arithmetic.tree;


import java.util.Stack;

/**
 * Created by chayang on 2017/9/17.
 * 先序遍历二叉树
 */
public class PreOrder {

    /**
     * 先序遍历二叉树，非递归实现
     * 先遍历根节点，之后再左子树，左子树遍历完成之后再回来遍历右子树
     * 需要把节点存起来，这种结构属于后进先出，所以要用栈
     */

    /**
     * 算法思路：
     * 如果节点不为空，打印，并且入栈
     * 之后遍历左子树，不停的入栈
     * 如果左子树为空，则节点出栈，遍历右子树
     * 节点不为空或者栈不为空时循环
     */
    private static void preOrder(Node root) {
        if (root == null) {
            return;
        }
        Stack<Node> stack = new Stack<Node>();
        Node node = root;
        while (!stack.isEmpty() || node != null) {//需要判断node是否为空，不然就会在根节点右子树那里断掉。
            if (node != null) {
                System.out.print(node.getData() + ", ");
                stack.push(node);
                node = node.getLeftChild();
            } else {
                node = stack.pop();
                node = node.getRightChild();
            }
        }
    }


    /**
     * 非递归遍历
     * 算法思路：
     * 先序遍历的顺序为：根节点，左子树，右子树
     * 既然需要用栈，那右节点要先入栈，然后再左节点入栈
     *
     * 具体步骤：
     * 然后将头结点压入栈中
     * 每次从栈中弹出栈顶元素，记为node，然后打印node节点的值。
     * 如果node右孩子不为空的话，将node的右孩子压入栈中
     * 如果node左孩子不为空的话，将node的做孩子压入栈中
     * 不断重复上面的步骤，直到栈为空
     */
    private static void preOrder2(Node root) {
        if (root == null) {
            return;
        }
        Stack<Node> stack = new Stack<Node>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            System.out.print(node.getData() + ", ");
            if (node.getRightChild() != null) {
                stack.push(node.getRightChild());
            }
            if (node.getLeftChild() != null) {
                stack.push(node.getLeftChild());
            }
        }
    }


    public static void main(String[] args) {
        int[] a = {2, 8, 7, 4, 9, 3, 1, 6, 7, 5};
        MyBinaryTree myBinaryTree = new MyBinaryTree();
        myBinaryTree.buildTree(a);
        System.out.println("先序递归遍历:");
        myBinaryTree.preOrder(myBinaryTree.getRoot());
        System.out.println();
        System.out.println("先序非递归遍历方法1:");
        preOrder(myBinaryTree.getRoot());
        System.out.println();
        System.out.println("先序非递归遍历方法2:");
        preOrder2(myBinaryTree.getRoot());

    }
}
