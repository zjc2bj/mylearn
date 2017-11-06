package cn.zjc.learn.concurrent;

public abstract class AbstractOwnableSynchronizer implements java.io.Serializable {
	private static final long serialVersionUID = 7691623246221008088L;

	protected AbstractOwnableSynchronizer() {
	}

	private transient Thread exclusiveOwnerThread;

	protected final void setExclusiveOwnerThread(Thread t) {
		exclusiveOwnerThread = t;
	}

	protected final Thread getExclusiveOwnerThread() {
		return exclusiveOwnerThread;
	}
}
