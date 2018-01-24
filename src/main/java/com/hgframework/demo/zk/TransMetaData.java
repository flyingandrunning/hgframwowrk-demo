package com.hgframework.demo.zk;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransMetaData implements Serializable{

    //全局事务Id uuid标识
    private String transId;
    //事务开始时间
    private long startTime = System.currentTimeMillis();
    //分布式事务执行队列，以map方式标识
    private LinkedHashMap<String, TransEntry<?>> transQueue;
    //事务结束的时间
    private long endTime;
    //事务状态，0:初始化,1:执行中,2:挂起中,3:成功,4:回滚
    private int status;
    /*
    *   1、PROPAGATION_REQUIRED：如果当前没有事务，就创建一个新事务，如果当前存在事务，就加入该事务，该设置是最常用的设置。
        2、PROPAGATION_SUPPORTS：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务执行。‘
        3、PROPAGATION_MANDATORY：支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常。
        4、PROPAGATION_REQUIRES_NEW：创建新事务，无论当前存不存在事务，都创建新事务。
        5、PROPAGATION_NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
        6、PROPAGATION_NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。
        7、PROPAGATION_NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION_REQUIRED类
    * */
    //事务传播性  0;无需事务，1新建事务，2.延用当前事务，如果没有创建一个，3.嵌套事务,这里无需完全严格遵循以上特性
    private int propagate;

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public LinkedHashMap<String, TransEntry<?>> getTransQueue() {
        return transQueue;
    }

    public void setTransQueue(LinkedHashMap<String, TransEntry<?>> transQueue) {
        this.transQueue = transQueue;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPropagate() {
        return propagate;
    }

    public void setPropagate(int propagate) {
        this.propagate = propagate;
    }

    @Override
    public String toString() {
        return "DTransMetaData{" +
                "transId='" + transId + '\'' +
                ", startTime=" + startTime +
                ", transQueue=" + transQueue +
                ", endTime=" + endTime +
                ", status=" + status +
                ", propagate=" + propagate +
                '}';
    }
}
