package cn.zjc.learn.concurrent.atomic;


import sun.misc.Unsafe;

public class AtomicInteger extends Number implements java.io.Serializable{
	private static final long serialVersionUID = 7458131429574143750L;

	private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    
    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    private volatile int value;
    public AtomicInteger() {
    }
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }
    
    public final int get() {
        return value;
    }

    public final void set(int newValue) {
        value = newValue;
    }
    
    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }
    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this, valueOffset, -1);
    }
    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
    }
    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
    }
    
	@Override
	public int intValue() {
		return get();
	}

	@Override
	public long longValue() {
		return (long)get();
	}

	@Override
	public float floatValue() {
		return (float)get();
	}

	@Override
	public double doubleValue() {
		return (double)get();
	}

}
