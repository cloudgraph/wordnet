package org.cloudgraph.examples.wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wordnet.query.QLexdomains;
import org.cloudgraph.examples.wordnet.query.QLexlinks;
import org.cloudgraph.examples.wordnet.query.QLinktypes;
import org.cloudgraph.examples.wordnet.query.QSemlinks;
import org.cloudgraph.examples.wordnet.query.QSenses;
import org.cloudgraph.examples.wordnet.query.QSynsets;
import org.cloudgraph.examples.wordnet.query.QWords;
import org.plasma.query.Query;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.helper.PlasmaCopyHelper;
import org.plasma.sdo.helper.PlasmaTypeHelper;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class DictionaryQueryTest extends WordnetTestCase {
    private static Log log = LogFactory.getLog(DictionaryQueryTest.class);
    private int incr = 1000;
    
    public void testMe() {
    	
    }
     	
    public void testWordQuery1() throws IOException {
    	Words word = getWordGraph("calculable", 19294);
	}
    
    public void testWordQuery2() throws IOException {
    	Words word = getWordGraph("worry", 0);
	}
     
    private Words getWordGraph(String lemma, int wordid) throws IOException
    {
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
		    // lexical relations
		    //.select(query.senses().synsets().lexlinks().wildcard())  
		    //.select(query.senses().synsets().lexlinks().linktypes().wildcard())
		    //.select(query.senses().synsets().lexlinks().synsets().wildcard())  
		    //.select(query.senses().synsets().lexlinks().synsets().samples().wildcard())  
		    //.select(query.senses().synsets().lexlinks().synsets().senses().wildcard())  
		    //.select(query.senses().synsets().lexlinks().synsets().senses().words().wildcard())  
		;
		
		//query.where(query.lemma().eq("vehicle").and(query.wordid().eq(140022)));	
		if (wordid > 0)
		    query.where(query.lemma().eq(lemma).and(query.wordid().eq(19294)));	
		else
			query.where(query.lemma().eq(lemma));	
		
		Words result = null;
		DataGraph[] graphs = this.hbaseService.find(query);
		for (DataGraph graph : graphs) {
		    log.info(this.serializeGraph(graph));
		    //log.info(((PlasmaDataGraph)graph).dump());
		    Words w = (Words)graph.getRootObject();
		    if (w.getLemma().equals(lemma)) {
		    	result = w;
		    }
		    else {
		    	log.warn("invalid word: " + w.getLemma());
		    }
		}		
    	return result;
    }
    
/*    
    public void testCalculableWordQuery() throws IOException {
    	 List<Senses> leftSenses = getLeftSenses("calculable", 19294);
    	 assertTrue(leftSenses.size() == 1); 
    	 List<Senses> rightSemanticSenses = new ArrayList<Senses>();
    	 List<Senses> rightLexicalSenses = new ArrayList<Senses>();
    	 for (Senses sense : leftSenses) {
    		 assertTrue(sense.getSynsets() != null);
    		 //assertTrue(sense.getSynsets().getLexdomains() != null);
    		 Map<String, List<Semlinks>> semLinkMap = getSemanticLinks(sense.getSynsets());
    		 assertTrue(semLinkMap.size() == 2); 
    		 Iterator<String> semLinkIter = semLinkMap.keySet().iterator();
    		 while (semLinkIter.hasNext()) {
    			 String linkType = semLinkIter.next();
    			 List<Semlinks> links = semLinkMap.get(linkType);
        	     for (Semlinks link : links) {
        	    	 List<Senses> list = getSenses(link.getSynsets());
        	    	 rightSemanticSenses.addAll(list);
        	     }
    		 }
    		 assertTrue(rightSemanticSenses.size() == 7);
    		 
    		 Map<String, List<Lexlinks>> lexLinkMap = getLexicalLinks(sense.getSynsets());
    		 
    		 assertTrue(lexLinkMap.size() == 2); 
    		 Iterator<String> lexLinkIter = lexLinkMap.keySet().iterator();
    		 while (lexLinkIter.hasNext()) {
    			 String linkType = lexLinkIter.next();
    			 List<Lexlinks> links = lexLinkMap.get(linkType);
        	     for (Lexlinks link : links) {
        	    	 List<Senses> list = getSenses(link.getSynsets());
        	    	 rightLexicalSenses.addAll(list);
        	     }
    		 }
    	 }
    }
*/     
    
    private List<Senses> getLeftSenses(String lemma, long wordid) throws IOException
    {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		    .select(query.casedwords().wildcard())
		    .select(query.senses().wildcard())
		    .select(query.senses().synsets().wildcard())
		    .select(query.senses().synsets().lexdomains().wildcard())
		    .select(query.senses().synsets().adjpositions().wildcard())
   	     ;
		query.where(query.lemma().eq(lemma).and(query.wordid().eq(wordid)));	
		
		List<Senses> result = new ArrayList<Senses>();
		DataGraph[] graphs = this.hbaseService.find(query);
		for (DataGraph graph : graphs) {
			log.info(this.serializeGraph(graph));
			Words word = (Words)graph.getRootObject();			
			if (word.getSensesCount() > 0)
			for (Senses sence : word.getSenses()) {
	        	result.add(sence);
			}
		} 
		return result;
	}
    
    private Map<String, List<Semlinks>> getSemanticLinks(Synsets synset) throws IOException
    {
		QSemlinks query = QSemlinks.newQuery();
		query.select(query.wildcard())
		    .select(query.linktypes().wildcard())
		    .select(query.synsets().wildcard())
		    .select(query.synsets().samples().wildcard())
		    .select(query.synsets().senses().wildcard()) // link is broken between synsets->senses to get back to Words
		    //.select(query.synsets1().wildcard())  
   	     ;
		// FIXME: Where properties getting into query
		query.where(query.synsets1().synsetid().eq(synset.getSynsetid()));	
		
		Map<String, List<Semlinks>> result = new HashMap<String, List<Semlinks>>();
		DataGraph[] graphs = this.hbaseService.find(query);
		for (DataGraph graph : graphs) {
			log.info(this.serializeGraph(graph));
			Semlinks link = (Semlinks)graph.getRootObject();
			assertTrue(link.getSynsets() != null);
			assertTrue(link.getLinktypes() != null);
			assertTrue(link.getLinktypes().getLink() != null);
			List<Semlinks> list = result.get(link.getLinktypes().getLink());
			if (list == null) {
				list = new ArrayList<Semlinks>();
				result.put(link.getLinktypes().getLink(), list);
			}
			list.add(link);
		} 
		return result;
	}

    private Map<String, List<Lexlinks>> getLexicalLinks(Synsets synset) throws IOException
    {
		QLexlinks query = QLexlinks.newQuery();
		query.select(query.wildcard())
		    .select(query.linktypes().wildcard())
		    .select(query.synsets().wildcard())
		    .select(query.synsets().samples().wildcard())
		    .select(query.synsets().senses().wildcard()) // link is broken between synsets->senses to get back to Words
		    //.select(query.synsets1().wildcard())  
   	     ;
		// FIXME: Where properties getting into query
		query.where(query.synsets1().synsetid().eq(synset.getSynsetid()));	
		
		Map<String, List<Lexlinks>> result = new HashMap<String, List<Lexlinks>>();
		DataGraph[] graphs = this.hbaseService.find(query);
		for (DataGraph graph : graphs) {
			log.info(this.serializeGraph(graph));
			Lexlinks link = (Lexlinks)graph.getRootObject();
			assertTrue(link.getSynsets() != null);
			assertTrue(link.getLinktypes() != null);
			assertTrue(link.getLinktypes().getLink() != null);
			List<Lexlinks> list = result.get(link.getLinktypes().getLink());
			if (list == null) {
				list = new ArrayList<Lexlinks>();
				result.put(link.getLinktypes().getLink(), list);
			}
			list.add(link);
		} 
		return result;
	}
    
    
    private List<Senses> getSenses(Synsets synset) throws IOException
    {
		QSenses query = QSenses.newQuery();
		query.select(query.wildcard())
		     .select(query.words().wildcard())
		     .select(query.synsets().wildcard())
   	     ;
		// FIXME: Where properties getting into query
		query.where(query.synsets().synsetid().eq(synset.getSynsetid()));	
		
		List<Senses> result = new ArrayList<Senses>();
		DataGraph[] graphs = this.hbaseService.find(query);
		for (DataGraph graph : graphs) {
			log.info(this.serializeGraph(graph));
			Senses senses = (Senses)graph.getRootObject();
			result.add(senses);
		} 
		return result;
	}
    
     
    
    /*
    public void testLexLinkQuery() throws IOException {
 		QLexlinks query = QLexlinks.newQuery();
 		query.select(query.wildcard())
 		    .select(query.words().wildcard())
 		    .select(query.words1().wildcard())
 		;
 		
 		//query.where(query.linktypes().linkid().eq(1));
 			 //.and(query.synsets().synsetid().eq(200714066)
 			 //.and(query.words().wordid().eq(19295))));	
 		
 		query.setStartRange(1);
 		query.setEndRange(20);
 		
 		DataGraph[] graphs = this.hbaseService.find(query);
 		for (DataGraph graph : graphs)
 		    log.info(this.serializeGraph(graph));
 		
 	}
 	*/
/*  	
    public void testSemlinksQuery() throws IOException {
  		QSemlinks query = QSemlinks.newQuery();
  		query.select(query.wildcard())
   		    .select(query.synsets().wildcard())
 		    .select(query.synsets().senses().wildcard()) // senses are not roots can't link
  		    .select(query.synsets().senses().words().wildcard())
   		    .select(query.synsets1().wildcard())
 		    .select(query.synsets1().senses().wildcard())
  		    .select(query.synsets1().senses().words().wildcard())
  		;
  		
  		query.where(query.synsets().synsetid().eq(100004258));
  		
  		//query.setStartRange(1);
  		//query.setEndRange(20);
  		
  		DataGraph[] graphs = this.hbaseService.find(query);
  		for (DataGraph graph : graphs)
  		    log.info(this.serializeGraph(graph));
  		
  	}
 */	
}
