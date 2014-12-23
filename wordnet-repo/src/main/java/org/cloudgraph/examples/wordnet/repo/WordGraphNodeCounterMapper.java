package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.config.ConfigurationProperty;
import org.cloudgraph.config.QueryFetchType;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.examples.wordnet.model.query.QWords;
import org.cloudgraph.hbase.mapreduce.GraphMapper;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.cloudgraph.mapreduce.GraphWritable;
import org.plasma.query.Query;
import org.plasma.sdo.core.CoreNode;

import commonj.sdo.DataGraph;


public class WordGraphNodeCounterMapper extends GraphMapper<Text, LongWritable> {
	private static Log log = LogFactory.getLog(WordGraphNodeCounterMapper.class);
	private GraphServiceDelegate service;


	public WordGraphNodeCounterMapper() {
		this.service = new GraphServiceDelegate();
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		WORDS_STARTED, WORDS_PROCESSED, WORDS_FAILED,
	}
	

	@Override
	public void map(ImmutableBytesWritable row, GraphWritable graph,
			Context context) throws IOException {
		
		context.getCounter(Counters.WORDS_STARTED).increment(1);
		
		try { 
			Words word = (Words)graph.getDataGraph().getRootObject();
			Query query = createRelationQuery(word.getLemma());
			query.addConfigurationProperty(
				ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value(), 
				QueryFetchType.PARALLEL.value());
			
			DataGraph[] graphs = this.service.find(createRelationQuery(word.getLemma()), context);
			CoreNode wordGraphRoot = (CoreNode)graphs[0].getRootObject();
	    	Long assemTime = (Long)wordGraphRoot.getValueObject().get(
	        		CloudGraphConstants.GRAPH_ASSEMBLY_TIME);
	    	Long threadCount = (Long)wordGraphRoot.getValueObject().get(
	        		CloudGraphConstants.GRAPH_THREAD_COUNT);
	    	Long nodeCount = (Long)wordGraphRoot.getValueObject().get(
	        		CloudGraphConstants.GRAPH_NODE_COUNT);
			log.info(word.getLemma() + "\t" + nodeCount);
			Text text = new Text(word.getLemma());
			context.write(text, new LongWritable(nodeCount));
			
			context.getCounter(Counters.WORDS_PROCESSED).increment(1);

		} catch (Throwable t) {
			context.getCounter(Counters.WORDS_FAILED).increment(1);
			log.info(t.getMessage(), t);
		}
	}
	
	private Query createRelationQuery(String lemma) {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		    .select(query.casedwords().wildcard())
		    .select(query.senses().wildcard())
		    .select(query.senses().synsets().wildcard())
		    .select(query.senses().synsets().samples().wildcard())
		    .select(query.senses().synsets().adjpositions().wildcard())		    
		    // semantic relations
		    .select(query.senses().synsets().semlinks().wildcard())  
		    .select(query.senses().synsets().semlinks().linktypes().wildcard())  		    
		    .select(query.senses().synsets().semlinks().synsets().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().semlinks().synsets1().senses().words().wildcard())  
		    // lexical relations
		    .select(query.senses().synsets().lexlinks().wildcard())  
		    .select(query.senses().synsets().lexlinks().linktypes().wildcard())
		    .select(query.senses().synsets().lexlinks().synsets().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().samples().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().wildcard())  
		    .select(query.senses().synsets().lexlinks().synsets1().senses().words().wildcard())  
		;
		query.where(query.lemma().eq(lemma));
		
		return query;
	}
	
}
