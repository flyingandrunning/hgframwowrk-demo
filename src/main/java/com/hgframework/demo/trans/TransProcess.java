package com.hgframework.demo.trans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransProcess {

    //事务上下文
    private TransContext context;
    //分布式事务配置中心，从中获取事务配置信息
    private DConfigCenter configCenter;
    //数据源功能操作
    private DataSourceFunction<DataTransObject> dataSourceFuntion;
    //动态代理
    private JdkDynamicProxy proxy;
    //事务挂起池，挂起或者失败池,本功能主要面向查询，回滚性比较弱，失败的事务再次推进即可，如果三次失败
    //转入手工处理
    private ConcurrentHashMap<String, TransMetaData> failTransPool = new ConcurrentHashMap<>();

    private TransManager transManager;

    public TransProcess() {
        this.configCenter = new DConfigCenter();
        proxy = new JdkDynamicProxy(new DefaultDataSourceFuntion());
        this.dataSourceFuntion = (DataSourceFunction<DataTransObject>) proxy.getProxy();
        this.transManager = new DefaultTransManager();
    }


    //事务状态管理
    //事务推进 commit,
    //事务回滚，原子性独立

    /**
     * 执行任务链
     *
     * @param context
     * @param metaData
     */
    public void handler(TransContext context, TransMetaData metaData) throws Exception {
        //这里加入同步事务处理
        String transId = metaData.getTransId();
        //事务资源初始化
        TransSynchronizationManager.init(transId);
        LinkedHashMap<String, TransEntry<?>> queue = metaData.getTransQueue();
        Set<String> queueKeys = queue.keySet();
        //入事务队列，分配事务slot，这块需要事务容器支持，后期优化
        for (String key : queueKeys) {
            TransEntry entry = queue.get(key);
            TransSynchronizationManager.transQueue.get().offer(entry);
        }
        //执行事务
        for (String key : queueKeys) {
            TransEntry entry = queue.get(key);
            this.dataSourceFuntion.doInvoke((DataTransObject) entry.getTransObj());
        }

    }

    static class DataTransObject extends TransObject {

        String userName;
        String userPwd;
        String hostname;
        int port;
        String driverName;
        String sql;
        //记录数，用于做事务验证
        int counter;
    }

    interface DataSourceFunction<T extends TransObject> {

        void doInvoke(T t) throws Exception;
    }

    /**
     * aop 模式做事务隔离，对于外部是无感
     */
    static class DefaultDataSourceFuntion implements DataSourceFunction<DataTransObject> {
        private static Random random = new Random();

        @Override
        public void doInvoke(DataTransObject transObject) throws Exception {
            System.out.println("执行具体的业务操作................................");
            //时间加大会引起执行错误，进行事务回滚
            Thread.sleep(1000 * random.nextInt(10));
        }
    }


    static class JdkDynamicProxy implements InvocationHandler {

        //代理目标对象
        private Object target;

        public JdkDynamicProxy(Object target) {
            this.target = target;
        }

        /**
         * 根据接口生成代理对象
         *
         * @return
         */
        public Object getProxy() {
            ClassLoader c = Thread.currentThread().getContextClassLoader();
            Object obj = Proxy.newProxyInstance(c, new Class[]{DataSourceFunction.class}, this);
            return obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //做事务处理，事务各项阶段
            Object ret = null;
            try {
                //生成全局子事务编号，事务初始化,初始化已经做顺序处理
                TransEntry entry = (TransEntry) TransSynchronizationManager.transQueue.get().poll();
                System.out.println(entry);
                //执行目标方法
                ret = method.invoke(target, args);
//                System.out.println("事务提交");
                //子事务提交，事务落地
                //结果值处理，事务收尾
            } catch (Exception e) {
                //做异常处理
                e.printStackTrace();
                System.out.println("事务回滚");
                //数据回滚等
            } catch (Error error) {
                error.printStackTrace();
                System.out.println("事务回滚");
            }
            return ret;
        }
    }


    static TransMetaData initTransMetaData(int subTransSize) {
        TransMetaData dTransMetaData = new TransMetaData();
        dTransMetaData.setStartTime(System.currentTimeMillis());
        dTransMetaData.setTransId(UUID.randomUUID().toString());
        dTransMetaData.setStatus(0);
        dTransMetaData.setPropagate(0);
        dTransMetaData.setTransQueue(new LinkedHashMap<>());
        for (int i = 0; i < subTransSize; i++) {
            TransEntry<TransObject> entry = new TransEntry<>();
            entry.setGlobalTransId(dTransMetaData.getTransId());
            entry.setEntryId(UUID.randomUUID().toString());
            entry.setTransStatus(0);
            DataTransObject dataTransObject = new DataTransObject();
            dataTransObject.driverName = "com.mysql";
            dataTransObject.hostname = "devtest";
            dataTransObject.port = 3306;
            dataTransObject.setDesc("分布式事务测试");
            dataTransObject.setName("分布式子事务: " + i);
            dataTransObject.setId(UUID.randomUUID().toString());
            dataTransObject.setStart(new Date());
            entry.setTransObj(dataTransObject);
            dTransMetaData.getTransQueue().put(entry.getEntryId(), entry);
        }
        dTransMetaData.setEndTime(System.currentTimeMillis());
        return dTransMetaData;
    }

    static void testProcess() {
        TransProcess transProcess = new TransProcess();
        TransContext context = new TransContext();
        TransMetaData transMetaData = initTransMetaData(5);
        try {
            transProcess.handler(context, transMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void testThreadTrans(int threads) {
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                testProcess();
            }).start();
        }
    }

    public static void main(String[] args) throws Exception {
        //获取代理的class对象，通过class对象生成构造器等
//        Class clazz = Proxy.getProxyClass(TransProcess.class.getClassLoader(), InnerProcess.class);
//        Constructor constructor= clazz.getConstructor(InvocationHandler.class);
//        InnerProcess process=constructor.newInstance(new )
//        InnerProcess innerProcess = new DefaultInnerProcess();
//        //通过代理类生成对象
//        InnerProcess process = (InnerProcess) Proxy.newProxyInstance(TransProcess.class.getClassLoader(), new Class[]{InnerProcess.class}, new ProcessHandler(innerProcess));
//        process.commit();
//        testProcess();
        testThreadTrans(100);

    }

}
