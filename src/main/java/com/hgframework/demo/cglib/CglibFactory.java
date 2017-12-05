package com.hgframework.demo.cglib;

import net.sf.cglib.proxy.Enhancer;

/**
 * Created by Administrator on 2017/12/5 0005.
 */
public class CglibFactory {

    /**
     * 工厂类，生成增强过的目标类（已加入切入逻辑）
     * @param proxy
     * @return
     */
    public static BaseFun getInstance(CglibProxy proxy) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(BaseFun.class);
        //回调方法的参数为代理类对象CglibProxy，最后增强目标类调用的是代理类对象CglibProxy中的intercept方法
        enhancer.setCallback(proxy);
        // 此刻，base不是单纯的目标类，而是增强过的目标类
        BaseFun base = (BaseFun) enhancer.create();
        return base;
    }
}
