package org.cloudgraph.examples.wordnet.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wordnet.Lexdomains;
import org.cloudgraph.examples.wordnet.Lexlinks;
import org.cloudgraph.examples.wordnet.Linktypes;
import org.cloudgraph.examples.wordnet.Morphology;
import org.cloudgraph.examples.wordnet.Semlinks;
import org.cloudgraph.examples.wordnet.Senses;
import org.cloudgraph.examples.wordnet.Synsets;
import org.cloudgraph.examples.wordnet.Words;
import org.cloudgraph.examples.wordnet.query.QLexdomains;
import org.cloudgraph.examples.wordnet.query.QLexlinks;
import org.cloudgraph.examples.wordnet.query.QLinktypes;
import org.cloudgraph.examples.wordnet.query.QMorphology;
import org.cloudgraph.examples.wordnet.query.QSemlinks;
import org.cloudgraph.examples.wordnet.query.QSenses;
import org.cloudgraph.examples.wordnet.query.QSynsets;
import org.cloudgraph.examples.wordnet.query.QWords;
import org.plasma.query.Query;
import org.plasma.sdo.access.client.JDBCPojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

/**
 * Exports XML serialized heterogeneous graph structures from Wordnet using CloudGraph RDB service into an XML file. 
 * A specified graph or graph-set may be exported, or all data is exported by default.
 *   
 * This simple export driver is not a mapreduce driver but since Wordnet is a relatively small database, 
 * is intended as a convenient way to export Wordnet locally.
 * 
 * Running mvn install on this subproject results in a *-distribution.zip file. Extract the distribution to
 * some working directory and execute the below or similar commands.  
 * 
 * Since Wordnet is so highly connected, it is necessary to import the data in stages. Since Mapreduce jobs run in parallel
 * it is necessary to run 4 import mapreduce jobs, to we export the data into 4 files as below. 
 * There may be other ways to export Wordnet but the below is an example.  
 *   
 * java -jar ./wordnet-export-0.5.1/wordnet-export-0.5.1.jar -file wordnet-ref.xml -exportall_ref
 * java -jar ./wordnet-export-0.5.1/wordnet-export-0.5.1.jar -file wordnet-synsets.xml -export synsets
 * java -jar ./wordnet-export-0.5.1/wordnet-export-0.5.1.jar -file wordnet-words.xml -export words_and_senses
 * java -jar ./wordnet-export-0.5.1/wordnet-export-0.5.1.jar -file wordnet-links.xml -export all_links 
 */
public class ExportDriver {
    protected SDODataAccessClient rdbService;
    private static String ARG_BATCH_SIZE = "-size";
    private static String ARG_BATCH_EXPORT = "-export";
    private static String ARG_BATCH_FILE = "-file";
    private String file;
    private OutputStream outputStream;
    private enum Export {
    	all,
    	all_refs,
    	all_links,
    	lexdomains,
    	linktypes,
    	synsets,
    	words,
    	senses,
    	words_and_senses,
    	semlinks,
    	lexlinks,
    	morphology
    }
    private Export toExport = Export.all;
	
    private static Log log = LogFactory.getLog(ExportDriver.class);
    private int incr = 1000;

    private ExportDriver(Map<String, String> args) throws IOException {
     	this.rdbService = new SDODataAccessClient(new JDBCPojoDataAccessClient());
    	
    	String value = args.get(ARG_BATCH_SIZE);
    	if (value != null)
    		incr = Integer.valueOf(value).intValue();
    	value = args.get(ARG_BATCH_FILE);
    	if (value != null) {
    		file = value;
    		this.outputStream = new FileOutputStream(new File(".", file));
    	}
    	else {
    		printUsage();
    		return;
    	}
    	value = args.get(ARG_BATCH_EXPORT);
    	if (value != null) {
    		try {
    		    toExport = Export.valueOf(value);
    		}
    		catch (IllegalArgumentException e) {
        		printUsage();
        		return;
    		}
    	}
    	
    	switch (toExport) {
    	case lexdomains:
        	log.info("loading LexDomain graphs");
        	loadLexDomain();
    		break;
    	case linktypes:
        	log.info("loading LinkTypes graphs");
       	    loadLinkTypes();
    		break;
    	case all_refs:
        	log.info("loading reference graphs");
        	loadLexDomain();
       	    loadLinkTypes();
    		break;
    	case synsets:
        	log.info("loading Synsets graphs");
        	loadSynsets();
    		break;
    	case words:
        	log.info("loading Words graphs");
        	loadWordsAndSenses();
    		break;
    	case senses:
        	log.info("loading Senses graphs");
        	loadSenses();
    		break;
    	case words_and_senses:
        	log.info("loading Words with senses graphs");
        	loadWordsAndSenses();
    		break;
    	case semlinks:
        	log.info("loading SemLinks graphs");
        	loadSemLinks();
    		break;
    	case lexlinks:
        	log.info("loading LexLinks graphs");
        	loadLexLinks();
    		break;
    	case all_links:
        	log.info("loading SemLinks and LexLinks graphs");
        	loadSemLinks();
        	loadLexLinks();
   		    break;
    	case morphology:
        	log.info("loading Morphology graphs");
        	loadMorphology();
    		break;
    	case all:
        	log.info("loading LexDomain graphs");
        	loadLexDomain();
        	log.info("loading LinkTypes graphs");
       	    loadLinkTypes();
        	log.info("loading Synsets graphs");
        	loadSynsets();
        	log.info("loading Words graphs");
        	loadWordsAndSenses();
        	//log.info("loading Senses graphs");
        	//loadSenses();
        	log.info("loading SemLinks graphs");
        	loadSemLinks();
        	log.info("loading LexLinks graphs");
        	loadLexLinks();
        	log.info("loading Morphology graphs");
        	loadMorphology();
    		break;
    	}
    	
    	this.outputStream.close();
    }
    
