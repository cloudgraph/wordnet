package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.examples.wordnet.model.query.QWords;
import org.cloudgraph.hbase.mapreduce.GraphMapReduceSetup;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.plasma.query.model.Query;

/**
 */
public class WordGraphNodeCounterDriver {
	private static Log log = LogFactory.getLog(WordGraphNodeCounterDriver.class);

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + "hadoop jar /home/lib/wordnet-repo-0.5.2.jar " + 
	        WordGraphNodeCounterDriver.class.getName() + " -libjars ${LIBJARS}");
	}
	
	/*
	 * @param errorMessage Can attach a message when error occurs.
	 */
	private static void printUsage(String errorMessage) {
		System.err.println("ERROR: " + errorMessage);
		printUsage();
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Configuration conf = CloudGraphContext.instance().getConfig();
		
		conf.set("mapred.child.java.opts", "-Xms256m -Xmx2g -XX:+UseSerialGC");
		conf.set("mapred.job.map.memory.mb", "4096");
		conf.set("mapred.job.reduce.memory.mb", "1024");
		
		//long milliSeconds = 1000*60*20; // 60 minutes rather than default 10 min
		//conf.setLong("mapred.task.timeout", milliSeconds);
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 0) {
			printUsage("Wrong number of parameters: " + args.length);
			System.exit(-1);
		}
		try {
			runJob(conf);

		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public static void runJob(Configuration conf) throws IOException {


		Job job = new Job(conf, WordGraphNodeCounterDriver.class.getSimpleName());
		job.setJarByClass(WordGraphNodeCounterDriver.class);

		Query queryIn = createInputQuery();
	    GraphMapReduceSetup.setupGraphMapperJob(queryIn,
	    		WordGraphNodeCounterMapper.class, Text.class,
				LongWritable.class, job);
		job.setNumReduceTasks(1);
		job.setReducerClass(WordGraphNodeCounterReducer.class);
		
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		Path outPath = new Path("/tmp/wordnet/graphcount"); // no output but Hadoop demands an output path
		TextOutputFormat.setOutputPath(job, outPath);
		FileSystem dfs = FileSystem.get(outPath.toUri(), conf);
		if (dfs.exists(outPath)) {
			dfs.delete(outPath, true); // watch it this works
		}

		try {
			job.waitForCompletion(true);

		} catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
		} catch (ClassNotFoundException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Returns minimal page info with the document body which stores XML serialized
	 * dependencies. 
	 * @return the query
	 */
	public static Query createInputQuery() {
		QWords query = QWords.newQuery();
		query.select(query.wildcard());
		return query.getModel();
	}
}
