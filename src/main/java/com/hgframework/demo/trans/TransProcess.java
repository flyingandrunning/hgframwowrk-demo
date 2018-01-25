package com.hgframework.demo.trans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransProcess {

    //事务上下文
    private TransContext context;

    //分布式事务配置中心，从中获取事务配置信息
    private DConfigCenter configCenter;

    private DataSourceFuntion<DataTransObject> dataSourceFuntion;
    private JdkDynamicProxy proxy;

    public TransProcess() {
        proxy = new JdkDynamicProxy(new DefaultDataSourceFuntion());
        this.dataSourceFuntion = (DataSourceFuntion<DataTransObject>) proxy.getProxy();
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
        String transId = metaData.getTransId();
        LinkedHashMap<String, TransEntry<?>> queue = metaData.getTransQueue();
        Set<String> queueKeys = queue.keySet();
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

    interface DataSourceFuntion<T extends TransObject> {

        void doInvoke(T t) throws Exception;
    }

    /**
     * aop 模式做事务隔离，对于外部是无感
     */
    static class DefaultDataSourceFuntion implements DataSourceFuntion<DataTransObject> {
        @Override
        public void doInvoke(DataTransObject transObject) throws Exception {

            System.out.println("初始化数据库链接");
            //查询数据分段
            System.out.println("查询数据分段");
            //数据进入分析容器
            System.out.println("数据进入分析容器");
            //从阶段做结束处理
            System.out.println("从阶段做结束处理");
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
            Object obj = Proxy.newProxyInstance(c, new Class[]{DataSourceFuntion.class}, this);
            return obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //做事务处理，事务各项阶段
            Object ret = null;
            try {
                //生成全局子事务编号，事务初始化
                //执行目标方法
                ret = method.invoke(target, args);
                //子事务提交，事务落地
                //结果值处理，事务收尾
            }catch (Exception e){
                //做异常处理
                //数据回滚等
            }catch (Error error){

            }
            return ret;
        }
    }


//    interface InnerProcess {
//
//        void commit();
//
//        void rollback();
//
//        void suspend();
//
//        void cancel();
//    }
//
//    static class DefaultInnerProcess implements InnerProcess {
//        @Override
//        public void commit() {
//            System.out.println("commit");
//        }
//
//        @Override
//        public void rollback() {
//            System.out.println("rollback");
//        }
//
//        @Override
//        public void suspend() {
//
//        }
//
//        @Override
//        public void cancel() {
//
//        }
//    }

    static TransMetaData initTransMetaData() {
        TransMetaData dTransMetaData = new TransMetaData();
        dTransMetaData.setStartTime(System.currentTimeMillis());
        dTransMetaData.setTransId(UUID.randomUUID().toString());
        dTransMetaData.setStatus(0);
        dTransMetaData.setPropagate(0);
        dTransMetaData.setTransQueue(new LinkedHashMap<>());
        //构建子事务对象1.
        TransEntry<TransObject> entry = new TransEntry<>();
        entry.setGlobalTransId(dTransMetaData.getTransId());
        entry.setEntryId(UUID.randomUUID().toString());
        entry.setTransStatus(0);
        DataTransObject dataTransObject = new DataTransObject();
        dataTransObject.driverName = "com.mysql";
        dataTransObject.hostname = "devtest";
        dataTransObject.setStart(new Date());
        entry.setTransObj(dataTransObject);
        dTransMetaData.getTransQueue().put(entry.getEntryId(), entry);
        //构建子事务对象2
        TransEntry<TransObject> entry2 = new TransEntry<>();
        entry2.setGlobalTransId(dTransMetaData.getTransId());
        entry2.setEntryId(UUID.randomUUID().toString());
        entry2.setTransStatus(0);
        DataTransObject dataTransObject2 = new DataTransObject();
        dataTransObject2.driverName = "com.mysql2";
        dataTransObject2.hostname = "devtest2";
        dataTransObject2.setStart(new Date());
        entry.setTransObj(dataTransObject2);
        dTransMetaData.getTransQueue().put(entry2.getEntryId(), entry2);
        dTransMetaData.setEndTime(System.currentTimeMillis());
        return dTransMetaData;
    }

    static void testProcess() {
        TransProcess transProcess = new TransProcess();
        TransContext context = new TransContext();
        TransMetaData transMetaData = initTransMetaData();
        try {
            transProcess.handler(context, transMetaData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    /**
//     * 通过反射机制，动态代理机制实现事务执行器
//     */
//    static class ProcessHandler implements InvocationHandler {
//        //代理目标对象
//        private Object target;
//
//        public ProcessHandler(Object target) {
//            this.target = target;
//        }
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            System.out.println("开始执行");
//            Object ret = method.invoke(target, args);
//            System.out.println("结束执行");
//            return ret;
//        }
//    }


    public static void main(String[] args) throws Exception {
        //获取代理的class对象，通过class对象生成构造器等
//        Class clazz = Proxy.getProxyClass(TransProcess.class.getClassLoader(), InnerProcess.class);
//        Constructor constructor= clazz.getConstructor(InvocationHandler.class);
//        InnerProcess process=constructor.newInstance(new )
//        InnerProcess innerProcess = new DefaultInnerProcess();
//        //通过代理类生成对象
//        InnerProcess process = (InnerProcess) Proxy.newProxyInstance(TransProcess.class.getClassLoader(), new Class[]{InnerProcess.class}, new ProcessHandler(innerProcess));
//        process.commit();
        testProcess();


    }

}
