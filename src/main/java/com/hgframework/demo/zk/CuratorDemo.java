package com.hgframework.demo.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;


/**
 * https://www.jianshu.com/p/70151fc0ef5d
 * Created by Administrator on 2018/1/23 0023.
 */
public class CuratorDemo {

    private static final String ZK_HOST = "devtest.node3.com:2181,devtest.node3.com:2181,devtest.node3.com:2181";
    private static final String PATH = "/dt/trans/dev/path/cache";
    private static final String NODE_PATH = "/dt/trans/dev/node/cache";
    private static final String TREE_PATH = "/dt/trans/dev/tree/cache";

    private static final int SESSION_TIMEOUT = 5000;

    /**
     * Zookeeper原生支持通过注册Watcher来进行事件监听，但是开发者需要反复注册(Watcher只能单次注册单次使用)。Cache是Curator中对事件监听的包装，
     * 可以看作是对事件监听的本地缓存视图，能够自动为开发者处理反复注册监听。Curator提供了三种Watcher(Cache)来监听结点的变化。
     * <p>
     * Path Cache用来监控一个ZNode的子节点. 当一个子节点增加， 更新，删除时， Path Cache会改变它的状态， 会包含最新的子节点， 子节点的数据和状态，
     * 而状态的更变将通过PathChildrenCacheListener通知。
     * 实际使用时会涉及到四个类：
     * PathChildrenCache
     * PathChildrenCacheEvent
     * PathChildrenCacheListener
     * ChildData
     * 想使用cache，必须调用它的start方法，使用完后调用close方法。 可以设置StartMode来实现启动的模式，
     * StartMode有下面几种：
     * NORMAL：正常初始化。
     * BUILD_INITIAL_CACHE：在调用start()之前会调用rebuild()。
     * POST_INITIALIZED_EVENT： 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
     * public void addListener(PathChildrenCacheListener listener)可以增加listener监听缓存的变化。
     * getCurrentData()方法返回一个List<ChildData>对象，可以遍历所有的子节点。
     * 设置/更新、移除其实是使用client (CuratorFramework)来操作, 不通过PathChildrenCache操作：
     * <p>
     *
     * @throws Exception
     */
    private static void initPathCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory
                .newClient(ZK_HOST, new ExponentialBackoffRetry(1000, 3));
        client.start();
        //如果new PathChildrenCache(client, PATH, true)中的参数cacheData值设置为false，则示例中的event.getData().getData()、data.getData()将返回null，cache将不会缓存节点数据。
        PathChildrenCache cache = new PathChildrenCache(client, PATH, true);
        cache.start();
        cache.getListenable().addListener((c, event) -> {
            System.out.println("类型为" + event.getType());
            if (event.getData() != null) {
                System.out.println(new String(event.getData().getData()));
            }
        });
        client.create().creatingParentsIfNeeded().forPath("/dt/trans/dev/path/cache/test03", "01".getBytes());
        Thread.sleep(100);
        client.create().creatingParentsIfNeeded().forPath("/dt/trans/dev/path/cache/test04", "02".getBytes());
        Thread.sleep(100);
        client.setData().forPath("/dt/trans/dev/path/cache/test03", "01_V2".getBytes());
        Thread.sleep(100);
        for (ChildData data : cache.getCurrentData()) {
            System.out.println("getCurrentData:" + data.getPath() + " = " + new String(data.getData()));
        }
        client.delete().forPath("/dt/trans/dev/path/cache/test03");
        //线程休眠对数据显示有关系，和内部实现有关系
        Thread.sleep(100);
        client.delete().forPath("/dt/trans/dev/path/cache/test04");
        Thread.sleep(1000 * 5);
        cache.close();
        client.close();
        System.out.println("OK!");
    }

    /**
     * Node Cache
     * <p>
     * Node Cache与Path Cache类似，Node Cache只是监听某一个特定的节点。它涉及到下面的三个类：
     * <p>
     * NodeCache - Node Cache实现类
     * NodeCacheListener - 节点监听器
     * ChildData - 节点数据
     * 注意：使用cache，依然要调用它的start()方法，使用完后调用close()方法。
     * <p>
     * getCurrentData()将得到节点当前的状态，通过它的状态可以得到当前的值。
     */
    private static void initNodeCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_HOST, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.create().creatingParentsIfNeeded().forPath(NODE_PATH);
        final NodeCache cache = new NodeCache(client, NODE_PATH);
        NodeCacheListener listener = () -> {
            ChildData data = cache.getCurrentData();
            if (null != data) {
                System.out.println("节点数据：" + new String(cache.getCurrentData().getData()));
            } else {
                System.out.println("节点被删除!");
            }
        };
        cache.getListenable().addListener(listener);
        cache.start();
        client.setData().forPath(NODE_PATH, "01".getBytes());
        Thread.sleep(100);
        client.setData().forPath(NODE_PATH, "02".getBytes());
        Thread.sleep(100);
        client.delete().deletingChildrenIfNeeded().forPath(NODE_PATH);
        Thread.sleep(1000 * 2);
        cache.close();
        client.close();
        System.out.println("OK!");
    }

    /**
     * Tree Cache可以监控整个树上的所有节点，类似于PathCache和NodeCache的组合，主要涉及到下面四个类：
     * <p>
     * TreeCache - Tree Cache实现类
     * TreeCacheListener - 监听器类
     * TreeCacheEvent - 触发的事件类
     * ChildData - 节点数据
     * 注意：在此示例中没有使用Thread.sleep(10)，但是事件触发次数也是正常的。
     * <p>
     * 注意：TreeCache在初始化(调用start()方法)的时候会回调TreeCacheListener实例一个事TreeCacheEvent，
     * 而回调的TreeCacheEvent对象的Type为INITIALIZED，ChildData为null，此时event.getData().getPath()很有可能导致空指针异常，
     * 这里应该主动处理并避免这种情况。
     *
     * @throws Exception
     */
    private static void initTreeCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_HOST, new ExponentialBackoffRetry(1000, 3));
        client.getConnectionStateListenable().addListener((client1, newState) -> {
            System.out.println("xxx"+newState);
        });
        client.start();
        client.create().creatingParentsIfNeeded().forPath(TREE_PATH);
        TreeCache cache = new TreeCache(client, TREE_PATH);
        TreeCacheListener listener = (client1, event) ->
                System.out.println("事件类型：" + event.getType() +
                        " | 路径：" + (null != event.getData() ? event.getData().getPath() : null));
        cache.getListenable().addListener(listener);
        cache.start();
        client.setData().forPath(TREE_PATH, "01".getBytes());
        Thread.sleep(100);
        client.setData().forPath(TREE_PATH, "02".getBytes());
        Thread.sleep(100);
        client.delete().deletingChildrenIfNeeded().forPath(TREE_PATH);
        Thread.sleep(1000 * 2);
        cache.close();
        client.close();
        System.out.println("OK!");


    }

    private static void init() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ZK_HOST)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("dt")
                .build();
        //启动客户端
        client.start();
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/trans/dev/2", "hello world".getBytes());
        //检测节点是否存在
