package cn.zjc.learn;

public class Temp {
	private static final int COUNT_BITS = Integer.SIZE - 3;
	private static final int RUNNING = -1 << COUNT_BITS;
	private static final int SHUTDOWN = 0 << COUNT_BITS;
	private static final int STOP = 1 << COUNT_BITS;
	private static final int TIDYING = 2 << COUNT_BITS;
	private static final int TERMINATED = 3 << COUNT_BITS;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

	public static void main(String[] args) {
		System.out.println(RUNNING);
		System.out.println(SHUTDOWN);
		System.out.println(STOP);
		System.out.println(TIDYING);
		System.out.println(TERMINATED);
		System.out.println(CAPACITY);
	}
}