package com.hgframework.demo.flink.hdfs;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.fs.StringWriter;
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink;
import org.apache.flink.streaming.connectors.fs.bucketing.DateTimeBucketer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

/**
 * Created by Administrator on 2017/12/15 0015.
 */
public class KafkaToHdfs {

    private static String HDFS_PATH = "hdfs://devtest.node2.com:8020/dev/flink";

    public static void main(String[] args) throws Exception {
        LocalStreamEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
        env.enableCheckpointing(5000); // create a checkpoint every 5 seconds
        Properties prop = new Properties();
        prop.put("bootstrap.servers", "devtest.node3.com:9092,devtest.node3.com:9092,devtest.node3.com:9092");
        prop.put("zookeeper.connect", "devtest.node3.com:2181,devtest.node3.com:2181,devtest.node3.com:2181");
        prop.put("group.id", "flink.event.id");


        FlinkKafkaConsumer010 flinkKafkaConsumer010 = new FlinkKafkaConsumer010("dev.dw.aries.etl.op.active", new SimpleStringSchema(), prop);
        DataStream<String> text = env.addSource(flinkKafkaConsumer010);

        String dfsPath = HDFS_PATH + "/org/data";
        BucketingSink<String> sink = new BucketingSink<>(dfsPath);
        sink.setBucketer(new DateTimeBucketer<>("yyyymmdd"));
        sink.setWriter(new StringWriter<>());
        text.addSink(sink);
        env.execute("同步HDFS数据");

//        DataStream<Word> counts =
//                // split up the lines into Word objects
//                text.flatMap(new Tokenizer())
//                        // group by the field word and sum up the frequency
//                        .keyBy("productLine").sum("sum");
//
//    }
    }
}
