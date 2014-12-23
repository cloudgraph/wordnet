package org.cloudgraph.examples.wordnet.repo;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

public class SparkGraphReader {
    private static Log log = LogFactory.getLog(SparkGraphReader.class);

    public static void main(String[] args) throws Exception {
    	
    	log.info("main()");
    	System.setProperty("hadoop.home.dir","/home/hadoop/hadoop-1.0.2");
    	
        SparkConf sparkConf = new SparkConf().setAppName("SparkGraphReader");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        //jsc.textFile("hdfs://u16551142.onlinehome-server.com:9000/tmp/wordnetxml/wordnet-ref.xml");
    	log.info("setup context");
        
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
