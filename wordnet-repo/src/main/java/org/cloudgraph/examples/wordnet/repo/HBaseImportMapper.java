package org.cloudgraph.examples.wordnet.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;


public class HBaseImportMapper extends ImportMapper  {
	private static Log log = LogFactory.getLog(HBaseImportMapper.class);


	public HBaseImportMapper() {
		this.service = new GraphServiceDelegate();
	}

}
