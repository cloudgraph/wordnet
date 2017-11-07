package org.cloudgraph.examples.wordnet.repo;

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

import scala.Tuple2;

/**
 * ./bin/spark-submit --master spark://u16551142.onlinehome-server.com:7077 --class org.cloudgraph.examples.wordnet.repo.SimpleHadoopReader /home/lib/wordnet-repo-0.5.2.jar
 */
public class SimpleHadoopReader {
    private static Log log = LogFactory.getLog(SimpleHadoopReader.class);
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {
    	
    	log.info("main()");
    	System.setProperty("hadoop.home.dir","/home/hadoop/hadoop-1.0.2");
    	
        SparkConf sparkConf = new SparkConf().setAppName("SimpleHadoopReader");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        //jsc.textFile("hdfs://u16551142.onlinehome-server.com:9000/tmp/wordnetxml/wordnet-ref.xml");
    	log.info("setup context");
        
    	
     	Configuration conf = jsc.hadoopConfiguration();
		Job job = new Job(conf);
		FileInputFormat.setInputPaths(job, "hdfs://u16551142.onlinehome-server.com:9000/user/root/examples/input-data/text/data.txt");
		
    	log.info("setup conf");
        JavaPairRDD<LongWritable, Text> rdd = jsc.newAPIHadoopRDD(job.getConfiguration(),  
        		TextInputFormat.class, 
        		LongWritable.class, Text.class);
         
        JavaRDD<Long> something = rdd.map(new Function<Tuple2<LongWritable, Text>, Long>() {
			@Override
			public Long call(Tuple2<LongWritable, Text> tuple)
					throws Exception {
				log.info("TEST: " + new String(tuple._2.getBytes()));
				return 1L;
			}			         	
        });
        
        List<Long> fff = something.collect();
        for (Long ggg : fff)
        	log.info("EATIT: " + ggg);
        
    	
    	/* 
    	Configuration conf = jsc.hadoopConfiguration();
    	Job job = new Job(conf);
    	job.setInputFormatClass(TextInputFormat.class);
        log.info("conf: " + conf); 
        
        Path inputPathPattern = new Path("/user/root/examples/input-data/text/data.txt");
        
        FileSystem fs = FileSystem.get(conf);
        
        if (!fs.exists(inputPathPattern))
        {
        	log.info("path does not exist: " + inputPathPattern);
        	//return;
        }
        else {
        FileStatus[] inputPathStatuses = fs.globStatus(inputPathPattern);
        log.info("file count: " + inputPathStatuses.length);
        for (FileStatus fstat : inputPathStatuses)
        	log.info("file stat: " + fstat.getPath());
        FileInputFormat.setInputPaths(job, inputPathPattern);
        } 
        
        DistributedFileSystem dfs = new DistributedFileSystem();
        dfs.initialize(new URI("hdfs://u16551142.onlinehome-server.com:9000"), new Configuration());
        Path path = new Path("/user/root/examples/input-data/text/data.txt");
        if (!dfs.exists(path))
        	log.info("FFFFFFF path does not exist: " + inputPathPattern);
        else
        	log.info("FFFF");
        FileInputFormat.setInputPaths(job, path);
        	
        	
        //Configuration conf = new Configuration();
    	log.info("setup conf");

        JavaPairRDD<LongWritable, Text> rdd = jsc.newAPIHadoopRDD(conf,  
        		TextInputFormat.class, 
        		LongWritable.class, Text.class);
        log.info("created RDD AAAA");
        log.info(rdd);
        */ 
             
        /*
        rdd.map(new Function<Tuple2<LongWritable, Text>, Long>() {
			@Override
			public Long call(Tuple2<LongWritable, Text> tuple)
					throws Exception {
				log.info("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF call()");
				String foo = null;
				foo.toCharArray();
				
				return 1L;
			}
			         	
        });
    	*/
        
        jsc.stop();
    	/*
        JavaPairRDD<LongWritable, GraphWritable> rdd = jsc.newAPIHadoopRDD(conf, 
        	GraphXmlInputFormat.class, 
        	LongWritable.class, GraphWritable.class);
    	log.info("created RDD");
        try {
        rdd.map(new Function<Tuple2<LongWritable, GraphWritable>, Long>() {
			@Override
			public Long call(Tuple2<LongWritable, GraphWritable> tuple)
					throws Exception {
				log.info("call()");
				try {
					
				PlasmaDataGraph graph = (PlasmaDataGraph)tuple._2.getDataGraph();
		    	GraphMetricVisitor visitor = new GraphMetricVisitor();
		    	graph.accept(visitor);
		    	log.info(tuple._1.get() + " - graph nodes: " + visitor.getCount() + " depth" + visitor.getDepth());
		    	
				return tuple._1.get();
				}
				catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
				return new Long(-1);
			}        	
        });
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
        */
    }
}
