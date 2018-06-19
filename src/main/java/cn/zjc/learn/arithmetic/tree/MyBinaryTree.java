package cn.zjc.learn.arithmetic.tree;

/**
 * Created by chayang on 2017/9/17.
 * 定义一个二叉树
 */
public class MyBinaryTree {
    private Node root;

    public MyBinaryTree() {
        this.root = null;
    }

    /**
     * 创建一棵树
     *
     * @param a
     */
    public void buildTree(int[] a) {
        for (int i = 0; i < a.length; i++) {
            insertNode(a[i]);
        }
    }

    /**
     * 插入节点
     *
     * @param data
     */
    private void insertNode(int data) {
        Node newNode = new Node(data);

        if (this.root == null) {
            this.root = newNode;
            return;
        }
        Node parent;
        Node current = root;

        while (true) {
            parent = current;
            if (data < current.getData()) {
                current = current.getLeftChild();
                if (current == null) {
                    parent.setLeftChild(newNode);
                    return;
                }
            } else {
                current = current.getRightChild();
                if (current == null) {
                    parent.setRightChild(newNode);
                    return;
                }
            }

        }
    }

    public void preOrder(Node root) {
        if (root == null) {
            return;
        }
        System.out.print(root.getData() + ", ");
        preOrder(root.getLeftChild());
        preOrder(root.getRightChild());
    }

    public void inOrder(Node root) {
        if (root == null) {
            return;
        }
        inOrder(root.getLeftChild());
        System.out.print(root.getData() + ", ");
        inOrder(root.getRightChild());
    }

    public void postOrder(Node root) {
        if (root == null) {
            return;
        }
        postOrder(root.getLeftChild());
        postOrder(root.getRightChild());
        System.out.print(root.getData() + ", ");
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
