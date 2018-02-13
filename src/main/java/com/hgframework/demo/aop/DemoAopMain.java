package com.hgframework.demo.aop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

@SpringBootApplication(scanBasePackages = {"com.hgframework.demo.spring","com.hgframework.demo.aop"})
public class DemoAopMain {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoAopMain.class, args);
    }
}

