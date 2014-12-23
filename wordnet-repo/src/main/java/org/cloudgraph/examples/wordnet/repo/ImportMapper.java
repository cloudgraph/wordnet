package org.cloudgraph.examples.wordnet.repo;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.cloudgraph.examples.wordnet.model.Lexlinks;
import org.cloudgraph.examples.wordnet.model.Semlinks;
import org.cloudgraph.examples.wordnet.model.Synsets;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.mapreduce.GraphService;
import org.cloudgraph.mapreduce.GraphWritable;
import org.cloudgraph.mapreduce.GraphXmlMapper;
import org.plasma.sdo.PlasmaChangeSummary;

import commonj.sdo.DataObject;


public abstract class ImportMapper extends GraphXmlMapper<LongWritable, GraphWritable>  {
	private static Log log = LogFactory.getLog(ImportMapper.class);
	protected GraphService service;

	private Context context;
	private ImportSupport support;
	
	public ImportMapper() {
		this.support = new ImportSupport();
	}

	/** Counter enumeration to count the actual rows. */
	public static enum Counters {
		RECORDS_INPUT,
		RECORDS_SUCCESS,
		RECORDS_FAILED,
	}
	
	@Override
	public void map(LongWritable key, GraphWritable graph, Context context) throws IOException {
		
	    this.context = context;
		context.getCounter(Counters.RECORDS_INPUT).increment(1);
		try {
			DataObject result = graph.getDataGraph().getRootObject();
			PlasmaChangeSummary changeSummary = ((PlasmaChangeSummary)graph.getDataGraph().getChangeSummary());
			if (result instanceof Synsets) {
				Synsets synset = (Synsets)result;
				changeSummary.clear(synset.getLexdomains()); // clears 'created' state so won't try to insert
			}
			else if (result instanceof Words) {
				result = this.support.mapWordGraph((Words)result, service, context);
			}
			else if (result instanceof Semlinks) {
				result = this.support.mapSemLinkGraph((Semlinks)result, service, context);
				Semlinks link = (Semlinks)result;
			    link.setSemlinkid((int)key.get());
			}
			else if (result instanceof Lexlinks) {
				result = this.support.mapLexLinkGraph((Lexlinks)result, service, context);
				Lexlinks link = (Lexlinks)result;
			    link.setLexlinkid((int)key.get());			     
			}
			// else commit as is, no references
			 
			log.info("comitting: " + result.getClass().getSimpleName());
            //log.info(result.getChangeSummary().toString());
			this.service.commit(result.getDataGraph(), context);
			
		    context.getCounter(Counters.RECORDS_SUCCESS).increment(1);
			
		} catch (Exception ex) {
		    context.getCounter(Counters.RECORDS_FAILED).increment(1);
			log.error(ex);
			//throw new RuntimeException(ex);
		}
		
	}

}
