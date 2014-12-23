package org.cloudgraph.examples.wordnet;

import java.io.IOException;
import java.util.List;

import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.config.QueryFetchType;
import org.cloudgraph.examples.wordnet.service.WordRelations;
import org.cloudgraph.examples.wordnet.service.Wordnet;
import org.cloudgraph.examples.wordnet.service.WordnetService;
import org.cloudgraph.examples.wordnet.service.WordnetServiceImpl;
import org.plasma.sdo.core.CoreNode;

public class WordnetHBaseServiceTest extends CommonTest {
	
	private WordnetService service;
	
    public void setUp() throws Exception {
    	 this.service = new WordnetServiceImpl();
    }
     
    /* 
    public void testWord() {
    	Wordnet wordnet = this.service.getAllRelations("fallow");
    	assertTrue(wordnet != null);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	try {
			log.info(ServiceTestUtils.serializeGraph(wordnet.getWord().getDataGraph()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
     
    public void testSynonyms() throws IOException {
    	//Wordnet wordnet = this.service.getSynonyms("beautiful", 12474);
    	//Wordnet wordnet = this.service.getAllRelations("beautiful", 12474);
    	Wordnet wordnet = this.service.getAllRelations("faithful");
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	//assertTrue(relations.size() == 1);
    	//log.info(serializeGraph(wordnet.getWord().getDataGraph()));
    	try {
			log.info(ServiceTestUtils.serializeGraph(wordnet.getWord().getDataGraph()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    */
    public void testSynonymsConcurrent() throws IOException {
    	Wordnet wordnet = this.service.getAllRelations("faithful", 
    			QueryFetchType.PARALLEL);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	//assertTrue(relations.size() == 1);
    	//log.info(serializeGraph(wordnet.getWord().getDataGraph()));
    	try {
			//log.info(ServiceTestUtils.serializeGraph(wordnet.getWord().getDataGraph()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
