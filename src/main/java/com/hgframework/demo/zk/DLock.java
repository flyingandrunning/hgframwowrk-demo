package com.hgframework.demo.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2018/1/23 0023.
 */
public class DLock {

    private static final String ZK_HOST = "devtest.node3.com:2181,devtest.node3.com:2181,devtest.node3.com:2181";
    private static final String PATH = "/dt/trans/dev/dlocks/locks";

    private InterProcessMutex lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public DLock(CuratorFramework client, String lockPath, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        this.lock = new InterProcessMutex(client, lockPath);
    }

    public void doWork(long time, TimeUnit unit) throws Exception {
        if (!lock.acquire(time, unit)) {
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }
        try {
            System.out.println(clientName + " get the lock");
            resource.use(); //access resource exclusively
        } finally {
            System.out.println(clientName + " releasing the lock");
            lock.release(); // always release the lock in a finally block
        }
    }

    private static final int QTY = 5;
    private static final int REPETITIONS = QTY * 10;

    public static void main(String[] args) throws Exception {
        final FakeLimitedResource resource = new FakeLimitedResource();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        try {
            for (int i = 0; i < QTY; ++i) {
                final int index = i;
                service.submit(() -> {
                    CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_HOST, new ExponentialBackoffRetry(1000, 3));
                    try {
                        client.start();
                        final DLock example = new DLock(client, PATH, resource, "Client " + index);
                        for (int j = 0; j < REPETITIONS; ++j) {
                            example.doWork(10, TimeUnit.SECONDS);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    } finally {
                        CloseableUtils.closeQuietly(client);
                    }
                });
            }
            service.shutdown();
            service.awaitTermination(10, TimeUnit.MINUTES);
        } finally {

        }
    }

    static class FakeLimitedResource {
        private final AtomicBoolean inUse = new AtomicBoolean(false);

        public void use() throws InterruptedException {
            // 真实环境中我们会在这里访问/维护一个共享的资源
            //这个例子在使用锁的情况下不会非法并发异常IllegalStateException
            //但是在无锁的情况由于sleep了一段时间，很容易抛出异常
            if (!inUse.compareAndSet(false, true)) {
                throw new IllegalStateException("Needs to be used by one client at a time");
            }
            try {
                Thread.sleep((long) (3 * Math.random()));
            } finally {
                inUse.set(false);
            }
        }
    }


}
