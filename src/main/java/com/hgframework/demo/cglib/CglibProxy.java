package com.hgframework.demo.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 此为代理类，用于在pointcut处添加advise
 * CglibProxy 可以认为是一个切面 (待考虑)，
 * <p>
 * Created by Administrator on 2017/12/5 0005.
 */
public class CglibProxy implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        /**
         * 内部方法的线性调用构成的调用栈为Joinpoint，将整个aop内部方法连打通，所谓的aop概念在体现出来。
         */
        // 添加切面逻辑（advise），此处是在目标类代码执行之前，即为MethodBeforeAdviceInterceptor。
        System.out.println("before-------------");
        // 执行目标类add方法 方法需要根据实际情况进行转换
        Object retval = proxy.invokeSuper(obj, args);
        if (retval instanceof Void) {
            return null;
        }
        // 添加切面逻辑（advise），此处是在目标类代码执行之后，即为MethodAfterAdviceInterceptor。
        System.out.println("after--------------");
        //以下调用会导致递归死循环，
//        Object retval = method.invoke(obj, args);
//        System.out.println(retval);

        return retval;
    }
}
