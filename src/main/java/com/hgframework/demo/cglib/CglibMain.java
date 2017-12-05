package com.hgframework.demo.cglib;

/**
 * 可以参考 http://blog.csdn.net/danchu/article/details/70238002
 * Created by Administrator on 2017/12/5 0005.
 */
public class CglibMain {
    public static void main(String[] args) {
        CglibProxy proxy = new CglibProxy();
        // base为生成的增强过的目标类
        BaseFun base = CglibFactory.getInstance(proxy);
//        base.fun();
        Integer retval = base.sum(Integer.valueOf(5), Integer.valueOf(4));
        System.out.println(retval);
    }
}
