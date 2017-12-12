package com.hgframework.demo.rxjava;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/11/29 0029.
 */
public class RxHelloWorld {
    public static void main(String[] args) {
//        Flowable.just("Hello world").subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) throws Exception {
//                System.out.println(s);
//            }
//        });
        //lambda 编程 ([...参数])->{}
//        Flowable.just("hi").subscribe((s) -> {
//            System.out.println("hi");
//        });
        //牛逼的函数式编程

        Flowable.range(1, 100)
                .flatMap(v -> Flowable.just(v).subscribeOn(Schedulers.computation()))
                .map(w -> w * w).blockingSubscribe(s -> System.out.println(s));

//        Flowable.interval(100,TimeUnit.MICROSECONDS).onBackpressureBuffer()
        rx.Observable.interval(100,TimeUnit.SECONDS).onBackpressureBuffer();

//        Flowable.fromPublisher(s -> {
//            System.out.println(s);
//        });
    }
}
