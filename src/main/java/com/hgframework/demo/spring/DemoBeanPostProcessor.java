package com.hgframework.demo.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/2/13 0013.
 */
@Component
public class DemoBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean 初始化之前处理 查询到的 bean 名称为： " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean 初始化之后处理 查询到的 bean 名称为： " + beanName);
        return bean;
    }
}
