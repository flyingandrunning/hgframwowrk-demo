package com.hgframework.demo.trans;

import org.springframework.core.NamedThreadLocal;

import java.util.Map;

/**
 * 事务同步管理器
 * Created by Administrator on 2018/1/25 0025.
 */
public abstract class TransSynchronizationManager {
    //事务资源管理器
    private static final ThreadLocal<Map<String, Object>> resources =
            new NamedThreadLocal<>("Transactional resources");

    private static final ThreadLocal<String> currentTransactionName =
            new NamedThreadLocal<String>("Current transaction name");
    //只读事务处理
    private static final ThreadLocal<Boolean> currentTransactionReadOnly =
            new NamedThreadLocal<Boolean>("Current transaction read-only status");
    //隔离级别
    private static final ThreadLocal<Integer> currentTransactionIsolationLevel =
            new NamedThreadLocal<Integer>("Current transaction isolation level");
    //当前事务是否激活
    private static final ThreadLocal<Boolean> actualTransactionActive =
            new NamedThreadLocal<Boolean>("Actual transaction active");




}
