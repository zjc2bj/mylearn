package cn.zjc.learn.arithmetic.linkedlist;

public class LinkNode {
	public String value;
	public LinkNode next;

	public LinkNode() {
	}

	public LinkNode(String... args) {
		LinkNode tail = null;
		for (int i = args.length - 1; i >= 0; i--) {
			String value = args[i];
			LinkNode node = new LinkNode();
			node.value = value;
			if(tail == null) {
				tail = node;
			}else {
				node.next = tail;
				tail = node;
			}
		}
		this.value = tail.value;
		this.next = tail.next;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		LinkNode temp = this;
		do {
			buffer.append(temp.value+",");
			temp = temp.next;
		} while (temp != null);
		if(buffer.length()>1) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		return buffer.toString();
	}
	
	public static void main(String[] args) {
		LinkNode linkNode = new LinkNode("a","b","c","d","e","f");
		System.out.println(linkNode);
	}
}
