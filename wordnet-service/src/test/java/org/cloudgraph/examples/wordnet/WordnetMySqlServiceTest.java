package org.cloudgraph.examples.wordnet;

import java.io.IOException;
import java.util.List;

import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.config.QueryFetchType;
import org.cloudgraph.examples.wordnet.service.WordRelations;
import org.cloudgraph.examples.wordnet.service.Wordnet;
import org.cloudgraph.examples.wordnet.service.WordnetServiceImpl;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.core.CoreNode;

public class WordnetMySqlServiceTest extends CommonTest {
	
	private WordnetServiceImpl service;
	
    public void setUp() throws Exception {
    	  
    	 this.service = new WordnetServiceImpl(DataAccessProviderName.JDBC);
    }    

    public void testSynonyms() throws IOException {
   	    Wordnet wordnet = this.service.getSynonyms("beautiful", QueryFetchType.PARALLEL);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	//assertTrue(relations.size() == 1);
    	//log.info(serializeGraph(wordnet.getWord().getDataGraph()));
    	/* 
    	try {
			log.info(ServiceTestUtils.serializeGraph(wordnet.getWord().getDataGraph()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		 
    	CoreNode rootNode = (CoreNode)wordnet.getWord();
    	Long assemTime = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_ASSEMBLY_TIME);
    	Long threadCount = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_THREAD_COUNT);
    	Long nodeCount = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_NODE_COUNT);
    	log.info("threads: " + threadCount + ", nodes: " + nodeCount + ", time: " + assemTime);
    	assertTrue(nodeCount == 143);
    }
    
    public void testWord() {
    	Wordnet wordnet = this.service.getAllRelations("city", QueryFetchType.PARALLEL);
    	assertTrue(wordnet != null);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	/*
    	try {
			log.info(ServiceTestUtils.serializeGraph(wordnet.getWord().getDataGraph()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    	CoreNode rootNode = (CoreNode)wordnet.getWord();
    	Long assemTime = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_ASSEMBLY_TIME);
    	Long threadCount = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_THREAD_COUNT);
    	Long nodeCount = (Long)rootNode.getValueObject().get(
        		CloudGraphConstants.GRAPH_NODE_COUNT);
    	log.info("threads: " + threadCount + ", nodes: " + nodeCount + ", time: " + assemTime);
    }    
}
