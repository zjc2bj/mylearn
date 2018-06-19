package cn.zjc.learn.arithmetic.linkedlist;

/**
 * 链表反转
 * 
 * @author zhujunchao
 */
public class InversionLink {
	/**
	 * 递归
	 * 
	 * @param node
	 * @return
	 */
	@Deprecated
	public static LinkNode invers(LinkNode node) {
		if (node.next == null) {
			return node;
		}

		LinkNode temp = invers(node.next);
		// 换位置
		node.next = null;

		LinkNode last = temp;// 取最后一个节点
		while (last.next != null) {
			last = last.next;
		}
		last.next = node;// 将当前节点 放到最后一个节点后面
		return temp;
	}

	/**
	 * 递归
	 * 
	 * @param node
	 * @return
	 */
	public static LinkNode reverse3(LinkNode node) {
		if (node.next == null)
			return node;
		LinkNode next = node.next;
		node.next = null;
		LinkNode re = reverse3(next);
		next.next = node;
		return re;
	}
	
	/**
	 * 遍历
	 * 
	 * @param node
	 * @return
	 */
	public static LinkNode reverse(LinkNode node) {
		LinkNode prev = null;
		LinkNode now = node;
		while (now != null) {
			LinkNode next = now.next;
			now.next = prev;
			prev = now;
			now = next;
		}

		return prev;
	}

	/**
	 * 遍历
	 * 
	 * @param node
	 * @return
	 */
	public static LinkNode reverse2(LinkNode node) {
		LinkNode head = node;
		LinkNode next = node.next;
		LinkNode temp = node.next.next;
		
		node.next = null;
		next.next = node;
		head = next;
		
		while(temp != null) {
			LinkNode temp2 = temp.next;
			temp.next = head;
			head = temp;
			temp = temp2;
		}
		
		return head;
	}

	public static void main(String[] args) {
		LinkNode linkNode = new LinkNode("a", "b", "c", "d");
		System.out.println(reverse2(linkNode));
	}
}
