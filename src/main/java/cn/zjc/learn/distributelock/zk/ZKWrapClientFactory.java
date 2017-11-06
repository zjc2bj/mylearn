package cn.zjc.learn.distributelock.zk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ZK Client静态工厂
 * @author wuwenqi
 */
public final class ZKWrapClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(ZKWrapClientFactory.class);

    private static final Map<String, ZKWrapClient> CACHE = Maps.newConcurrentMap();
    private final static Lock lock = new ReentrantLock();

    private ZKWrapClientFactory(){}

    /***
     * 根据给定的ZK地址获取ZKClient
     * @param zkAddress zk地址
     * @return  ZKWrapperClient
     */
    public static ZKWrapClient create(String zkAddress) throws RuntimeException{
        logger.info("use the zkAddress fetch zk client", zkAddress);
        Preconditions.checkArgument(StringUtils.isNotBlank(zkAddress), "zk address must not be null");
        try{
            lock.lockInterruptibly();
            ZKWrapClient zkWrapperClient = CACHE.get(zkAddress);
            if(zkWrapperClient == null){
                logger.info("the init build zk client with zkAddress [{}]",zkAddress);
                CACHE.put(zkAddress,new ZKWrapClient(zkAddress));
            }
            zkWrapperClient = CACHE.get(zkAddress);
            zkWrapperClient.incrementReference();
            return zkWrapperClient;
        }catch (Exception e){
            logger.error("fetch zk client with address occur exception",e);
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        try {
            ZKWrapClient zkWrapClient = create("localhost:2181");
            System.out.println(zkWrapClient.isConnected() + ".............");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
