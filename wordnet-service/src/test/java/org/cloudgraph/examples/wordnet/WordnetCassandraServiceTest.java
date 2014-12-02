package org.cloudgraph.examples.wordnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.cloudgraph.cassandra.service.CassandraGraphService;
import org.cloudgraph.common.CommonTest;
import org.cloudgraph.examples.wordnet.query.QLinktypes;
import org.cloudgraph.examples.wordnet.query.QSynsets;
import org.cloudgraph.examples.wordnet.service.WordRelations;
import org.cloudgraph.examples.wordnet.service.Wordnet;
import org.cloudgraph.examples.wordnet.service.WordnetService;
import org.cloudgraph.examples.wordnet.service.WordnetServiceImpl;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.PlasmaDataGraph;
import org.plasma.sdo.access.client.PojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaDataFactory;
import org.plasma.sdo.helper.PlasmaDataHelper;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public class WordnetCassandraServiceTest extends CommonTest {
	
	private WordnetServiceImpl service;
	
    public void setUp() throws Exception {
    	  
    	 this.service = new WordnetServiceImpl(DataAccessProviderName.CASSANDRA);
     }    
 /*     
    public void testInsertWord() {
    	PlasmaDataGraph graph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Words.class);
    	graph.getChangeSummary().beginLogging();
   	    Words root = (Words)graph.createRootObject(rootType);
    	
    	root.setWordid(2323);
    	root.setLemma("fallow");
    	this.service.getService().commit(graph, this.getClass().getSimpleName());
    }   
    public void testInsertLinkType() {
    	PlasmaDataGraph graph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Linktypes.class);
    	graph.getChangeSummary().beginLogging();
    	Linktypes root = (Linktypes)graph.createRootObject(rootType);
    	
    	root.setLinkid((short)13);
    	root.setLink("hypernym");
    	root.setRecurses((short)0);
    	this.service.getService().commit(graph, this.getClass().getSimpleName());
    }   
    public void testInsertLexdomains() {
    	PlasmaDataGraph graph = PlasmaDataFactory.INSTANCE.createDataGraph();
    	Type rootType = PlasmaTypeHelper.INSTANCE.getType(Lexdomains.class);
    	graph.getChangeSummary().beginLogging();
    	Lexdomains root = (Lexdomains)graph.createRootObject(rootType);
    	
    	root.setLexdomainid((short)13);
    	root.setLexdomainname("noun.body");
    	root.setPos("n");
    	this.service.getService().commit(graph, this.getClass().getSimpleName());
    }   
 
     
    public void testQuerySynsets() {
		QSynsets query = QSynsets.newQuery();
		query.select(query.synsetid())
	         .select(query.pos())
	         .select(query.lexdomains().lexdomainname())
	    ;
		 
		query.where(query.synsetid().eq(114429295)   
			 .and(query.pos().eq("n")  
		     .and(query.lexdomains().lexdomainname().eq("noun.state")))
				);
		
		DataGraph[] graphs = this.service.getService().find(query);
		if (graphs == null || graphs.length != 1) { 
			if (graphs == null)
			    throw new RuntimeException("expected single Synset result, not 0");
			else
			    throw new RuntimeException("expected single Synset result, not " + graphs.length);
	    }
		Synsets result = (Synsets)graphs[0].getRootObject();
    }
     
    
     
    public void testQueryLinktypes() {
    	QLinktypes query = QLinktypes.newQuery();
		query.select(query.wildcard())
	    ;
		 
		query.where(query.linkid().eq(11));   
		
		DataGraph[] graphs = this.service.getService().find(query);
		if (graphs == null || graphs.length != 1) { 
			if (graphs == null)
			    throw new RuntimeException("expected single Synset result, not 0");
			else
			    throw new RuntimeException("expected single Synset result, not " + graphs.length);
	    }
		Linktypes result = (Linktypes)graphs[0].getRootObject();
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
*/      
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
 
}
