package cn.zjc.learn.concurrent.queue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

import cn.zjc.learn.concurrent.ReentrantLock.ReentrantLock;

/**
 * 单向链表 -- next
 * 
 * @author zhujunchao
 */
public class LinkedBlockingQueueMock<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
	private static final long serialVersionUID = -6648117528678239576L;

	static class Node<E> {
		E item;
		Node<E> next;

		Node(E x) {
			item = x;
		}
	}

	private final int capacity;// 容量 Integer.MAX_VALUE if none
	private final AtomicInteger count = new AtomicInteger();// 当前节点数量--长度

	transient Node<E> head;// 表头 Invariant: head.item == null --队列实际上从head.next开始有值的
	private transient Node<E> last;// 表尾 Invariant: last.next == null

	private final ReentrantLock takeLock = new ReentrantLock();// by take, poll, etc
	/** Wait queue for waiting takes */
	private final Condition notEmpty = takeLock.newCondition();
	private final ReentrantLock putLock = new ReentrantLock();// by put, offer, etc
	/** Wait queue for waiting puts */
	private final Condition notFull = putLock.newCondition();

	/** 默认创建容量为Integer.MAX_VALUE的队列 */
	public LinkedBlockingQueueMock() {
		this(Integer.MAX_VALUE);
	}

	/** 指定队列长度 */
	public LinkedBlockingQueueMock(int capacity) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		this.capacity = capacity;
		last = head = new Node<E>(null);// 初始化 last和head item都为null
	}

	/** 入队到队尾, 如果 count.get() == capacity即容量已满 则调用notFull.await()阻塞 */
	@Override
	public void put(E e) throws InterruptedException {
		assertNotNull(e);

		int preCount = -1;
		putLock.lockInterruptibly();// TODO ???与调用lock的区别
		try {
			while (count.get() == capacity) {
				notFull.await();// 阻塞
			}
			enqueue(e);
			preCount = count.getAndIncrement();
			// 该处不用判断是否==capacity 下个put会在while中判断

			// 为什么此处还需要判断并调用notFull.signal()，参考take同样的判断原因说明
			if (preCount + 1 < capacity)
				notFull.signal();
		} finally {
			putLock.unlock();
		}
		if (preCount == 0) {// 如果原来队列满0 put之后 则可以唤醒take的阻塞操作
			// TODO 实际代码调用该方法前后 加了takeLock？？？
			notEmpty.signal();// 该处可能存在伪唤醒（如前面程序异常），唤醒后需要重新判断状态
		}
	}

	/** 根据FIFO 获取头节点（调用dequeue），如果队列为empty 则调用notEmpty.await()阻塞 */
	@Override
	public E take() throws InterruptedException {
		takeLock.lockInterruptibly();
		// 出对
		int preCount = -1;
		try {
			while (count.getAndIncrement() == 0) {
				notEmpty.await();
			}
			E node = dequeue();
			preCount = count.getAndDecrement();

			// put时如果preCount==0 已经调用了notEmpty.signal(); 如果为什么要加这步操作？？？
			// -->因为当capacity==0时 一个或多个take阻塞 而put可以触发多次 但在take阻塞之后
			// 只有第一次put会调用notEmpty.signal()
			// 这会导致take阻塞队列 只会唤醒一条 而其他仍在阻塞 而put中 只放入了一条 又不能调用signalAll() 所以在此处做判断 以唤醒take阻塞
			if (preCount > 1) {
				notEmpty.signal();
			}
			return node;
		} finally {
			takeLock.unlock();
			if (preCount == capacity) {// 如果原来队列满 take之后 则可以唤醒put的阻塞操作
				// TODO 实际代码调用该方法前后 加了putLock？？？
				notFull.signal();// 该处可能存在伪唤醒（如前面程序异常），唤醒后需要重新判断状态
			}
		}

	}

	/** 同put 不阻塞 */
	@Override
	public boolean offer(E e) {
		assertNotNull(e);

		if (count.get() == capacity) {
			return false;
		}

		int preCount = -1;
		putLock.lock();// ???为何不用lockInterruptibly()
		try {
			if (count.get() < capacity) {// 双重检查
				enqueue(e);
				preCount = count.getAndIncrement();
				// TODO

				// 为什么此处还需要判断并调用notFull.signal()，参考take同样的判断原因说明
				if (preCount + 1 < capacity)
					notFull.signal();

				return true;
			}
			return false;
		} finally {
			putLock.unlock();

			if (preCount == 0) {// 如果原来队列满0 put之后 则可以唤醒take的阻塞操作
				notEmpty.signal();
			}
		}
	}

	@Override
	public E poll() {
		if (count.get() == 0) {
			return null;
		}

		int preCount = -1;
		takeLock.lock();
		try {
			if (count.get() > 0) {// 双重检查
				E node = dequeue();
				preCount = count.getAndIncrement();

				if (preCount > 1)
					notEmpty.signal();

				return node;
			}
			return null;
		} finally {
			takeLock.unlock();
			if (preCount == capacity) {
				notFull.signal();
			}
		}
	}

	@Override
	public E peek() {
		if (count.get() == 0) {
			return null;
		}

		takeLock.lock();
		try {
			Node<E> first = head.next;
			if (first == null) {
				return null;
			} else {
				return first.item;
			}
		} finally {
			takeLock.unlock();
		}
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int remainingCapacity() {
		return capacity - count.get();
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return count.get();
	}

	/** 入队 放入队尾 TODO ???为何不加锁 */
	private void enqueue(E newValue) {
		Node<E> newNode = new Node<E>(newValue);
		last.next = newNode;
		last = newNode;
	}

	/** 移除并返回队列中的头节点 --实际返回的是head.next.item TODO ???为何不加锁 */
	E dequeue() {
		Node<E> dequeNode = head.next;
		head.next = null;
		head = dequeNode;
		E returnItem = dequeNode.item;
		head.item = null;// head节点item 为null
		return returnItem;
	}

	private void assertNotNull(E e) {
		if (e == null) {
			throw new NullPointerException();
		}
	}
}
