package com.hgframework.demo.trans;

import org.springframework.core.NamedThreadLocal;

import java.util.*;

/**
 * 这里建议引入泛型等
 * 事务同步管理器
 * Created by Administrator on 2018/1/25 0025.
 */
public abstract class TransSynchronizationManager {
    //事务资源管理器
    public static final ThreadLocal<Map<String, Object>> resources =
            new NamedThreadLocal<>("Transactional resources");
    //当前事务名称
    public static final ThreadLocal<String> currentTransactionName =
            new NamedThreadLocal<String>("Current transaction name");
    //只读事务处理
    public static final ThreadLocal<Boolean> currentTransactionReadOnly =
            new NamedThreadLocal<Boolean>("Current transaction read-only status");
    //隔离级别
    public static final ThreadLocal<Integer> currentTransactionIsolationLevel =
            new NamedThreadLocal<Integer>("Current transaction isolation level");
    //当前事务是否激活
    public static final ThreadLocal<Boolean> actualTransactionActive =
            new NamedThreadLocal<Boolean>("Actual transaction active");
    //事务同步器，用户事务辅助操作，例如：事务恢复，挂起等
    public static final ThreadLocal<Set<TransSynchronization>> synchronizations =
            new NamedThreadLocal<Set<TransSynchronization>>("Transaction synchronizations");

    public static final ThreadLocal<Queue<Object>> transQueue =
            new NamedThreadLocal<>("Transaction queue");


    public static void init(String name) {
        currentTransactionName.set(name);
        //这里同步数据，不存在隔离性，以及数据传播性，后面根据实际情况增加
        actualTransactionActive.set(true);
        synchronizations.set(new LinkedHashSet<>());
        resources.set(new LinkedHashMap<>());
        //子事务队列，保证各项事务正常执行
        transQueue.set(new ArrayDeque<>());
    }

    /**
     * 该资源是操作resources,这里资源可以用slot进行标识，后面根据实际情况进行设计
     * 另外可以在事务级别方式进行限流
     */
    public static void bindResource() {

    }


}
