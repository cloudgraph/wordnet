package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.tools.ant.types.Mapper;
import org.cloudgraph.examples.wordnet.model.Lexlinks;
import org.cloudgraph.examples.wordnet.model.LinkAggregate;
import org.cloudgraph.examples.wordnet.model.Linktypes;
import org.cloudgraph.examples.wordnet.model.Semlinks;
import org.cloudgraph.examples.wordnet.model.Senses;
import org.cloudgraph.examples.wordnet.model.Synsets;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.examples.wordnet.model.query.QLinktypes;
import org.cloudgraph.examples.wordnet.model.query.QWords;
import org.cloudgraph.hbase.mapreduce.GraphInputFormat;
import org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.cloudgraph.mapreduce.GraphWritable;
import org.cloudgraph.mapreduce.GraphXmlInputFormat;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaDataGraph;














import org.plasma.sdo.PlasmaDataGraphVisitor;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import scala.Tuple2;

/**
 * Reads Plasma data graphs from HDFS and simply unmarshalls them using GraphWritable and Spark. 
 * Run this class using the below command line putting in the classpath created from the
 * instructions in the pom.xml under maven-assembly-plugin. 
 * 
 * ./bin/spark-submit --master spark://u16551142.onlinehome-server.com:7077 --jars [comma separated classpath] --class org.cloudgraph.examples.wordnet.repo.SparkRelationCounter /home/lib/wordnet-repo-0.5.2.jar
 */
public class SparkRelationCounter {
    private static Log log = LogFactory.getLog(SparkRelationCounter.class);
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {
    	
    	log.info("main()");
    	System.setProperty("hadoop.home.dir","/home/hadoop/hadoop-1.0.2");
    	
        SparkConf sparkConf = new SparkConf().setAppName("SparkRelationCounter");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
    	log.info("setup context");       
    	
    	// create a spark-hadoop config and merge in
    	// any cloudgraph settings
     	Configuration conf = jsc.hadoopConfiguration();
		HBaseConfiguration.merge(conf, 
				CloudGraphContext.instance().getConfig());
		
		//conf.set("mapred.child.java.opts", "-Xms256m -Xmx2g -XX:+UseSerialGC");
		//conf.set("mapred.job.map.memory.mb", "4096");
		//conf.set("mapred.job.reduce.memory.mb", "1024");
		
		//long milliSeconds = 1000*60*15; // 15 minutes rather than default 10 min
		//conf.setLong("mapred.task.timeout", milliSeconds);
     	
     	
		Job job = new Job(conf);
		//FileInputFormat.setInputPaths(job, "hdfs://u16551142.onlinehome-server.com:9000/tmp/wordnetxml/wordnet-links.xml");
		
		
		Query queryIn = createInputQuery();
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
	    		GraphMapper.class, Text.class,
				LongWritable.class, job);		
		
    	log.info("setup conf - creating RDD");
     	String quorum2 = job.getConfiguration().get("hbase.zookeeper.quorum");
     	log.info("quorum2: " + quorum2);
     	
        JavaPairRDD<ImmutableBytesWritable, GraphWritable> rdd = jsc.newAPIHadoopRDD(job.getConfiguration(),  
        		GraphInputFormat.class, 
        		ImmutableBytesWritable.class, GraphWritable.class);
        /*
		// For each word in the RDD (returned as a one node graph), get the full relation graph
        // for the word/lemma and emit counts of each found link type
        // for the word as PairRDD's using [word].[linktype] as the key, e.g. beautiful.holonymn
        JavaPairRDD<String, Integer> pairs = rdd.flatMapToPair(
            new PairFlatMapFunction<Tuple2<ImmutableBytesWritable, GraphWritable>, String, Integer>() {
				private static final long serialVersionUID = 1L;
				@Override
				public Iterable<Tuple2<String, Integer>> call(
						Tuple2<ImmutableBytesWritable, GraphWritable> tuple)
						throws Exception {
					Words word = (Words)tuple._2.getDataGraph().getRootObject();
					Query relationQuery = createRelationQuery(word.getLemma());
					GraphServiceDelegate service = new GraphServiceDelegate();
					DataGraph[] results = service.find(relationQuery, null);
					word = (Words)results[0].getRootObject();
					List<Tuple2<String, Integer>> result = new ArrayList<Tuple2<String, Integer>>(); 
					return result;
				}
				
				private Query createRelationQuery(String lemma) {
					QWords query = QWords.newQuery();
					query.select(query.wildcard())
					    .select(query.senses().synsets().semlinks().linktypes().wildcard())  		    
					    .select(query.senses().synsets().semlinks().synsets().senses().words().wildcard())  
					    .select(query.senses().synsets().semlinks().synsets1().senses().words().wildcard())  
					    .select(query.senses().synsets().lexlinks().linktypes().wildcard())
					    .select(query.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
					    .select(query.senses().synsets().lexlinks().synsets1().senses().words().wildcard())  
					;
					query.where(query.lemma().eq(lemma));
					
					return query.getModel();
				}
       });
       */ 
        
