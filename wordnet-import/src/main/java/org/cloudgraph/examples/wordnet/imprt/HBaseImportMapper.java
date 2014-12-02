package org.cloudgraph.examples.wordnet.imprt;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.cloudgraph.examples.wordnet.Lexlinks;
import org.cloudgraph.examples.wordnet.Linktypes;
import org.cloudgraph.examples.wordnet.Semlinks;
import org.cloudgraph.examples.wordnet.Senses;
import org.cloudgraph.examples.wordnet.Synsets;
import org.cloudgraph.examples.wordnet.Words;
import org.cloudgraph.examples.wordnet.query.QLinktypes;
import org.cloudgraph.examples.wordnet.query.QSynsets;
import org.cloudgraph.examples.wordnet.query.QWords;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;
import org.cloudgraph.mapreduce.GraphWritable;
import org.cloudgraph.mapreduce.GraphXmlMapper;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.core.NullValue;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.DataGraph;
import commonj.sdo.DataObject;
import commonj.sdo.Property;


public class HBaseImportMapper extends ImportMapper  {
	private static Log log = LogFactory.getLog(HBaseImportMapper.class);


	public HBaseImportMapper() {
		this.service = new GraphServiceDelegate();
	}

}