    public static void main(String[] args) throws IOException {
    	Map<String, String> map = new HashMap<String, String>();
    	try {
	    	for (int i = 0; i < args.length; i+=2) {
	    		map.put(args[i], args[i+1]);
	    	}
    	}
    	catch (ArrayIndexOutOfBoundsException e) {
    		printUsage();
    		return;
    	}
    	new ExportDriver(map);
    }
    
    private static void printUsage() {
    	StringBuilder buf = new StringBuilder();
    	for (int i = 0; i < Export.values().length; i++)
    	{
    		if (i > 0)
    			buf.append("|");
    		buf.append(Export.values()[i]);
    	}
    	
    	System.out.println("--------------------------------------------------------------------------");
    	System.out.println("java -jar wordnet-export-0.5.1.jar [-size record-chunk-size] [-file export-file-name] [-export "+buf.toString()+"]");
    	System.out.println("--------------------------------------------------------------------------");
    	System.out.println("examples:");
     	System.out.println("java wordnet-export-0.5.1.jar -size 10000 -file name");
    	System.out.println("--------------------------------------------------------------------------");
    }

	public void loadLexDomain() throws IOException {
		QLexdomains query = QLexdomains.newQuery();
		query.select(query.wildcard());
		load(query, Lexdomains.class);
    }
	
	private void loadLinkTypes() throws IOException {
		QLinktypes query = QLinktypes.newQuery();
		query.select(query.wildcard());
		load(query, Linktypes.class);
    }

	private void loadSynsets() throws IOException {
		
		QSynsets query = QSynsets.newQuery();
		query.select(query.wildcard())
		     .select(query.samples().wildcard())
		     .select(query.lexdomains().wildcard())
		;
		load(query, Synsets.class);
	}
	
	// load word graphs 
	private void loadWords() throws IOException {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		     .select(query.casedwords().wildcard())
		;
		load(query, Words.class);
	}

	// load word graphs with senses
	private void loadWordsAndSenses() throws IOException {
		QWords query = QWords.newQuery();
		query.select(query.wildcard())
		     .select(query.casedwords().wildcard())
		     .select(query.senses().wildcard())
		     .select(query.senses().synsets().wildcard())
		     .select(query.senses().synsets().lexdomains().lexdomainname())
		;
		load(query, Words.class);
	}
	
	// links senses to synsets words
	private void loadSenses() throws IOException {
		QSenses query = QSenses.newQuery();
		query.select(query.wildcard())
		     .select(query.words().wildcard())
		     .select(query.synsets().wildcard())
		     .select(query.synsets().lexdomains().lexdomainname())
		;
		load(query,Senses.class);
	}
	
	public void loadSemLinks() throws IOException {
		QSemlinks query = QSemlinks.newQuery();
		query.select(query.linktypes().wildcard())
		    .select(query.synsets().synsetid())
		    .select(query.synsets().pos())
		    .select(query.synsets().lexdomains().lexdomainname())
		    .select(query.synsets1().synsetid())
		    .select(query.synsets1().pos())
		    .select(query.synsets1().lexdomains().lexdomainname())
		;
		load(query, Semlinks.class);
    } 	
	 
	public void loadLexLinks() throws IOException {
		QLexlinks query = QLexlinks.newQuery();
		query.select(query.linktypes().wildcard())
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
		load(query, Lexlinks.class);
	}
	
	private void loadMorphology() throws IOException {
		QMorphology query = QMorphology.newQuery();
		query.select(query.wildcard())
		;
		load(query, Morphology.class);
	}
		
    private void load(Query query, Class clss) throws IOException
    {
		for (int start = 1;; start += incr) {
			log.info("fetching " + start + " to " + (start + (incr-1)) + " " + clss.getSimpleName());
			query.setStartRange(start);
			query.setEndRange(start + (incr-1));
			DataGraph[] graphs = this.rdbService.find(query);
			log.info("found " + graphs.length + " results " + clss.getSimpleName());
			if (graphs.length > 0) {
				for (DataGraph graph : graphs) {
					 byte[] graphBytes = serializeGraph(graph);
					 this.outputStream.write(graphBytes);
					 this.outputStream.write("\n".getBytes());
					 this.outputStream.flush();
				}
			}
			else
				break;
		}    	
    }
    
    protected byte[] serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("ns1");
        options.setPrettyPrint(false); // single line per graph       
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        return os.toByteArray();
    }

}
