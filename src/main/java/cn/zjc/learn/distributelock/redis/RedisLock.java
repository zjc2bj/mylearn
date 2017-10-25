/*
* Copyright (c) 2015 daojia.com. All Rights Reserved.
*/
package cn.zjc.learn.distributelock.redis;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @author wuguangxu
 * @description redis锁
 * @since 1.0
 */
public class RedisLock {

    //锁的公共前缀
    private static final String PREFIX_KEY = "dop2c:lock:key";
    //默认获取锁周期
    private static final int DEFAULT_RETRY_TIME = 500;
    //默认过期时间
    private static final long DEFAULT_EXPIRED_TIME = 60 * 1000;
    //默认超时时间
    private static final long DEFAULT_TIMEOUT_TIME = 0;

    //锁的名称
    private String key;

    //锁的名称唯一前缀
    private String namespace;

    //锁的过期毫秒数
    private long expiredTime;

    //获取锁的等待超时时间毫秒数
    private long timeout;

    //重复获取锁周期
    private long retryTime;

    //锁定时间
    private long lockTime;

    //spring redis封装
    private ValueOperations<Object, Object> valueOperations;

    /**
     * 创建redis锁实例
     *
     * @param key
     */
    public RedisLock(String key) {
        this(key, DEFAULT_EXPIRED_TIME, DEFAULT_TIMEOUT_TIME);
    }

    /**
     * 创建redis锁实例
     *
     * @param key
     * @param expiredTime
     * @param timeout
     */
    public RedisLock(String key, long expiredTime, long timeout) {
        this(key, null, expiredTime, timeout, DEFAULT_RETRY_TIME);
    }

    /**
     * 创建redis锁实例
     *
     * @param key
     * @param namespace
     * @param expiredTime
     * @param timeout
     * @param retryTime
     */
    public RedisLock(String key, String namespace, long expiredTime, long timeout, long retryTime) {
        if (StringUtils.isBlank(namespace)) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (int i = 1; i < trace.length; i++) {
                if (!this.getClass().getName().equals(trace[i].getClassName())) {
                    namespace = trace[i].getClassName();
                    break;
                }
            }
        }
        this.key = key;
        this.namespace = namespace;
        this.expiredTime = expiredTime;
        this.timeout = timeout;
        this.retryTime = retryTime;
    }

    /**
     * 获取锁，得不到锁立即返回
     *
     * @return
     */
    public boolean tryLock() {
        if(valueOperations == null) {
            throw new RuntimeException("valueOperations和redisTemplate不能为空");
        }
        long _expiredTime = System.currentTimeMillis() + expiredTime + 1;

        if (valueOperations.setIfAbsent(key, String.valueOf(_expiredTime))) {
            lockTime = _expiredTime;
            return true;
        } else {
            String currentValue = String.valueOf(valueOperations.get(key));
            //判断是否过期
            if (StringUtils.isBlank(currentValue)
                    || System.currentTimeMillis() > Long.valueOf(currentValue)) {
                _expiredTime = System.currentTimeMillis() + expiredTime + 1;

                //重新写入值，返回原来的值，主要是考虑两个事务的影响
                currentValue = String.valueOf(valueOperations.getAndSet(key, String.valueOf(_expiredTime)));
                //判断原来的值是否是超时的（其实是是否是原来的）
                if (StringUtils.isBlank(currentValue)
                        || System.currentTimeMillis() > Long.valueOf(currentValue)) {
                    lockTime = _expiredTime;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 得到锁返回设置的超时时间，得不到锁等待
     *
     * @return
     * @throws InterruptedException
     */
    public boolean lock() throws InterruptedException, RuntimeException {
        long startTime = System.currentTimeMillis();

        try {
            for (; ; ) {
                if (tryLock()) {
                    return true;
                } else {
                    Thread.sleep(retryTime);
                }
                if (System.currentTimeMillis() - startTime >= timeout) {
                    return false;
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw e;
        } catch (Exception e1) {
            throw new RuntimeException("获取redis锁异常", e1);
        }

    }

    /**
     * 释放锁
     */
    public boolean unlock() {
        if (lockTime == 0) {
            return false;
        }
        if (lockTime > System.currentTimeMillis()) {
            long currentValue = 0;
            try {
                currentValue = Long.valueOf(String.valueOf(valueOperations.get(key)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (lockTime == currentValue) {
                valueOperations.getOperations().delete(key);
            } else {
                return false;
            }
        }

        lockTime = 0;
        return true;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public ValueOperations<Object, Object> getValueOperations() {
        return valueOperations;
    }

    public void setValueOperations(ValueOperations<Object, Object> valueOperations) {
        this.valueOperations = valueOperations;
    }
}
