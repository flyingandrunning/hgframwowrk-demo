package com.hgframework.demo.trans;

/**
 * Created by Administrator on 2018/1/26 0026.
 */
public interface TransManager {

    /**
     * 获取事务元信息
     * @return
     */
    TransMetaData getTrans();

    /**
     *
     * @param metaData
     * @param entry 当前子事务
     */
    void commit(TransMetaData metaData, TransEntry entry);

    /**
     *
     * @param metaData
     * @param entry 当前子事务
     */
    void rollback(TransMetaData metaData, TransEntry entry);

}
