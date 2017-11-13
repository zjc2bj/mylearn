package cn.zjc.learn.concurrent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

import sun.misc.Unsafe;

/**
 * <pre>
 * 	基于第一次状态判断失败 执行自旋 将waitStatus=SIGNAL(初始值为0) 
 * 	下次执行则加锁，使用LockSupport.park(this);
 *  -->等待release唤醒(LockSupport.unpark(s.thread);)
 * </pre>
 * 
 * 
 * <pre>
 * 排它的获取这个状态。这个方法的实现需要查询当前状态是否允许获取，然后再进行获取（使用compareAndSetState来做）状态。 
 * protected boolean tryAcquire(int arg);
 *
 * 释放状态。 
 * protected boolean tryRelease(int arg) ;
 *
 * 共享的模式下获取状态。 
 * protected int tryAcquireShared(int arg) ;
 *
 * 共享的模式下释放状态。
 * protected boolean tryReleaseShared(int arg) ;
 *
 * 在排它模式下，状态是否被占用。 
 * protected boolean isHeldExclusively();
 * </pre>
 * 
 * @author zjc <br>
 */
@SuppressWarnings("restriction")
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements java.io.Serializable {

	private static final long serialVersionUID = 7373984972572414691L;

	protected AbstractQueuedSynchronizer() {
	}

	/**
	 * <pre>
	 *      +------+  prev +-----+       +-----+
	 * head |      | <---- |     | <---- |     |  tail
	 *      +------+       +-----+       +-----+
	 * </pre>
	 */
	static final class Node {
		static final Node SHARED = new Node();// 共享模式
		static final Node EXCLUSIVE = null;// 独占模式

		/** 表示当前的线程被取消 */
		static final int CANCELLED = 1;
		/** 表示当前节点的后继节点包含的线程需要运行，需要进行unpark操作 */
		static final int SIGNAL = -1;
		/** 表示当前节点在等待condition，也就是在condition队列中 */
		static final int CONDITION = -2;
		/** 表示当前场景下后续的acquireShared能够得以执行 */
		static final int PROPAGATE = -3;

		volatile int waitStatus;// 值为0表示当前节点在sync队列中，等待着获取锁

		volatile Node prev;

		volatile Node next;

		volatile Thread thread;// 入队列时的当前线程。

		Node nextWaiter;// 存储condition队列中的后继节点。

		final boolean isShared() {
			return nextWaiter == SHARED;
		}

		final Node predecessor() throws NullPointerException {
			Node p = prev;
			if (p == null)
				throw new NullPointerException();
			else
				return p;
		}

		Node() {
		}

		Node(Thread thread, Node mode) { // Used by addWaiter
			this.nextWaiter = mode;
			this.thread = thread;
		}

		Node(Thread thread, int waitStatus) { // Used by Condition
			this.waitStatus = waitStatus;
			this.thread = thread;
		}
	}

	/** If head exists, its waitStatus is guaranteed not to be CANCELLED. */
	private transient volatile Node head;
	private transient volatile Node tail;

	/** 锁监视器状态 */
	private volatile int state;

	/**
	 * Setup to support compareAndSet. We need to natively implement this here:
	 * For the sake of permitting future enhancements, we cannot explicitly
	 * subclass AtomicInteger, which would be efficient and useful otherwise.
	 * So, as the lesser of evils, we natively implement using hotspot
	 * intrinsics API. And while we are at it, we do the same for other CASable
	 * fields (which could otherwise be done with atomic field updaters).
	 */
	// private static final Unsafe unsafe = Unsafe.getUnsafe();
	private static final Unsafe unsafe;
	private static final long stateOffset;
	private static final long headOffset;
	private static final long tailOffset;
	private static final long waitStatusOffset;
	private static final long nextOffset;

	static {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			unsafe = (Unsafe) theUnsafe.get(null);

			stateOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("state"));
			headOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
			tailOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
			waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
			nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	/**
	 * CAS head field. Used only by enq.
	 */
	private final boolean compareAndSetHead(Node update) {
		return unsafe.compareAndSwapObject(this, headOffset, null, update);
	}

	/**
	 * CAS tail field. Used only by enq.
	 */
	private final boolean compareAndSetTail(Node expect, Node update) {
		return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
	}

	/**
	 * CAS waitStatus field of a node.
	 */
	private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
		return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
	}

	/**
	 * CAS next field of a node.
	 */
	private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
		return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
	}

	protected final int getState() {
		return state;
	}

	protected final void setState(int newState) {
		state = newState;
	}

	protected final boolean compareAndSetState(int expect, int update) {
		return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
	}

	// Queuing utilities

	/**
	 * The number of nanoseconds for which it is faster to spin rather than to
	 * use timed park. A rough estimate suffices to improve responsiveness with
	 * very short timeouts.
	 */
	static final long spinForTimeoutThreshold = 1000L;

	/**
	 * 自旋-->将node入队-->放到双向链表末尾 <B>head为new Node()</B>，并返回原始的tail节点。 <br>
	 * 如果第一次调用 即返回head
	 * 
	 * <pre>
	 * 如果tail(head为null)则初始化一个new Node(). 即  
	 * +------+   +------+ 
	 * | head | = | tail |  head = tail = new Node(); head = tail = new Node();
	 * +------+   +------+ 
	 * enq之后
	 * +------+  prev +-------+       +-----+
	 * | head | <---- |node...| <---- |tail | 
	 * +------+       +-------+       +-----+
	 *   head = new Node();
	 *   tail = node;(入参)
	 * </pre>
	 */
	private Node enq(final Node node) {
		for (;;) {
			Node t = tail;
			if (t == null) { // Must initialize
				if (compareAndSetHead(new Node()))
					tail = head;
			} else {
				node.prev = t;
				if (compareAndSetTail(t, node)) {
					t.next = node;
					return t;
				}
			}
		}
	}

	/**
	 * 创建当前线程的node 并入队，返回创建的node
	 * 
	 * @return 返回指定模式的node
	 */
	private Node addWaiter(Node mode) {
		Node node = new Node(Thread.currentThread(), mode);
		// 先尝试入队 失败则执行 enq流程
		Node t = tail;
		if (t != null) {
			node.prev = t;
			if (compareAndSetTail(t, node)) {
				t.next = node;
				return node;
			}
		}
		enq(node);// 初始化head tail 并放入对了末尾
		return node;
	}

	/**
	 * 原head出对，当前node设置为head
	 */
	private void setHead(Node node) {
		head = node;
		node.thread = null;
		node.prev = null;
	}

	/**
	 * 修改当前节点node的waitStatus=0，唤醒下个节点从阻塞中恢复
	 * <p>
	 * 在此过程中结束，下个节点恢复执行tryAcquire前 非公平锁可能会正好在此期间 执行tryAcquire成功
	 * </p>
	 * 
	 * @param node
	 *            the node
	 */
	// 将节点waitStatus状态设置为0
	private void unparkSuccessor(Node node) {
		int ws = node.waitStatus;
		if (ws < 0)
			compareAndSetWaitStatus(node, ws, 0);

		// 循环取下个等待的节点
		Node s = node.next;
		if (s == null || s.waitStatus > 0) {
			s = null;
			for (Node t = tail; t != null && t != node; t = t.prev)
				if (t.waitStatus <= 0)
					s = t;
		}

		// 唤醒下个节点的阻塞状态
		if (s != null)
			LockSupport.unpark(s.thread);
	}

	/**
	 * Release action for shared mode -- signal successor and ensure
	 * propagation. (Note: For exclusive mode, release just amounts to calling
	 * unparkSuccessor of head if it needs signal.)
	 */
	private void doReleaseShared() {
		/*
		 * Ensure that a release propagates, even if there are other in-progress
		 * acquires/releases. This proceeds in the usual way of trying to
		 * unparkSuccessor of head if it needs signal. But if it does not,
		 * status is set to PROPAGATE to ensure that upon release, propagation
		 * continues. Additionally, we must loop in case a new node is added
		 * while we are doing this. Also, unlike other uses of unparkSuccessor,
		 * we need to know if CAS to reset status fails, if so rechecking.
		 */
		for (;;) {
			Node h = head;
			if (h != null && h != tail) {
				int ws = h.waitStatus;
				System.out.println("waitStatus = " + h.waitStatus);
				if (ws == Node.SIGNAL) {
					if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
						continue; // loop to recheck cases
					unparkSuccessor(h);
				} else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
					continue; // loop on failed CAS
			}
			if (h == head) // loop if head changed
				break;
		}
	}

	/**
	 * Sets head of queue, and checks if successor may be waiting in shared
	 * mode, if so propagating if either propagate > 0 or PROPAGATE status was
	 * set.
	 *
	 * @param node
	 *            the node
	 * @param propagate
	 *            the return value from a tryAcquireShared
	 */
	private void setHeadAndPropagate(Node node, int propagate) {
		Node h = head; // Record old head for check below
		setHead(node);
		/*
		 * Try to signal next queued node if: Propagation was indicated by
		 * caller, or was recorded (as h.waitStatus) by a previous operation
		 * (note: this uses sign-check of waitStatus because PROPAGATE status
		 * may transition to SIGNAL.) and The next node is waiting in shared
		 * mode, or we don't know, because it appears null
		 *
		 * The conservatism in both of these checks may cause unnecessary
		 * wake-ups, but only when there are multiple racing acquires/releases,
		 * so most need signals now or soon anyway.
		 */
		if (propagate > 0 || h == null || h.waitStatus < 0) {
			Node s = node.next;
			if (s == null || s.isShared())
				doReleaseShared();
		}
	}

	// Utilities for various versions of acquire

	/**
	 * Cancels an ongoing attempt to acquire.
	 *
	 * @param node
	 *            the node
	 */
	private void cancelAcquire(Node node) {
		// Ignore if node doesn't exist
		if (node == null)
			return;

		node.thread = null;

		// Skip cancelled predecessors
		Node pred = node.prev;
		while (pred.waitStatus > 0)
			node.prev = pred = pred.prev;

		// predNext is the apparent node to unsplice. CASes below will
		// fail if not, in which case, we lost race vs another cancel
		// or signal, so no further action is necessary.
		Node predNext = pred.next;

		// Can use unconditional write instead of CAS here.
		// After this atomic step, other Nodes can skip past us.
		// Before, we are free of interference from other threads.
		node.waitStatus = Node.CANCELLED;

		// If we are the tail, remove ourselves.
		if (node == tail && compareAndSetTail(node, pred)) {
			compareAndSetNext(pred, predNext, null);
		} else {
			// If successor needs signal, try to set pred's next-link
			// so it will get one. Otherwise wake it up to propagate.
			int ws;
			if (pred != head && ((ws = pred.waitStatus) == Node.SIGNAL
					|| (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) && pred.thread != null) {
				Node next = node.next;
				if (next != null && next.waitStatus <= 0)
					compareAndSetNext(pred, predNext, next);
			} else {
				unparkSuccessor(node);
			}

			node.next = node; // help GC
		}
	}

	/**
	 * 验证waitStatus状态 首次则将waitStatus设置为SIGNAL 并返回false(用于再次尝试),第二次则返回true
	 */
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		int ws = pred.waitStatus;
		if (ws == Node.SIGNAL)
			return true;
		if (ws > 0) {
			// 如果当前线程被取消，则遍历所有取消节点(waitStatus>0)并删除，最终返回false
			do {
				node.prev = pred = pred.prev;
			} while (pred.waitStatus > 0);
			pred.next = node;
		} else {
			// 第一次判断 先将waitStatus=Node.SIGNAL,返回false，在下次再acquire失败判断时 返回true
			compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
		}
		return false;
	}

	/**
	 * 中断当前线程
	 */
	private static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	/** 阻塞当前线程 解除阻塞后 返回当前线程的阻塞状态 */
	private final boolean parkAndCheckInterrupt() {
		LockSupport.park(this);
		return Thread.interrupted();
	}

	/**
	 * *
	 * 
	 * <pre>
	 * 	基于第一次状态判断失败 执行自旋 将waitStatus=SIGNAL(初始值为0) 
	 * 	下次执行则加锁，使用LockSupport.park(this);
	 *  -->等待release唤醒(LockSupport.unpark(s.thread);)
	 * </pre>
	 * 
	 * <pre>
	 *  自旋for(;;)
	 * 		if(node.prev==head && tryAcquire()==true)
	 * 			 return 当前线程interrupted状态
	 * 		else
	 * 			判断是否需要阻塞(waitStatus==SIGNAL) ：第一次不阻塞 第二次阻塞
	 * 			if true--> 执行parkAndCheckInterrupt阻塞: LockSupport.park(this);
	 * 					   阻塞需要等待unparkSuccessor调用LockSupport.unpark(node.next.thread);解除阻塞
	 * 		continue;
	 * </pre>
	 */
	final boolean acquireQueued(final Node node, int arg) {
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);// 将当前节点设置为head
					p.next = null; // help GC
					failed = false;
					return interrupted;
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					interrupted = true;
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * {@link #acquireQueued(Node, int)}} 区别在于阻塞期间 如果Thread.interrupted()==ture
	 * 则该方法抛出InterruptedException异常
	 */
	private void doAcquireInterruptibly(int arg) throws InterruptedException {
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return;
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * <pre>
	 * AQS与JVM内置锁的一个不同点在于AQS中提供了超时机制，即线程在等待一定时间后会立即返回。
	 * 1、首先取得当前系统时间，在循环等待的过程中，如果剩余时间nanosTimeout<=0立即返回；
	 * 2、如果剩余时间>0，就用总时间减去一次循环耗费的时间，继续阻塞；
	 * 3、如果在这期间线程被中断，就抛出中断异常，如果有其他异常产生，就取消这次获取。
	 * </pre>
	 * 
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return true;
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared uninterruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireShared(int arg) {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						if (interrupted)
							selfInterrupt();
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					interrupted = true;
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared interruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared timed mode.
	 *
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {

		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return true;
					}
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	// Main exported methods

	/**
	 * 排它的获取这个状态。这个方法的实现需要查询当前状态是否允许获取，然后再进行获取（使用compareAndSetState来做）状态。
	 */
	protected boolean tryAcquire(int arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 释放状态。
	 */
	protected boolean tryRelease(int arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 共享的模式下获取状态。
	 */
	protected int tryAcquireShared(int arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 共享的模式下释放状态。
	 */
	protected boolean tryReleaseShared(int arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 在排它模式下，状态是否被占用。
	 */
	protected boolean isHeldExclusively() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * <pre>
	 * 独占模式, 忽略interrupts. 
	 * 至少执行一次 {@link #tryAcquire},成功则返回 
	 * 	否则.现成进入队列, possibly repeatedly blocking and unblocking, invoking
	 * {@link #tryAcquire} until success. 该方法可以用来实现 {@link Lock#lock}.
	 * </pre>
	 */
	public final void acquire(int arg) {
		if (!tryAcquire(arg) //
				// 自旋，尝试tryAcquire 尝试两次 则进入阻塞状态LockSupport.park(this);
				&& acquireQueued(
						// 创建node入队 设置为tail
						addWaiter(Node.EXCLUSIVE), arg))
			selfInterrupt();// 如果当前线程为中断状态 则调用中断
	}

	/**
	 * Acquires in exclusive mode, aborting if interrupted. Implemented by first
	 * checking interrupt status, then invoking at least once
	 * {@link #tryAcquire}, returning on success. Otherwise the thread is
	 * queued, possibly repeatedly blocking and unblocking, invoking
	 * {@link #tryAcquire} until success or the thread is interrupted. This
	 * method can be used to implement method {@link Lock#lockInterruptibly}.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquire} but is otherwise uninterpreted and can
	 *            represent anything you like.
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 */
	public final void acquireInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		if (!tryAcquire(arg))
			doAcquireInterruptibly(arg);
	}

	/**
	 * 独占模式下 在指定时间内 尝试获取锁 如果为达到指定时间 并且为获取失败 则执行自旋<br>
	 * --（连续2次失败 会调用LockSupport.parkNanos(this, nanosTimeout)阻塞）<br>
	 * 该方法可以用来实现{@link Lock#tryLock(long, TimeUnit)}.
	 * 
	 * @throws 遇中断则抛出InterruptedException
	 */
	public final boolean tryAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
	}

	/**
	 * 独占模式下 释放锁 <br>
	 * 该方法用于实现 {@link Lock#unlock}.
	 *
	 * @return tryRelease()
	 */
	public final boolean release(int arg) {
		if (tryRelease(arg)) {
			Node h = head;
			if (h != null && h.waitStatus != 0)
				unparkSuccessor(h);// 唤醒下个等待节点的阻塞
			return true;
		}
		return false;
	}

	/**
	 * Acquires in shared mode, ignoring interrupts. Implemented by first
	 * invoking at least once {@link #tryAcquireShared}, returning on success.
	 * Otherwise the thread is queued, possibly repeatedly blocking and
	 * unblocking, invoking {@link #tryAcquireShared} until success.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquireShared} but is otherwise uninterpreted and
	 *            can represent anything you like.
	 */
	public final void acquireShared(int arg) {
		if (tryAcquireShared(arg) < 0)
			doAcquireShared(arg);
	}

	/**
	 * Acquires in shared mode, aborting if interrupted. Implemented by first
	 * checking interrupt status, then invoking at least once
	 * {@link #tryAcquireShared}, returning on success. Otherwise the thread is
	 * queued, possibly repeatedly blocking and unblocking, invoking
	 * {@link #tryAcquireShared} until success or the thread is interrupted.
	 * 
	 * @param arg
	 *            the acquire argument This value is conveyed to
	 *            {@link #tryAcquireShared} but is otherwise uninterpreted and
	 *            can represent anything you like.
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 */
	public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		if (tryAcquireShared(arg) < 0)
			doAcquireSharedInterruptibly(arg);
	}

	/**
	 * Attempts to acquire in shared mode, aborting if interrupted, and failing
	 * if the given timeout elapses. Implemented by first checking interrupt
	 * status, then invoking at least once {@link #tryAcquireShared}, returning
	 * on success. Otherwise, the thread is queued, possibly repeatedly blocking
	 * and unblocking, invoking {@link #tryAcquireShared} until success or the
	 * thread is interrupted or the timeout elapses.
	 *
	 * @param arg
	 *            the acquire argument. This value is conveyed to
	 *            {@link #tryAcquireShared} but is otherwise uninterpreted and
	 *            can represent anything you like.
	 * @param nanosTimeout
	 *            the maximum number of nanoseconds to wait
	 * @return {@code true} if acquired; {@code false} if timed out
	 * @throws InterruptedException
	 *             if the current thread is interrupted
	 */
	public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
	}

	/**
	 * Releases in shared mode. Implemented by unblocking one or more threads if
	 * {@link #tryReleaseShared} returns true.
	 *
	 * @param arg
	 *            the release argument. This value is conveyed to
	 *            {@link #tryReleaseShared} but is otherwise uninterpreted and
	 *            can represent anything you like.
	 * @return the value returned from {@link #tryReleaseShared}
	 */
	public final boolean releaseShared(int arg) {
		if (tryReleaseShared(arg)) {
			doReleaseShared();
			return true;
		}
		return false;
	}

	// Queue inspection methods

	/**
	 * Queries whether any threads are waiting to acquire. Note that because
	 * cancellations due to interrupts and timeouts may occur at any time, a
	 * {@code true} return does not guarantee that any other thread will ever
	 * acquire.
	 *
	 * <p>
	 * In this implementation, this operation returns in constant time.
	 *
	 * @return {@code true} if there may be other threads waiting to acquire
	 */
	public final boolean hasQueuedThreads() {
		return head != tail;
	}

	/**
	 * Queries whether any threads have ever contended to acquire this
	 * synchronizer; that is if an acquire method has ever blocked.
	 *
	 * <p>
	 * In this implementation, this operation returns in constant time.
	 *
	 * @return {@code true} if there has ever been contention
	 */
	public final boolean hasContended() {
		return head != null;
	}

	/**
	 * Returns the first (longest-waiting) thread in the queue, or {@code null}
	 * if no threads are currently queued.
	 *
	 * <p>
	 * In this implementation, this operation normally returns in constant time,
	 * but may iterate upon contention if other threads are concurrently
	 * modifying the queue.
	 *
	 * @return the first (longest-waiting) thread in the queue, or {@code null}
	 *         if no threads are currently queued
	 */
	public final Thread getFirstQueuedThread() {
		// handle only fast path, else relay
		return (head == tail) ? null : fullGetFirstQueuedThread();
	}

	/**
	 * Version of getFirstQueuedThread called when fastpath fails
	 */
	private Thread fullGetFirstQueuedThread() {
		/*
		 * The first node is normally head.next. Try to get its thread field,
		 * ensuring consistent reads: If thread field is nulled out or s.prev is
		 * no longer head, then some other thread(s) concurrently performed
		 * setHead in between some of our reads. We try this twice before
		 * resorting to traversal.
		 */
		Node h, s;
		Thread st;
		if (((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null)
				|| ((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null))
			return st;

		/*
		 * Head's next field might not have been set yet, or may have been unset
		 * after setHead. So we must check to see if tail is actually first
		 * node. If not, we continue on, safely traversing from tail back to
		 * head to find first, guaranteeing termination.
		 */

		Node t = tail;
		Thread firstThread = null;
		while (t != null && t != head) {
			Thread tt = t.thread;
			if (tt != null)
				firstThread = tt;
			t = t.prev;
		}
		return firstThread;
	}

	/**
	 * Returns true if the given thread is currently queued.
	 *
	 * <p>
	 * This implementation traverses the queue to determine presence of the
	 * given thread.
	 *
	 * @param thread
	 *            the thread
	 * @return {@code true} if the given thread is on the queue
	 * @throws NullPointerException
	 *             if the thread is null
	 */
	public final boolean isQueued(Thread thread) {
		if (thread == null)
			throw new NullPointerException();
		for (Node p = tail; p != null; p = p.prev)
			if (p.thread == thread)
				return true;
		return false;
	}

	/**
	 * Returns {@code true} if the apparent first queued thread, if one exists,
	 * is waiting in exclusive mode. If this method returns {@code true}, and
	 * the current thread is attempting to acquire in shared mode (that is, this
	 * method is invoked from {@link #tryAcquireShared}) then it is guaranteed
	 * that the current thread is not the first queued thread. Used only as a
	 * heuristic in ReentrantReadWriteLock.
	 */
	final boolean apparentlyFirstQueuedIsExclusive() {
		Node h, s;
		return (h = head) != null && (s = h.next) != null && !s.isShared() && s.thread != null;
	}

	/**
	 * 判断队列中是否有等待线程。该判断并非绝对公平 由于interrupts 或timeouts随时可能发生 当前线程返回false
	 * 下个调用可能返回true
	 * 
	 * @since 1.7
	 */
	public final boolean hasQueuedPredecessors() {
		Node t = tail; // Read fields in reverse initialization order
		Node h = head;
		Node s;
		return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
	}

	// Instrumentation and monitoring methods

	/**
	 * Returns an estimate of the number of threads waiting to acquire. The
	 * value is only an estimate because the number of threads may change
	 * dynamically while this method traverses internal data structures. This
	 * method is designed for use in monitoring system state, not for
	 * synchronization control.
	 *
	 * @return the estimated number of threads waiting to acquire
	 */
	public final int getQueueLength() {
		int n = 0;
		for (Node p = tail; p != null; p = p.prev) {
			if (p.thread != null)
				++n;
		}
		return n;
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire.
	 * Because the actual set of threads may change dynamically while
	 * constructing this result, the returned collection is only a best-effort
	 * estimate. The elements of the returned collection are in no particular
	 * order. This method is designed to facilitate construction of subclasses
	 * that provide more extensive monitoring facilities.
	 *
	 * @return the collection of threads
	 */
	public final Collection<Thread> getQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			Thread t = p.thread;
			if (t != null)
				list.add(t);
		}
		return list;
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire in
	 * exclusive mode. This has the same properties as {@link #getQueuedThreads}
	 * except that it only returns those threads waiting due to an exclusive
	 * acquire.
	 *
	 * @return the collection of threads
	 */
	public final Collection<Thread> getExclusiveQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (!p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire in
	 * shared mode. This has the same properties as {@link #getQueuedThreads}
	 * except that it only returns those threads waiting due to a shared
	 * acquire.
	 *
	 * @return the collection of threads
	 */
	public final Collection<Thread> getSharedQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	/**
	 * Returns a string identifying this synchronizer, as well as its state. The
	 * state, in brackets, includes the String {@code "State ="} followed by the
	 * current value of {@link #getState}, and either {@code "nonempty"} or
	 * {@code "empty"} depending on whether the queue is empty.
	 *
	 * @return a string identifying this synchronizer, as well as its state
	 */
	public String toString() {
		int s = getState();
		String q = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + s + ", " + q + "empty queue]";
	}

	// Internal support methods for Conditions

	/**
	 * Returns true if a node, always one that was initially placed on a
	 * condition queue, is now waiting to reacquire on sync queue.
	 * 
	 * @param node
	 *            the node
	 * @return true if is reacquiring
	 */
	final boolean isOnSyncQueue(Node node) {
		if (node.waitStatus == Node.CONDITION || node.prev == null)
			return false;
		if (node.next != null) // If has successor, it must be on queue
			return true;
		/*
		 * node.prev can be non-null, but not yet on queue because the CAS to
		 * place it on queue can fail. So we have to traverse from tail to make
		 * sure it actually made it. It will always be near the tail in calls to
		 * this method, and unless the CAS failed (which is unlikely), it will
		 * be there, so we hardly ever traverse much.
		 */
		return findNodeFromTail(node);
	}

	/**
	 * Returns true if node is on sync queue by searching backwards from tail.
	 * Called only when needed by isOnSyncQueue.
	 * 
	 * @return true if present
	 */
	private boolean findNodeFromTail(Node node) {
		Node t = tail;
		for (;;) {
			if (t == node)
				return true;
			if (t == null)
				return false;
			t = t.prev;
		}
	}

	/**
	 * Transfers a node from a condition queue onto sync queue. Returns true if
	 * successful.
	 * 
	 * @param node
	 *            the node
	 * @return true if successfully transferred (else the node was cancelled
	 *         before signal).
	 */
	final boolean transferForSignal(Node node) {
		/*
		 * If cannot change waitStatus, the node has been cancelled.
		 */
		if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
			return false;

		/*
		 * Splice onto queue and try to set waitStatus of predecessor to
		 * indicate that thread is (probably) waiting. If cancelled or attempt
		 * to set waitStatus fails, wake up to resync (in which case the
		 * waitStatus can be transiently and harmlessly wrong).
		 */
		Node p = enq(node);
		int ws = p.waitStatus;
		if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
			LockSupport.unpark(node.thread);
		return true;
	}

	/**
	 * Transfers node, if necessary, to sync queue after a cancelled wait.
	 * Returns true if thread was cancelled before being signalled.
	 * 
	 * @param current
	 *            the waiting thread
	 * @param node
	 *            its node
	 * @return true if cancelled before the node was signalled
	 */
	final boolean transferAfterCancelledWait(Node node) {
		if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			enq(node);
			return true;
		}
		/*
		 * If we lost out to a signal(), then we can't proceed until it finishes
		 * its enq(). Cancelling during an incomplete transfer is both rare and
		 * transient, so just spin.
		 */
		while (!isOnSyncQueue(node))
			Thread.yield();
		return false;
	}

	/**
	 * 根据当前state状态 执行release();出现异常 则取消当前节点的阻塞状态
	 * 
	 * @param node
	 *            the condition node for this wait
	 * @return 返回原state
	 */
	final int fullyRelease(Node node) {
		boolean failed = true;
		try {
			int savedState = getState();
			if (release(savedState)) {
				failed = false;
				return savedState;
			} else {
				throw new IllegalMonitorStateException();
			}
		} finally {
			if (failed)
				node.waitStatus = Node.CANCELLED;
		}
	}

	// Instrumentation methods for conditions

	/**
	 * Queries whether the given ConditionObject uses this synchronizer as its
	 * lock.
	 *
	 * @param condition
	 *            the condition
	 * @return <tt>true</tt> if owned
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public final boolean owns(ConditionObject condition) {
		if (condition == null)
			throw new NullPointerException();
		return condition.isOwnedBy(this);
	}

	/**
	 * Queries whether any threads are waiting on the given condition associated
	 * with this synchronizer. Note that because timeouts and interrupts may
	 * occur at any time, a <tt>true</tt> return does not guarantee that a
	 * future <tt>signal</tt> will awaken any threads. This method is designed
	 * primarily for use in monitoring of the system state.
	 *
	 * @param condition
	 *            the condition
	 * @return <tt>true</tt> if there are any waiting threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public final boolean hasWaiters(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.hasWaiters();
	}

	/**
	 * Returns an estimate of the number of threads waiting on the given
	 * condition associated with this synchronizer. Note that because timeouts
	 * and interrupts may occur at any time, the estimate serves only as an
	 * upper bound on the actual number of waiters. This method is designed for
	 * use in monitoring of the system state, not for synchronization control.
	 *
	 * @param condition
	 *            the condition
	 * @return the estimated number of waiting threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public final int getWaitQueueLength(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitQueueLength();
	}

	/**
	 * Returns a collection containing those threads that may be waiting on the
	 * given condition associated with this synchronizer. Because the actual set
	 * of threads may change dynamically while constructing this result, the
	 * returned collection is only a best-effort estimate. The elements of the
	 * returned collection are in no particular order.
	 *
	 * @param condition
	 *            the condition
	 * @return the collection of threads
	 * @throws IllegalMonitorStateException
	 *             if exclusive synchronization is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this
	 *             synchronizer
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitingThreads();
	}

	/**
	 * http://www.cnblogs.com/leesf456/p/5350186.html
	 */
	public class ConditionObject implements Condition, java.io.Serializable {
		private static final long serialVersionUID = 1173984872572414699L;
		private transient Node firstWaiter;
		private transient Node lastWaiter;

		public ConditionObject() {
		}

		/**
		 * Implements interruptible condition wait.
		 * <ol>
		 * <li>If current thread is interrupted, throw InterruptedException.
		 * <li>Save lock state returned by {@link #getState}.
		 * <li>Invoke {@link #release} with saved state as argument, throwing
		 * IllegalMonitorStateException if it fails.
		 * <li>Block until signalled or interrupted.
		 * <li>Reacquire by invoking specialized version of {@link #acquire}
		 * with saved state as argument.
		 * <li>If interrupted while blocked in step 4, throw
		 * InterruptedException.
		 * </ol>
		 */
		public final void await() throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();// 添加到对尾
			int savedState = fullyRelease(node);
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null) // clean up if cancelled
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
		}

		/**
		 * Implements uninterruptible condition wait.
		 * <ol>
		 * <li>Save lock state returned by {@link #getState}.
		 * <li>Invoke {@link #release} with saved state as argument, throwing
		 * IllegalMonitorStateException if it fails.
		 * <li>Block until signalled.
		 * <li>Reacquire by invoking specialized version of {@link #acquire}
		 * with saved state as argument.
		 * </ol>
		 */
		public final void awaitUninterruptibly() {
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean interrupted = false;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if (Thread.interrupted())
					interrupted = true;
			}
			if (acquireQueued(node, savedState) || interrupted)
				selfInterrupt();
		}

		/**
		 * Implements timed condition wait.
		 * <ol>
		 * <li>If current thread is interrupted, throw InterruptedException.
		 * <li>Save lock state returned by {@link #getState}.
		 * <li>Invoke {@link #release} with saved state as argument, throwing
		 * IllegalMonitorStateException if it fails.
		 * <li>Block until signalled, interrupted, or timed out.
		 * <li>Reacquire by invoking specialized version of {@link #acquire}
		 * with saved state as argument.
		 * <li>If interrupted while blocked in step 4, throw
		 * InterruptedException.
		 * </ol>
		 */
		public final long awaitNanos(long nanosTimeout) throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			long lastTime = System.nanoTime();
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkNanos(this, nanosTimeout);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;

				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return nanosTimeout - (System.nanoTime() - lastTime);
		}

		/**
		 * Implements absolute timed condition wait.
		 * <ol>
		 * <li>If current thread is interrupted, throw InterruptedException.
		 * <li>Save lock state returned by {@link #getState}.
		 * <li>Invoke {@link #release} with saved state as argument, throwing
		 * IllegalMonitorStateException if it fails.
		 * <li>Block until signalled, interrupted, or timed out.
		 * <li>Reacquire by invoking specialized version of {@link #acquire}
		 * with saved state as argument.
		 * <li>If interrupted while blocked in step 4, throw
		 * InterruptedException.
		 * <li>If timed out while blocked in step 4, return false, else true.
		 * </ol>
		 */
		public final boolean awaitUntil(Date deadline) throws InterruptedException {
			if (deadline == null)
				throw new NullPointerException();
			long abstime = deadline.getTime();
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (System.currentTimeMillis() > abstime) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkUntil(this, abstime);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return !timedout;
		}

		/**
		 * Moves the longest-waiting thread, if one exists, from the wait queue
		 * for this condition to the wait queue for the owning lock.
		 *
		 * @throws IllegalMonitorStateException
		 *             if {@link #isHeldExclusively} returns {@code false}
		 */
		public final void signal() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			Node first = firstWaiter;
			if (first != null)
				doSignal(first);
		}

		/**
		 * Moves all threads from the wait queue for this condition to the wait
		 * queue for the owning lock.
		 *
		 * @throws IllegalMonitorStateException
		 *             if {@link #isHeldExclusively} returns {@code false}
		 */
		public final void signalAll() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			Node first = firstWaiter;
			if (first != null)
				doSignalAll(first);
		}

		/**
		 * 为当前现成创建node 并添加到队尾
		 * 
		 * @return 返回当前线程对应的新节点
		 */
		private Node addConditionWaiter() {
			Node t = lastWaiter;
			// If lastWaiter is cancelled, clean out.
			if (t != null && t.waitStatus != Node.CONDITION) {
				unlinkCancelledWaiters();
				t = lastWaiter;
			}
			Node node = new Node(Thread.currentThread(), Node.CONDITION);
			if (t == null)
				firstWaiter = node;
			else
				t.nextWaiter = node;
			lastWaiter = node;
			return node;
		}

		/**
		 * Removes and transfers nodes until hit non-cancelled one or null.
		 * Split out from signal in part to encourage compilers to inline the
		 * case of no waiters.
		 * 
		 * @param first
		 *            (non-null) the first node on condition queue
		 */
		private void doSignal(Node first) {
			do {
				if ((firstWaiter = first.nextWaiter) == null)
					lastWaiter = null;
				first.nextWaiter = null;
			} while (!transferForSignal(first) && (first = firstWaiter) != null);
		}

		/**
		 * Removes and transfers all nodes.
		 * 
		 * @param first
		 *            (non-null) the first node on condition queue
		 */
		private void doSignalAll(Node first) {
			lastWaiter = firstWaiter = null;
			do {
				Node next = first.nextWaiter;
				first.nextWaiter = null;
				transferForSignal(first);
				first = next;
			} while (first != null);
		}

		/**
		 * Unlinks cancelled waiter nodes from condition queue. Called only
		 * while holding lock. This is called when cancellation occurred during
		 * condition wait, and upon insertion of a new waiter when lastWaiter is
		 * seen to have been cancelled. This method is needed to avoid garbage
		 * retention in the absence of signals. So even though it may require a
		 * full traversal, it comes into play only when timeouts or
		 * cancellations occur in the absence of signals. It traverses all nodes
		 * rather than stopping at a particular target to unlink all pointers to
		 * garbage nodes without requiring many re-traversals during
		 * cancellation storms.
		 */
		private void unlinkCancelledWaiters() {
			Node t = firstWaiter;
			Node trail = null;
			while (t != null) {
				Node next = t.nextWaiter;
				if (t.waitStatus != Node.CONDITION) {
					t.nextWaiter = null;
					if (trail == null)
						firstWaiter = next;
					else
						trail.nextWaiter = next;
					if (next == null)
						lastWaiter = trail;
				} else
					trail = t;
				t = next;
			}
		}

		// public methods

		/*
		 * For interruptible waits, we need to track whether to throw
		 * InterruptedException, if interrupted while blocked on condition,
		 * versus reinterrupt current thread, if interrupted while blocked
		 * waiting to re-acquire.
		 */

		/** Mode meaning to reinterrupt on exit from wait */
		private static final int REINTERRUPT = 1;
		/** Mode meaning to throw InterruptedException on exit from wait */
		private static final int THROW_IE = -1;

		/**
		 * Checks for interrupt, returning THROW_IE if interrupted before
		 * signalled, REINTERRUPT if after signalled, or 0 if not interrupted.
		 */
		private int checkInterruptWhileWaiting(Node node) {
			return Thread.interrupted() ? (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) : 0;
		}

		/**
		 * Throws InterruptedException, reinterrupts current thread, or does
		 * nothing, depending on mode.
		 */
		private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
			if (interruptMode == THROW_IE)
				throw new InterruptedException();
			else if (interruptMode == REINTERRUPT)
				selfInterrupt();
		}

		/**
		 * Implements timed condition wait.
		 * <ol>
		 * <li>If current thread is interrupted, throw InterruptedException.
		 * <li>Save lock state returned by {@link #getState}.
		 * <li>Invoke {@link #release} with saved state as argument, throwing
		 * IllegalMonitorStateException if it fails.
		 * <li>Block until signalled, interrupted, or timed out.
		 * <li>Reacquire by invoking specialized version of {@link #acquire}
		 * with saved state as argument.
		 * <li>If interrupted while blocked in step 4, throw
		 * InterruptedException.
		 * <li>If timed out while blocked in step 4, return false, else true.
		 * </ol>
		 */
		public final boolean await(long time, TimeUnit unit) throws InterruptedException {
			if (unit == null)
				throw new NullPointerException();
			long nanosTimeout = unit.toNanos(time);
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			long lastTime = System.nanoTime();
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				if (nanosTimeout >= spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return !timedout;
		}

		// support for instrumentation

		/**
		 * Returns true if this condition was created by the given
		 * synchronization object.
		 *
		 * @return {@code true} if owned
		 */
		final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
			return sync == AbstractQueuedSynchronizer.this;
		}

		/**
		 * Queries whether any threads are waiting on this condition. Implements
		 * {@link AbstractQueuedSynchronizer#hasWaiters}.
		 *
		 * @return {@code true} if there are any waiting threads
		 * @throws IllegalMonitorStateException
		 *             if {@link #isHeldExclusively} returns {@code false}
		 */
		protected final boolean hasWaiters() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION)
					return true;
			}
			return false;
		}

		/**
		 * Returns an estimate of the number of threads waiting on this
		 * condition. Implements
		 * {@link AbstractQueuedSynchronizer#getWaitQueueLength}.
		 *
		 * @return the estimated number of waiting threads
		 * @throws IllegalMonitorStateException
		 *             if {@link #isHeldExclusively} returns {@code false}
		 */
		protected final int getWaitQueueLength() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			int n = 0;
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION)
					++n;
			}
			return n;
		}

		/**
		 * Returns a collection containing those threads that may be waiting on
		 * this Condition. Implements
		 * {@link AbstractQueuedSynchronizer#getWaitingThreads}.
		 *
		 * @return the collection of threads
		 * @throws IllegalMonitorStateException
		 *             if {@link #isHeldExclusively} returns {@code false}
		 */
		protected final Collection<Thread> getWaitingThreads() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			ArrayList<Thread> list = new ArrayList<Thread>();
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION) {
					Thread t = w.thread;
					if (t != null)
						list.add(t);
				}
			}
			return list;
		}
	}
}
