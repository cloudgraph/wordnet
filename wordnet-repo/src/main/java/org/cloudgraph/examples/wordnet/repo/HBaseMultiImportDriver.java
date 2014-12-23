package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.cloudgraph.hbase.service.CloudGraphContext;
import org.cloudgraph.mapreduce.GraphXmlInputFormat;

/**
 * hadoop jar /home/lib/wordnet-repo-0.5.1.jar -libjars ${LIBJARS} /tmp/wordnetxml/wordnet-ref.xml /tmp/wordnetxml/wordnet-synsets.xml /tmp/wordnetxml/wordnet-words.xml /tmp/wordnetxml/wordnet-links.xml   
 */
public class HBaseMultiImportDriver {
	private static Log log = LogFactory.getLog(HBaseMultiImportDriver.class);

	/*
	 * Prints usage without error message
	 */
	private static void printUsage() {
		System.err.println("Usage: " + HBaseMultiImportDriver.class.getSimpleName());
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
		
		long milliSeconds = 1000*60*15; // 15 minutes rather than default 10 min
		conf.setLong("mapred.task.timeout", milliSeconds);
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length == 0) {
			printUsage("Wrong number of parameters: " + args.length);
			System.exit(-1);
		}
		try {
			runJob(conf, otherArgs);

		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public static void runJob(Configuration conf, String[] otherArgs) throws IOException {

		conf.set(
				"io.serializations",
				"org.apache.hadoop.io.serializer.JavaSerialization,org.apache.hadoop.io.serializer.WritableSerialization");

		for (String input : otherArgs) {
			Job job = new Job(conf, HBaseMultiImportDriver.class.getSimpleName());
			FileInputFormat.setInputPaths(job, input);
			job.setJarByClass(HBaseMultiImportDriver.class);
			job.setMapperClass(HBaseImportMapper.class);	
			job.setInputFormatClass(GraphXmlInputFormat.class);
			job.getConfiguration().setLong("mapred.max.split.size", 1000000); // 1M rather than default 64M
			job.setNumReduceTasks(0);
			job.setOutputKeyClass(NullWritable.class);
			job.setOutputValueClass(NullWritable.class);
			job.setOutputFormatClass(org.apache.hadoop.mapreduce.lib.output.NullOutputFormat.class);
			Path outPath = new Path("/tmp/wordnetout"); // no output but Hadoop demands an output path
			FileOutputFormat.setOutputPath(job, outPath);
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

	}

}
