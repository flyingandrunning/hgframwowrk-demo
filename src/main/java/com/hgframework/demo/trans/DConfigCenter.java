package com.hgframework.demo.trans;

import com.alibaba.fastjson.JSON;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.*;
import java.util.*;

/**
 * Created by Administrator on 2018/1/23 0023.
 */
public class DConfigCenter {

    //数据目录
    private static final String STORE_PATH = "/auto/sync/dev/para1";
    //分布式锁路径
    private static final String STORE_LOCKS = "/auto/sync/dev/locks";

    private static final String ZK_HOST = "devtest.node3.com:2181,devtest.node3.com:2181,devtest.node3.com:2181";
    //zk客户端
    private CuratorFramework curatorFramework;
    //采用路径模式保存配置子信息
    private PathChildrenCache pathChildrenCache;
    //参数池
    private Map<String, TransMetaData> parameterPools = new HashMap<>();
    //读写锁控制
    private InterProcessReadWriteLock rwlock;

    public void init() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10000, 3);
        this.curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("config/center")
                .build();
        this.curatorFramework.start();
        //路径初始化
        pathChildrenCache = new PathChildrenCache(curatorFramework, STORE_PATH, true);

        this.pathChildrenCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    innerAdd(event);
                    break;
                case INITIALIZED:
                    //初始化，加载相关配置信息,且刷新本地缓存
                    break;
                case CHILD_REMOVED:
                    //节点del，从本地del相关数据信息
                    String removePath = event.getData().getPath();
                    if (STORE_PATH.startsWith(removePath)) {
                        String remoteKey = removePath.substring(removePath.lastIndexOf("/"));
                        parameterPools.remove(remoteKey);
                    }
                    break;
                case CHILD_UPDATED:
                    innerAdd(event);
                    break;
                case CONNECTION_RECONNECTED:
                    //重新连接，刷新本地缓存数据
                    break;
            }
        });
        this.pathChildrenCache.start(PathChildrenCache.StartMode.NORMAL);
        //初始化锁
        this.rwlock = new InterProcessReadWriteLock(this.curatorFramework, STORE_LOCKS);
    }

    private void innerAdd(PathChildrenCacheEvent event) throws Exception {
        //本质已经包含子路径
        String remotePath = event.getData().getPath();
        if (remotePath.startsWith(STORE_PATH)) {
            //更新本地cache
            String remoteKey = remotePath.substring(remotePath.lastIndexOf("/"));
            if (event.getData() != null) {
                TransMetaData transMetaData = (TransMetaData) SerializableUtils
                        .decode(event.getData().getData());
                parameterPools.put(remoteKey, transMetaData);
            }
        }
    }

    public void put(String key, TransMetaData value) throws Exception {
        String removeKey = builderKey(key);
        //本地数据同步
        this.parameterPools.put(key, value);
        try {
            this.rwlock.writeLock().acquire();  //写入到zk
            Stat stat = this.curatorFramework.checkExists().forPath(removeKey);
            byte[] bytes = SerializableUtils.encode(value);
            if (null != stat) {
                this.curatorFramework.setData().forPath(removeKey, bytes);
                return;
            }
            //如果数据不存在，创建新节点做持久化处理
            this.curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(removeKey, bytes);
        } finally {
            this.rwlock.writeLock().release();
        }


    }

    public TransMetaData get(String key, boolean isRemote) throws Exception {
        if (!isRemote) {
            return this.parameterPools.get(key);
        }
        try {
            this.rwlock.readLock().acquire();
            //从远程获取数据信息
            String removeKey = builderKey(key);
            Stat stat = this.curatorFramework.checkExists().forPath(removeKey);
            if (null == stat) {
                return null;
            }
            byte[] bytes = this.curatorFramework.getData().forPath(removeKey);
            TransMetaData transMetaData = (TransMetaData) SerializableUtils.decode(bytes);
            //更新本地缓存
            this.parameterPools.put(key, transMetaData);
            return transMetaData;
        } finally {
            this.rwlock.readLock().release();
        }

    }


    /**
     * 删除key
     *
     * @param key
     * @throws Exception
     */
    public void remove(String key) throws Exception {
        //不一定能del成功，本地不一定有远程key,可能有算法同步问题,网络延迟问题
        this.parameterPools.remove(key);
        try {
            this.rwlock.writeLock().acquire();
            String removeKey = builderKey(key);
            Stat stat = this.curatorFramework.checkExists().forPath(removeKey);
            if (stat != null) {
                this.curatorFramework.delete().guaranteed().forPath(removeKey);
            }
        } finally {
            this.rwlock.writeLock().release();
        }


    }

    public void close() throws Exception {
        this.pathChildrenCache.close();
        this.curatorFramework.close();
    }

    private static String builderKey(String key) {
        StringBuffer sb = new StringBuffer();
        sb.append(STORE_PATH).append("/").append(key);
        return sb.toString();
    }


    /**
     * 对象序列化
     */
    static class SerializableUtils {
        /**
         * 编码处理
         * 被编码的对象必须实现 Serializable接口
         *
         * @param object
         * @return
         * @throws IOException
         */
        public static byte[] encode(Object object) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            return bos.toByteArray();
        }

