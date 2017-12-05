package com.hgframework.demo.cglib;

/**
 * Created by Administrator on 2017/12/5 0005.
 * 被代理类，即目标对象target
 */
public class BaseFun {

    /**
     * 可以认为是目标方法 在aop中可以认为是切点，真正aop所代理的方便，当然也可以为整个类
     */
    public void fun() {
        System.out.println("this is fun .....");
    }

    public Integer sum(Integer a, Integer b) {
        return (a + b);
    }
}
