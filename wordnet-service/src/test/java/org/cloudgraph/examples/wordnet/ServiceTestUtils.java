package org.cloudgraph.examples.wordnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

public class ServiceTestUtils {
	public static String serializeGraph(DataGraph graph) throws IOException {
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
