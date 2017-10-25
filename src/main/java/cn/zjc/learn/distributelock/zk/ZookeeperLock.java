package cn.zjc.learn.distributelock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ZookeeperLock {
	private final static Logger logger = LoggerFactory.getLogger(ZookeeperLock.class);

	private InterProcessLock lock;
	private CuratorFramework client;
	private final static String LOCK_PREFIX = "/lock";
	private String lockPath;

	public ZookeeperLock(CuratorFramework client, String lockPath) {
		this.client = client;
		this.lockPath = lockPath;
		this.lock = new InterProcessMutex(client, ZKPaths.makePath(LOCK_PREFIX, lockPath));
	}

	public ZookeeperLock(InterProcessLock lock, CuratorFramework client, String lockPath) {
		this.lock = lock;
		this.client = client;
		this.lockPath = lockPath;
	}

	public void lock() throws RuntimeException {
		try {
			lock.acquire();
		} catch (Exception e) {
			throw new RuntimeException("acquire lock error!", e);
		}
	}

	public boolean tryLock(long time, TimeUnit unit) throws RuntimeException {
		try {
			return lock.acquire(time, unit);
		} catch (Exception e) {
			throw new RuntimeException("try acquire lock error or timeout!", e);
		}
	}

	public void unlock() throws RuntimeException {
		try {
			lock.release();
		} catch (Exception e) {
			throw new RuntimeException("release lock error!", e);
		}
	}
}
