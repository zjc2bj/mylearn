package cn.zjc.learn.concurrent.ReentrantLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;

import cn.zjc.learn.concurrent.AbstractQueuedSynchronizer;

public class ReentrantLock implements Lock, java.io.Serializable {
	private static final long serialVersionUID = 6192030143029418797L;

	private final Sync sync;

	public ReentrantLock() {
		sync = new NonfairSync();
	}

	public ReentrantLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
	}

	@Override
	public void lock() {
		sync.lock();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		return sync.nonfairTryAcquire(1);
	}

	@Override
	/**
	 * 如果获取失败 会阻塞指定的时间
	 */
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}

	/**
	 * Queries the number of holds on this lock by the current thread.
	 */
	public int getHoldCount() {
        return sync.getHoldCount();
    }
	
	abstract static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = -2395738147825429428L;

		public void lock() {
			if (this instanceof NonfairSync) {
				// 非公平锁 -->先尝试直接获取锁 失败则走正常的获取逻辑 存在插队情况
				if (compareAndSetState(0, 1))
					setExclusiveOwnerThread(Thread.currentThread());
				else
					acquire(1);
			} else {
				acquire(1);
			}
		}

		/** 尝试获取锁 直接返回结果 不阻塞 */
		final boolean nonfairTryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (compareAndSetState(0, acquires)) {
					setExclusiveOwnerThread(current);
					return true;
				}
			} else if (current == getExclusiveOwnerThread()) {// 重入
				int nextc = c + acquires;// state 加 1
				if (nextc < 0) // overflow
					throw new Error("Maximum lock count exceeded");
				setState(nextc);
				return true;
			}
			return false;
		}

		protected final boolean tryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (this instanceof NonfairSync) {// 非公平锁
					if (compareAndSetState(0, acquires)) {
						setExclusiveOwnerThread(current);
						return true;
					}
				} else {// 公平锁
					// Don't grant access unless recursive call or no waiters or
					// is first.
					if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
						setExclusiveOwnerThread(current);
						return true;
					}
				}
			} else if (current == getExclusiveOwnerThread()) {
				int nextc = c + acquires;
				if (nextc < 0)
					throw new Error("Maximum lock count exceeded");
				setState(nextc);
				return true;
			}
			return false;
		}

		/**
		 * 将状态state 设置为 getState() - releases.<br>
		 * 为0则表示当前线程已释放锁--><br>
		 * 不为0则表示同一个线程中多次调用lock
		 */
		protected final boolean tryRelease(int releases) {
			int c = getState() - releases;
			if (Thread.currentThread() != getExclusiveOwnerThread())
				throw new IllegalMonitorStateException();
			boolean free = false;
			if (c == 0) {
				free = true;
				setExclusiveOwnerThread(null);
			}
			setState(c);
			return free;
		}
		
		final ConditionObject newCondition() {
            return new ConditionObject();
        }
		
		protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
		
		final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }
		
		final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }
		
		final boolean isLocked() {
            return getState() != 0;
        }
	}

	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = 434611634374840149L;
	}

	static final class FairSync extends Sync {
		private static final long serialVersionUID = 580096068561488247L;
	}
}