        JavaRDD<Long> something = rdd.map(new Function<Tuple2<ImmutableBytesWritable, GraphWritable>, Long>() {
			@Override
			public Long call(Tuple2<ImmutableBytesWritable, GraphWritable> tuple)
					throws Exception {
				Words word = (Words)tuple._2.getDataGraph().getRootObject();
				// get the full relation graph
				Query relationQuery = createRelationQuery(word.getLemma());
				GraphServiceDelegate service = new GraphServiceDelegate();
				DataGraph[] results = service.find(relationQuery, null);
				word = (Words)results[0].getRootObject();	
				//log.info(word.dump());
				LinkCounter counter = new LinkCounter(word);
				Map<String, Integer> map = counter.getMap();
				Iterator<String> iter = map.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					Integer count = map.get(key);
					LinkAggregate agg = findAggregate(word, key);
					if (agg == null) {
						agg = word.createLinkAggregate();
					    agg.setLinktypes(getLinkType(service, key));
					}
					agg.setCount(count);
				}
				service.commit(word.getDataGraph(), null);
				log.info("commit: " + word.getLemma() + ": " + map.toString());
				return 1L;
			}
			
			private LinkAggregate findAggregate(Words word, String linkId) {
				for (LinkAggregate agg : word.getLinkAggregate()) {
					if (agg.getLinktypes().getLink().equals(linkId))
						return agg;
				}
				return null;	
			}
			
			private Query createRelationQuery(String lemma) {
				QWords word = QWords.newQuery();
				word.select(word.wildcard())
				    .select(word.linkAggregate().wildcard())
				    .select(word.linkAggregate().linktypes().wildcard())
				    .select(word.senses().synsets().semlinks().linktypes().wildcard())  		    
				    .select(word.senses().synsets().semlinks().synsets().senses().words().wildcard())  
				    .select(word.senses().synsets().semlinks().synsets1().senses().words().wildcard())  
				    .select(word.senses().synsets().lexlinks().linktypes().wildcard())
				    .select(word.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
				    .select(word.senses().synsets().lexlinks().synsets1().senses().words().wildcard())  
				;
				word.where(word.lemma().eq(lemma));
				
				return word.getModel();
			}
			
			Map<String, Linktypes> linkCache = new HashMap<String, Linktypes>();
			private Linktypes getLinkType(GraphServiceDelegate service, String linkId) throws IOException
			{
				if (linkCache.containsKey(linkId)) {
					Linktypes result = linkCache.get(linkId);
					result.setDataGraph(null); // so can re parent
				}
				DataGraph[] results = service.find(createLinkTypeQuery(linkId), null);
				if (results.length == 0)
					throw new RuntimeException("no link type found for id: " + linkId);
				Linktypes result = (Linktypes)results[0].getRootObject();
				result.setDataGraph(null); // so can re parent
				linkCache.put(linkId, result);
				return result;
			}
			
			private Query createLinkTypeQuery(String linkId) {
				QLinktypes query = QLinktypes.newQuery();
				query.select(query.wildcard());
				query.where(query.link().eq(linkId));
				return query.getModel();
			}
        });
        
        //MUST have a collector or job won't even start
        List<Long> ids = something.collect();
        for (Long id : ids)
        	log.info("ID: " + id);
        
        jsc.stop();
    }
    
	private static Query createInputQuery() {
		QWords query = QWords.newQuery();
		query.select(query.lemma());
		//query.where(query.lemma().like("beaut*"));
		return query.getModel();
	}
	
	
}
