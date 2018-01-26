package com.hgframework.demo.trans;

import java.io.Serializable;

/**
 * 事务分录，可以认为就是子事务
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransEntry<T extends TransObject> implements Serializable {

    public static final int SUCC = 3;

    //全局事务Id
    private String globalTransId;
    //子事务ID
    private String entryId;

    //事务状态，0:初始化,1:执行中,2:挂起中,3:成功,4:回滚
    private int transStatus;

    //事务对象，有客户端或者调用方处理，定义即所用
    private T transObj;


    public String getGlobalTransId() {
        return globalTransId;
    }

    public void setGlobalTransId(String globalTransId) {
        this.globalTransId = globalTransId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public int getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(int transStatus) {
        this.transStatus = transStatus;
    }

    public T getTransObj() {
        return transObj;
    }

    public void setTransObj(T transObj) {
        this.transObj = transObj;
    }

    @Override
    public String toString() {
        return "TransEntry{" +
                "globalTransId='" + globalTransId + '\'' +
                ", entryId='" + entryId + '\'' +
                ", transStatus=" + transStatus +
                ", transObj=" + transObj +
                '}';
    }
}