//        public static byte[] encodeJson(Object o) throws IOException {
//            String json = JSON.toJSONString(o);
//            return json.getBytes();
//        }
//        public static Object decodeJson(byte[] bytes){
//            return JSON.p
//        }

        /**
         * 解码处理
         *
         * @param bytes
         * @return
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public static Object decode(byte[] bytes) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object object = ois.readObject();
            //其实没有价值
            bis.close();
            ois.close();
            return object;
        }
    }

    public static void main(String[] args) throws Exception {
//        testPut();
        testGet();
//        Thread.currentThread().join();
    }

    public static void testPut() throws Exception {
        TransMetaData dTransMetaData = new TransMetaData();
        dTransMetaData.setStartTime(System.currentTimeMillis());
        dTransMetaData.setTransId(UUID.randomUUID().toString());
        dTransMetaData.setStatus(0);
        dTransMetaData.setPropagate(0);
        dTransMetaData.setTransQueue(new LinkedHashMap<>());
        TransEntry<DataSyncTransObject> entry = new TransEntry<>();
//        entry.setGobalTransId(dTransMetaData.getTransId());
        entry.setGlobalTransId(dTransMetaData.getTransId());
        entry.setEntryId(UUID.randomUUID().toString());
        entry.setTransStatus(0);
        DataSyncTransObject dataSyncTransObject = new DataSyncTransObject();
        dataSyncTransObject.setDesc("xxx");
        dataSyncTransObject.setExecStart(new Date());
        dataSyncTransObject.setName("xxxb");
        dataSyncTransObject.setExecEnd(new Date());
        dataSyncTransObject.setSplitStart(new Date());
        dataSyncTransObject.setSplitNo(1);
        dataSyncTransObject.setSplitEnd(new Date());
        entry.setTransObj(dataSyncTransObject);
        dTransMetaData.getTransQueue().put(entry.getEntryId(), entry);
        dTransMetaData.setEndTime(System.currentTimeMillis());
//        byte[] bytes = SerializableUtils.encode(dTransMetaData);
//        TransMetaData decodeDTransMetaData = (TransMetaData) SerializableUtils.decode(bytes);
//        System.out.println(decodeDTransMetaData);
        DConfigCenter configCenter = new DConfigCenter();
        configCenter.init();
        configCenter.put("trans_id2", dTransMetaData);
//        TransMetaData transMetaData=configCenter.get("trans_id",true);
//        System.out.println(transMetaData);
        configCenter.close();
    }

    public static void testGet() throws Exception {
        DConfigCenter configCenter = new DConfigCenter();
        configCenter.init();
        TransMetaData transMetaData = configCenter.get("trans_id2", true);
        System.out.println("远程数据配置信息如下:\n" + transMetaData);
        configCenter.close();
    }


}
