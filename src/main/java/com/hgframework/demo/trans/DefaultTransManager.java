package com.hgframework.demo.trans;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/1/26 0026.
 */
public class DefaultTransManager implements TransManager {

    //当前真正执行的元信息
    private TransMetaData metaData;
    //事务状态配置中心
    private DConfigCenter configCenter;
    //提交远程事务配置失败后，保留本地，等待网络通畅后再次提交
    private ConcurrentHashMap<String, TransMetaData> failCommitPool = new ConcurrentHashMap<>();

    @Override
    public TransMetaData getTrans() {
        //是否需生成事务元信息待考虑，先完成原型即可
        return this.metaData;
    }

    @Override
    public void commit(TransMetaData metaData, TransEntry entry) {
        entry.setTransStatus(TransEntry.SUCC);
        if (metaData.isAllSucc()) {
            metaData.setStatus(TransMetaData.SUCC);
        }
        //更新事务
        try {
            this.configCenter.put(metaData.getTransId(), metaData);
        } catch (Exception e) {
            e.printStackTrace();
            //这可能会导致远程事务长期挂起，或者长事务,这里需要配置重试策略
            this.failCommitPool.put(metaData.getTransId(), metaData);
        }


    }

    @Override
    public void rollback(TransMetaData metaData, TransEntry entry) {
        //无需支持回滚
    }
}
