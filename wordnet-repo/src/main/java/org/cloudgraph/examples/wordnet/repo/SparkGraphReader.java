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
import org.cloudgraph.common.concurrent.GraphMetricVisitor;
import org.cloudgraph.mapreduce.GraphWritable;
import org.cloudgraph.mapreduce.GraphXmlInputFormat;
import org.plasma.sdo.PlasmaDataGraph;

import scala.Tuple2;

/**
 * ./bin/spark-submit --master spark://u16551142.onlinehome-server.com:7077 --class org.cloudgraph.examples.wordnet.repo.SparkGraphReader /home/lib/wordnet-repo-0.5.2.jar hdfs://u16551142.onlinehome-server.com:9000/tmp/wordnetxml/wordnet-links.xml
 */
public class SparkGraphReader {
    private static Log log = LogFactory.getLog(SparkGraphReader.class);
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {
    	
    	log.info("main()");
    	System.setProperty("hadoop.home.dir","/home/hadoop/hadoop-1.0.2");
    	
        SparkConf sparkConf = new SparkConf().setAppName("SparkGraphReader");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
    	log.info("setup context");       
    	
     	Configuration conf = jsc.hadoopConfiguration();
		Job job = new Job(conf);
		FileInputFormat.setInputPaths(job, args[0]);
		
    	log.info("setup conf");
        JavaPairRDD<LongWritable, GraphWritable> rdd = jsc.newAPIHadoopRDD(job.getConfiguration(),  
        		GraphXmlInputFormat.class, 
        		LongWritable.class, GraphWritable.class);
         
        JavaRDD<Long> something = rdd.map(new Function<Tuple2<LongWritable, GraphWritable>, Long>() {
			@Override
			public Long call(Tuple2<LongWritable, GraphWritable> tuple)
					throws Exception {
				PlasmaDataGraph graph = (PlasmaDataGraph)tuple._2.getDataGraph();
		    	GraphMetricVisitor visitor = new GraphMetricVisitor();
		    	graph.accept(visitor);
		    	log.info(tuple._1.get() + " - graph nodes: " 
		    	    + visitor.getCount() + " depth: " + visitor.getDepth());
				return 1L;
			}			         	
        });
        
        //MUST have a collector or job won't even start
        List<Long> ids = something.collect();
        for (Long id : ids)
        	log.info("ID: " + id);
        
        jsc.stop();
    }
}
