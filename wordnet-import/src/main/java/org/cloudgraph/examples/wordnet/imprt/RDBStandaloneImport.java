package org.cloudgraph.examples.wordnet.imprt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.common.service.MetricCollector;
import org.cloudgraph.examples.wordnet.Lexlinks;
import org.cloudgraph.examples.wordnet.Semlinks;
import org.cloudgraph.examples.wordnet.Synsets;
import org.cloudgraph.examples.wordnet.Words;
import org.cloudgraph.mapreduce.GraphService;
import org.cloudgraph.rdb.service.RDBGraphService;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.core.CoreDataObject;
import org.plasma.sdo.xml.DefaultOptions;
import org.plasma.sdo.xml.StreamUnmarshaller;
import org.plasma.sdo.xml.UnmarshallerException;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.helper.XMLDocument;

/**
 * Imports XML serialized heterogeneous graph structures to Wordnet using CloudGraph RDB service from an XML file. 
 *   
 * This simple import driver is not a mapreduce driver but since Wordnet is a relatively small database, 
 * is intended as a convenient way to import Wordnet locally.
 * 
 * Running mvn install on this subproject results in a *-distribution.zip file. Extract the distribution to
 * some working directory and execute the below or similar commands.  
 * 
 * Since Wordnet is so highly connected, it is necessary to import the data in stages. Since Mapreduce jobs run in parallel
 * it is necessary to run 4 import jobs, so we export the data into 4 files as below. 
 *   
 * java -jar ./wordnet-import-0.5.2/wordnet-import-0.5.2.jar -file wordnet-ref.xml    
 * java -jar ./wordnet-import-0.5.2/wordnet-import-0.5.2.jar -file wordnet-synsets.xml   
 * java -jar ./wordnet-import-0.5.2/wordnet-import-0.5.2.jar -file wordnet-words.xml   
 * java -jar ./wordnet-import-0.5.2/wordnet-import-0.5.2.jar -file wordnet-links.xml     
 */
public class RDBStandaloneImport extends ImportSupport {
	private static Log log = LogFactory.getLog(RDBStandaloneImport.class);
	protected GraphService service;
	private static String ARG_BATCH_SIZE = "-size";
	private static String ARG_BATCH_EXPORT = "-export";
	private static String ARG_BATCH_FILE = "-file";
	private String file;
	private InputStream inputStream;
	private int incr = 1000;
	private DefaultOptions unmarshalOptions;
	private StreamUnmarshaller unmarshaler;
	private String rootNamespaceUri;
	private String rootNamespacePrefix;

	public RDBStandaloneImport(Map<String, String> args)
			throws IOException {
		super();
		this.service = new RDBGraphService();
		String value = args.get(ARG_BATCH_SIZE);
		if (value != null)
			incr = Integer.valueOf(value).intValue();
		value = args.get(ARG_BATCH_FILE);
		if (value != null) {
			file = value;
			this.inputStream = new FileInputStream(new File(".", file));
		} else {
			printUsage();
			return;
		}
		this.unmarshalOptions = new DefaultOptions(this.rootNamespaceUri);
		this.unmarshalOptions.setRootNamespacePrefix(this.rootNamespacePrefix);
		this.unmarshalOptions.setValidate(false);
		this.unmarshalOptions.setFailOnValidationError(false);
		this.unmarshaler = new StreamUnmarshaller(this.unmarshalOptions, null);
		
		read();
	}

	private void read() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				this.inputStream));
		String line;

		//for (int start = 1;; start += incr) {
			//log.info("loading " + start + " to " + (start + (incr-1)) );
		//}    	
		
		int commitCount = 1;
		List<DataGraph> graphs = new ArrayList<DataGraph>();
		while ((line = in.readLine()) != null) {
			DataGraph graph = this.unmarshal(line);
			DataObject root = graph.getRootObject();
			PlasmaChangeSummary changeSummary = ((PlasmaChangeSummary) graph
					.getChangeSummary());
			if (root instanceof Synsets) {
				Synsets synset = (Synsets) root;
				changeSummary.clear(synset.getLexdomains()); // clears 'created'
																// state so
																// won't try to
																// insert
			} else if (root instanceof Words) {
				root = mapWordGraph((Words) root, service, null);
			} else if (root instanceof Semlinks) {
				root = mapSemLinkGraph((Semlinks) root, service, null);
			} else if (root instanceof Lexlinks) {
				root = mapLexLinkGraph((Lexlinks) root, service, null);
			}
			// else commit as is, no references
			graphs.add(graph);
			
			if (graphs.size() >= incr) {
				log.info("comitting " + incr + " graphs (" + commitCount + ")");
				for (DataGraph g : graphs)
				    this.service.commit(g, null);
				graphs.clear();
				commitCount++;
			}
		}
		
		log.info("comitting " + graphs.size() + " graphs");
		for (DataGraph g : graphs)
		    this.service.commit(g, null);

	}

	/**
	 * Deserializes the given text/xml and unmarshalles it as a data graph,
	 * capturing various metrics and returning the new graph. The given text/xml
	 * represents a single line in the underlying file and is assumed to be an
	 * XML serialized data graph.
	 * 
	 * @param text
	 *            the input text
	 * @return the new graph
	 * @throws IOException
	 */
	private DataGraph unmarshal(String text) throws IOException {

		long before = System.currentTimeMillis();

		ByteArrayInputStream xmlloadis = new ByteArrayInputStream(
				text.getBytes());
		try {
			this.unmarshaler.unmarshal(xmlloadis);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		} catch (UnmarshallerException e) {
			throw new IOException(e);
		}
		XMLDocument doc = this.unmarshaler.getResult();
		doc.setNoNamespaceSchemaLocation(null);

		long after = System.currentTimeMillis();

		CoreDataObject root = (CoreDataObject) doc.getRootObject();
		MetricCollector visitor = new MetricCollector();
		root.accept(visitor);

		root.setValue(CloudGraphConstants.GRAPH_ASSEMBLY_TIME,
				Long.valueOf(after - before));
		root.setValue(CloudGraphConstants.GRAPH_NODE_COUNT,
				Long.valueOf(visitor.getCount()));
		root.setValue(CloudGraphConstants.GRAPH_DEPTH,
				Long.valueOf(visitor.getDepth()));

		return doc.getRootObject().getDataGraph();
	}

	public static void main(String[] args) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		try {
			for (int i = 0; i < args.length; i += 2) {
				map.put(args[i], args[i + 1]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			printUsage();
			return;
		}
		new RDBStandaloneImport(map);
	}

	private static void printUsage() {
		StringBuilder buf = new StringBuilder();

		System.out
				.println("--------------------------------------------------------------------------");
		System.out
				.println("java -jar wordnet-import-0.5.2.jar [-size record-chunk-size] [-file import-file-name] ");
		System.out
				.println("--------------------------------------------------------------------------");
		System.out.println("examples:");
		System.out
				.println("java wordnet-import-0.5.2.jar -size 10000 -file name");
		System.out
				.println("--------------------------------------------------------------------------");
	}

}
