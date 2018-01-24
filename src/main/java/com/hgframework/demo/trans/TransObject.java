package com.hgframework.demo.trans;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransObject implements Serializable {

    //事务对象ID
    private String id;
    //事务对象名称
    private String name;
    //事务对象描述
    private String desc;
    //事务对象开始的时间
    private Date start;
    //事务对象结束对象
    private Date end;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "TransObject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
