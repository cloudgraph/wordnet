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

public class WordnetServiceTest extends CommonTest {
	
	private WordnetService service;
	
    public void setUp() throws Exception {
    	 this.service = new WordnetServiceImpl();
    }
/*    
    public void testWord() {
    	Wordnet wordnet = this.service.getAllRelations("calculable", 19294);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	assertTrue(relations.size() == 1);
    }
*/    
    public void testSynonyms() throws IOException {
    	//Wordnet wordnet = this.service.getSynonyms("beautiful", 12474);
    	//Wordnet wordnet = this.service.getAllRelations("beautiful", 12474);
    	Wordnet wordnet = this.service.getAllRelations("city", 25207);
    	List<WordRelations> relations = wordnet.getRelations();
    	assertTrue(relations != null);
    	//assertTrue(relations.size() == 1);
    	//log.info(serializeGraph(wordnet.getWord().getDataGraph()));
    	log.info(wordnet.getWord().dump());
    }
    
	private String serializeGraph(DataGraph graph) throws IOException {
		DefaultOptions options = new DefaultOptions(graph.getRootObject()
				.getType().getURI());
		options.setRootNamespacePrefix("wordnet");

		XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(
				graph.getRootObject(),
				graph.getRootObject().getType().getURI(), null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PlasmaXMLHelper.INSTANCE.save(doc, os, options);
		os.flush();
		os.close();
		String xml = new String(os.toByteArray());
		return xml;
	}
}
