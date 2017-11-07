package org.cloudgraph.examples.wordnet.repo;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;
import org.cloudgraph.common.concurrent.GraphMetricVisitor;
import org.cloudgraph.mapreduce.GraphWritable;
import org.cloudgraph.mapreduce.GraphXmlInputFormat;
import org.plasma.sdo.PlasmaDataGraph;

import com.google.common.collect.Lists;

import scala.Tuple2;

/**
 * ./bin/spark-submit --master spark://u16551142.onlinehome-server.com:7077 --class org.cloudgraph.examples.wordnet.repo.SparkFlumeReader /home/lib/wordnet-repo-0.5.2.jar
 */
public class SparkFlumeReader {
    private static Log log = LogFactory.getLog(SparkFlumeReader.class);
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {
    	
    	log.info("main()");
    	System.setProperty("hadoop.home.dir","/home/hadoop/hadoop-1.0.2");
    	String host = args[0];
    	int port = Integer.parseInt(args[1]);
   	
        SparkConf sparkConf = new SparkConf().setAppName("SparkFlumeReader");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        Duration batchInterval = new Duration(2000);
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, batchInterval);
        ssc.checkpoint("/home/spark/checkpoint"); 
    	log.info("setup context");       
 
        final Broadcast<String> broadcastTableName = ssc.sparkContext().broadcast("foo");
        final Broadcast<String> broadcastColumnFamily = ssc.sparkContext().broadcast("bar");

         
        JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils.createStream(ssc, host, port);
        JavaDStream<String> words = flumeStream.flatMap(new FlatMapFunction<SparkFlumeEvent,String>(){
            @Override
            public Iterable<String> call(SparkFlumeEvent arg0) throws Exception {
                String body = new String(arg0.event().getBody().array(), Charset.forName("UTF-8"));
                return Lists.newArrayList(SPACE.split(body));
            }
        });    	
    	
        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
                new PairFunction<String, String, Integer>() {
                  @Override
                  public Tuple2<String, Integer> call(String s) {
                    return new Tuple2<String, Integer>(s, 1);
                  }
                }).reduceByKey(new Function2<Integer, Integer, Integer>() {
                  @Override
                  public Integer call(Integer i1, Integer i2) {
                    return i1 + i2;
                  }
                });

        wordCounts.print();
        
        wordCounts.foreach(new Function2<JavaPairRDD<String,Integer>, Time, Void>() {

            @Override
            public Void call(JavaPairRDD<String, Integer> values,
                    Time time) throws Exception {

                values.foreach(new VoidFunction<Tuple2<String, Integer>> () {

                    @Override
                    public void call(Tuple2<String, Integer> tuple){
                        System.out.println("===========insert record========"+tuple._1()+"=="+tuple._2().toString());
                        //JavaFlumeEventTest.addRecord("mytable","PutInpu",columnFamily,tuple._1(),tuple._2().toString());
                        System.out.println("===========Done record========"+tuple._1());
                    }} );

                return null;
            }});

        flumeStream.count().map(new Function<Long, String>() {
          @Override
          public String call(Long in) {
            return "Received " + in + " flume events.";
          }
        }).print();

        ssc.start();  
    }
}
