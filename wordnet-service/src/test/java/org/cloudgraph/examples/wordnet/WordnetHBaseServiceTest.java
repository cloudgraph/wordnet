package org.cloudgraph.examples.wordnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wordnet.service.WordRelations;
import org.cloudgraph.examples.wordnet.service.Wordnet;
import org.cloudgraph.examples.wordnet.service.WordnetService;
import org.cloudgraph.examples.wordnet.service.WordnetServiceImpl;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class WordnetHBaseServiceTest extends CommonTest {
	
	private WordnetService service;
	
    public void setUp() throws Exception {
    	 this.service = new WordnetServiceImpl();
    }
     
     
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
    	Wordnet wordnet = this.service.getAllRelations("beautiful");
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

}