//        Stat stat = client.checkExists().forPath("/trans");
//        System.out.println(stat);
//        client.create().creatingParentsIfNeeded()
//                .withMode(CreateMode.PERSISTENT)
//                .inBackground().forPath("/trans/dev/1", "hello world".getBytes());
//        String data = new String(client.getData().forPath("/trans/dev/1"));
//        System.out.println(data);
        //做节点安全del
//        client.delete().guaranteed().deletingChildrenIfNeeded().forPath("/trans");
//        System.out.println(data);
        //做监听处理
        client.getCuratorListenable().addListener((curatorClient, event) -> {
            System.out.println("path is " + event.getPath());
            if (event.getData() != null) {
                System.out.println(new String(event.getData()));
            }
            System.out.println(event.getType());

        });
//        client.setData().forPath("/trans/dev/1", "更新数据++".getBytes());
//        Thread.sleep(2000);
        client.setData().forPath("/trans/dev/2", ("又一次更新数据" + System.currentTimeMillis() + "++").getBytes());

//        client.delete().deletingChildrenIfNeeded().forPath("/trans/dev/1");
//        Thread.sleep(50000);
        client.close();


    }

    public static void main(String[] args) throws Exception {
//        initPathCache();
//        initNodeCache();
        initTreeCache();
    }
}



