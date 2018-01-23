package com.hgframework.demo.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/1/23 0023.
 */
public class CuratorDemo {

    private static final String ZK_HOST = "devtest.node3.com:2181,devtest.node3.com:2181,devtest.node3.com:2181";

    private static final int SESSION_TIMEOUT = 5000;

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
//        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/trans/dev/2","hello world".getBytes());
        Stat stat=client.checkExists().forPath("/trans");
        System.out.println(stat);
//        client.create().creatingParentsIfNeeded()
//                .withMode(CreateMode.EPHEMERAL)
//                .inBackground().forPath("/trans/dev/1","hello world".getBytes());
//        String data = new String(client.getData().forPath("/trans/dev/2"));
//        client.delete().guaranteed().deletingChildrenIfNeeded().forPath("/trans");
//        System.out.println(data);



    }

    public static void main(String[] args) throws Exception {
        init();
//        //重试策略，初试时间1秒，重试10次
//        RetryPolicy policy = new ExponentialBackoffRetry(1000, 10);
//        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(ZK_HOST)
//                .sessionTimeoutMs(SESSION_TIMEOUT).retryPolicy(policy).build();
//        //开启连接
//        curator.start();
//        ExecutorService executor = Executors.newCachedThreadPool();
//
//        /**创建节点，creatingParentsIfNeeded()方法的意思是如果父节点不存在，则在创建节点的同时创建父节点；
//         * withMode()方法指定创建的节点类型，跟原生的Zookeeper API一样，不设置默认为PERSISTENT类型。
//         * */
//        curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
//                .inBackground((framework, event) -> { //添加回调
//                    System.out.println("Code：" + event.getResultCode());
//                    System.out.println("Type：" + event.getType());
//                    System.out.println("Path：" + event.getPath());
//                }, executor).forPath("/super/c1", "c1内容".getBytes());
//
////        Thread.sleep(5000); //为了能够看到回调信息
////        String data = new String(curator.getData().forPath("/super/c1")); //获取节点数据
//        String data = null;
////        System.out.println(data);
//        Stat stat = curator.checkExists().forPath("/super/c1"); //判断指定节点是否存在
//
//        System.out.println(stat);
//        curator.setData().forPath("/super/c1", "c1新内容".getBytes()); //更新节点数据
//        data = new String(curator.getData().forPath("/super/c1"));
//
//        System.out.println(data);
//        List<String> children = curator.getChildren().forPath("/super"); //获取子节点
//        for (String child : children) {
//            System.out.println(child);
//        }
//        //放心的删除节点，deletingChildrenIfNeeded()方法表示如果存在子节点的话，同时删除子节点
//        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
//        curator.close();
    }
}



