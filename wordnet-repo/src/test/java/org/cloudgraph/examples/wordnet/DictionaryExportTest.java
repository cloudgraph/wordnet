package org.cloudgraph.examples.wordnet;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wordnet.model.Lexdomains;
import org.cloudgraph.examples.wordnet.model.Lexlinks;
import org.cloudgraph.examples.wordnet.model.Linktypes;
import org.cloudgraph.examples.wordnet.model.Semlinks;
import org.cloudgraph.examples.wordnet.model.Synsets;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.examples.wordnet.model.query.QLexdomains;
import org.cloudgraph.examples.wordnet.model.query.QLexlinks;
import org.cloudgraph.examples.wordnet.model.query.QLinktypes;
import org.cloudgraph.examples.wordnet.model.query.QSemlinks;
import org.cloudgraph.examples.wordnet.model.query.QSynsets;
import org.cloudgraph.examples.wordnet.model.query.QWords;
import org.plasma.query.Query;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class DictionaryExportTest extends WordnetTestCase {
    private static Log log = LogFactory.getLog(DictionaryExportTest.class);
    private int incr = 1000;
    
    public void testMe() {
    	
    }

	public void testLexDomainExport() throws IOException {
		QLexdomains query = QLexdomains.newQuery();
		query.select(query.wildcard());
		query.orderBy(query.lexdomainid());
		
		load(query);
    }
	
	
	public void testLinkTypesExport() throws IOException {
		QLinktypes query = QLinktypes.newQuery();
		query.select(query.wildcard());
		load(query);
    }

    	
    public void testWordExport() throws IOException {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		    .select(query.casedwords().wildcard())
		    .select(query.senses().wildcard())
		    //.select(word.senses().synsets().wildcard())
		    //.select(word.senses().synsets().adjpositions().wildcard())
		    //.select(word.senses().synsets().semlinks().wildcard()) // old stack
		    //.select(word.senses().synsets().semlinks().linktypes().wildcard())  
		    //.select(word.senses().synsets().semlinks().synsets1().wildcard()) // recursion if wildcard		    
		    //.select(word.senses().synsets().lexlinks().wildcard())
		    //.select(word.senses().synsets().lexlinks().linktypes().wildcard())
		    //.select(word.senses().synsets().lexlinks().words1().wordid())
		    //.select(word.senses().synsets().lexlinks().words1().lemma())
		    //.select(word.senses().synsets().vframemaps().wildcard())
		    //.select(word.senses().synsets().vframemaps().vframes().wildcard())
		    //.select(word.senses().synsets().vframesentencemaps().wildcard())
		    //.select(word.senses().synsets().vframesentencemaps().vframesentences().wildcard())
		;
		
		//word.where(word.lemma().eq("vehicle"));				
		load(query);
	}
    
 	
	public void testSynsetExport() throws IOException {
		QSynsets query = QSynsets.newQuery();
		query.select(query.wildcard())
		       .select(query.lexdomains().wildcard())
		;
		Type[] referenceOnlyTypes = new Type[] { 
				 PlasmaTypeHelper.INSTANCE.getType(Lexdomains.class)
		    };
		
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr-1)));
			query.setStartRange(start);
			query.setEndRange(start + (incr-1));
			DataGraph[] graphs = this.rdbService.find(query);
			log.info("found " + graphs.length + " results");
			if (graphs.length > 0) {
				for (DataGraph graph : graphs) {
					//log.info("RDB: " + serializeGraph(graph));
				}
				DataGraph[] hbasegraphs = new DataGraph[graphs.length];
				int i = 0;
				for (DataGraph graph : graphs) {
					 Synsets synset = (Synsets)graph.getRootObject();
					 Synsets copyRoot = (Synsets)PlasmaCopyHelper.INSTANCE.copy(synset,
							 referenceOnlyTypes);
					 hbasegraphs[i] = copyRoot.getDataGraph();
					 i++;
				}
				
				//this.hbaseService.commit(hbasegraphs, "test");
			}
			else
				break;
		}
		
	}    
 	
	 		 
	public void testSemLinksExport() throws IOException {
		QSemlinks query = QSemlinks.newQuery();
		query.select(query.wildcard())
		    .select(query.linktypes().wildcard())
		    .select(query.synsets().synsetid())
		    .select(query.synsets().pos())
		    .select(query.synsets().lexdomains().lexdomainname())
		    .select(query.synsets1().synsetid())
		    .select(query.synsets1().pos())
		    .select(query.synsets1().lexdomains().lexdomainname())
		;
		
		Type[] referenceOnlyTypes = new Type[] { 
			 PlasmaTypeHelper.INSTANCE.getType(Synsets.class),
			 PlasmaTypeHelper.INSTANCE.getType(Linktypes.class),
			 PlasmaTypeHelper.INSTANCE.getType(Lexdomains.class)
	    };

		
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr-1)));
			query.setStartRange(start);
			query.setEndRange(start + (incr-1));
			DataGraph[] graphs = this.rdbService.find(query);
			log.info("found " + graphs.length + " results");
			if (graphs.length > 0) {
				for (DataGraph graph : graphs) {
					//log.info("RDB: " + serializeGraph(graph));
				}
				DataGraph[] hbasegraphs = new DataGraph[graphs.length];
				
				
				int i = 0;
				for (DataGraph graph : graphs) {
					 Semlinks copyRoot = (Semlinks)PlasmaCopyHelper.INSTANCE.copy(graph.getRootObject(),
							 referenceOnlyTypes);

					 hbasegraphs[i] = copyRoot.getDataGraph();
					 log.info("HBASE: " + serializeGraph(copyRoot.getDataGraph()));
					 
					 i++;
				}
				
				//this.hbaseService.commit(hbasegraphs, "test");
			}
			else
				break;
		}
    }
 	
	 
	public void testLexLinksExport() throws IOException {
		QLexlinks query = QLexlinks.newQuery();
		query.select(query.wildcard())
		    .select(query.linktypes().wildcard())
		    .select(query.synsets().synsetid())
		    .select(query.synsets().pos())
		    .select(query.synsets().lexdomains().lexdomainname())
		    .select(query.synsets1().synsetid())
		    .select(query.synsets1().pos())
		    .select(query.synsets1().lexdomains().lexdomainname())
		    .select(query.words().lemma())
		    .select(query.words().wordid())
		    .select(query.words1().lemma())
		    .select(query.words1().wordid())
		;
		Type[] referenceOnlyTypes = new Type[] { 
				 PlasmaTypeHelper.INSTANCE.getType(Synsets.class),
				 PlasmaTypeHelper.INSTANCE.getType(Linktypes.class),
				 PlasmaTypeHelper.INSTANCE.getType(Lexdomains.class),
				 PlasmaTypeHelper.INSTANCE.getType(Words.class)
		    };
		
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr-1)));
			query.setStartRange(start);
			query.setEndRange(start + (incr-1));
			DataGraph[] graphs = this.rdbService.find(query);
			log.info("found " + graphs.length + " results");
			if (graphs.length > 0) {
				DataGraph[] hbasegraphs = new DataGraph[graphs.length];
				int i = 0;
				for (DataGraph graph : graphs) {
					 Lexlinks copyRoot = (Lexlinks)PlasmaCopyHelper.INSTANCE.copy(graph.getRootObject(),
							 referenceOnlyTypes);
					 hbasegraphs[i] = copyRoot.getDataGraph();
					 i++;
				}
				
				//this.hbaseService.commit(hbasegraphs, "test");
			}
			else
				break;
		}
	}
 	 
	
    private void load(Query query) throws IOException
    {
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr-1)));
			query.setStartRange(start);
			query.setEndRange(start + (incr-1));
			DataGraph[] graphs = this.rdbService.find(query);
			log.info("found " + graphs.length + " results");
			if (graphs.length > 0) {
				for (DataGraph graph : graphs) {
					//log.info(serializeGraph(graph));
				}
				DataGraph[] hbasegraphs = new DataGraph[graphs.length];
				int i = 0;
				for (DataGraph graph : graphs) {
					 DataObject copyRoot = PlasmaCopyHelper.INSTANCE.copy(graph.getRootObject());
					 hbasegraphs[i] = copyRoot.getDataGraph();
					 i++;
				}
				
				//this.hbaseService.commit(hbasegraphs, "test");
			}
			else
				break;
		}
    	
    }
}
