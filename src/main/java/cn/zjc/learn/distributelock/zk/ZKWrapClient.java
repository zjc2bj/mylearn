package cn.zjc.learn.distributelock.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.DebugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此客户端是对apache curator client的简单封装
 * 负责zk 启动、关闭
 * @author wuwenqi
 */
public class ZKWrapClient {
    private static final Logger logger = LoggerFactory.getLogger(ZKWrapClient.class);
    private final AtomicInteger REFERENCE_COUNT = new AtomicInteger(0);
    private final CuratorFramework client;

    public ZKWrapClient(String zkAddress){
        System.setProperty(DebugUtils.PROPERTY_DONT_LOG_CONNECTION_ISSUES, "false");
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                .connectionTimeoutMs(5000).build();
        start();
    }

    /***
     * 启动客户端
     */
    private void start(){
        final CountDownLatch latch = new CountDownLatch(1);
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if(connectionState == ConnectionState.CONNECTED){
                    latch.countDown();
                }
            }
        });
        client.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("start zk latch.await() error", e);
            Thread.currentThread().interrupt();
        }
    }

    /***
     * 关闭客户端
     */
    public void close(){
        logger.info("Call close of ZKClient, reference count is: {}", REFERENCE_COUNT.get());
        if(REFERENCE_COUNT.decrementAndGet() == 0){
            CloseableUtils.closeQuietly(client);
            logger.info("ZKClient is closed");
        }
    }

    /***
     * 判断客户端连接是否正常
     * @return  boolean
     */
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    public CuratorFramework getClient() {
        return client;
    }

    /***
     * 将当前客户端引用自增
     */
    protected void incrementReference() {
        REFERENCE_COUNT.incrementAndGet();
    }
}
