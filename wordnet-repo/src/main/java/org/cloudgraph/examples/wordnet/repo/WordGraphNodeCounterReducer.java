package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.hbase.mapreduce.GraphReducer;

public class WordGraphNodeCounterReducer extends
    GraphReducer<Text, LongWritable, Text> {
	private static Log log = LogFactory.getLog(WordGraphNodeCounterReducer.class);

	/** Counter enumeration. */
	public static enum Counters {
		KEYS_STARTED, KEYS_PROCESSED, KEYS_FAILED,
	}

	public void reduce(Text key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException {

		
		try {
			int sum = 0;
			for (LongWritable val : values) {
				sum += val.get();
			}
			context.write(key, new LongWritable(sum));
			context.getCounter(Counters.KEYS_STARTED).increment(1);
			
			HashSet<String> seen = new HashSet<String>();
			if (seen.contains(key.toString())) {
				log.warn("duplicate key '"+String.valueOf(key)+"' in reducer");
			}
			else
			    seen.add(key.toString());
			
			//if (log.isDebugEnabled())
			    log.info(String.valueOf(key) + ": " + sum);
			
			
			 
			context.getCounter(Counters.KEYS_PROCESSED).increment(1);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			context.getCounter(Counters.KEYS_FAILED).increment(1);
		}
	}
	

}
