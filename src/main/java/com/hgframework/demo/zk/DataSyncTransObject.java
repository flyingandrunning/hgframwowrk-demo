package com.hgframework.demo.zk;

import java.util.Date;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class DataSyncTransObject extends TransObject {

    private String name;

    private String desc;

    private Date execStart;

    private Date execEnd;

    private int splitNo;

    private Date splitStart;

    private Date splitEnd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getExecStart() {
        return execStart;
    }

    public void setExecStart(Date execStart) {
        this.execStart = execStart;
    }

    public Date getExecEnd() {
        return execEnd;
    }

    public void setExecEnd(Date execEnd) {
        this.execEnd = execEnd;
    }

    public int getSplitNo() {
        return splitNo;
    }

    public void setSplitNo(int splitNo) {
        this.splitNo = splitNo;
    }

    public Date getSplitStart() {
        return splitStart;
    }

    public void setSplitStart(Date splitStart) {
        this.splitStart = splitStart;
    }

    public Date getSplitEnd() {
        return splitEnd;
    }

    public void setSplitEnd(Date splitEnd) {
        this.splitEnd = splitEnd;
    }

    @Override
    public String toString() {
        return "DataSyncTransObject{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", execStart=" + execStart +
                ", execEnd=" + execEnd +
                ", splitNo=" + splitNo +
                ", splitStart=" + splitStart +
                ", splitEnd=" + splitEnd +
                '}';
    }
}
