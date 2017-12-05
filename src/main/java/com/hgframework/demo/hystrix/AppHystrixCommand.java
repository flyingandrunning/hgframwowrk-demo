package com.hgframework.demo.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 参考问 http://hot66hot.iteye.com/blog/2155036
 * 以命令的形式实现逻辑封装
 * Created by Administrator on 2017/11/29 0029.
 */
public class AppHystrixCommand extends HystrixCommand<String> {
    private final String name;

    public AppHystrixCommand(String name) {
        //最少配置:指定命令组名(CommandGroup)
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HelloWorldPool"))
                /* 配置依赖超时时间,500毫秒*/
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500)));
//        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    @Override
    protected String run() throws InterruptedException {
//        TimeUnit.MILLISECONDS.sleep(1000);
        // 依赖逻辑封装在run()方法中
        return "Hello " + name + " thread:" + Thread.currentThread().getName();
    }

    @Override
    protected String getFallback() {
        return "execute failed";
    }

    /**
     * 命令模式进行调用
     *
     * @throws Exception
     */
    private static void commandModelInvoke() throws Exception {
        //每个Command对象只能调用一次,不可以重复调用,
        //重复调用对应异常信息:This instance can only be executed once. Please instantiate a new instance.
        AppHystrixCommand helloWorldCommand = new AppHystrixCommand("Synchronous-hystrix");
        //使用execute()同步调用代码,效果等同于:helloWorldCommand.queue().get();
        String result = helloWorldCommand.execute();
        System.out.println("result=" + result);

        helloWorldCommand = new AppHystrixCommand("Asynchronous-hystrix");
        //异步调用,可自由控制获取结果时机,
        Future<String> future = helloWorldCommand.queue();
        //get操作不能超过command定义的超时时间,默认:1秒
        result = future.get(100, TimeUnit.MILLISECONDS);
        System.out.println("result=" + result);
        System.out.println("mainThread=" + Thread.currentThread().getName());
    }

    /**
     * 注册异步事件回调执行,以观察者模式
     *
     * @throws Exception
     */
    private static void observaleModelInvoke() throws Exception {

        //注册观察者事件拦截
        Observable<String> fs = new AppHystrixCommand("World").observe();
        //注册结果回调事件
        fs.subscribe(new Action1<String>() {
            @Override
            public void call(String result) {
                //执行结果处理,result 为HelloWorldCommand返回的结果
                System.out.println("mainThread=" + Thread.currentThread().getName());
                //用户对结果做二次处理.
                System.out.println(result);
            }
        });

        //注册完整执行生命周期事件
        fs.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                // onNext/onError完成之后最后回调
                System.out.println("mainThread=" + Thread.currentThread().getName());
                System.out.println("execute onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                // 当产生异常时回调
                System.out.println("onError " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                // 获取结果后回调
                System.out.println("onNext: " + v);
            }
        });
    }

    //调用实例
    public static void main(String[] args) throws Exception {
        commandModelInvoke();
//        observaleModelInvoke();
        //当前线程退出CPU,让其他线程执行
        System.out.println("mainThread=" + Thread.currentThread().getName());
        //发起为主线程，主线程调用子线程，整个过程是main线程全部完成后退出
        Thread.currentThread().join();

    }

}
