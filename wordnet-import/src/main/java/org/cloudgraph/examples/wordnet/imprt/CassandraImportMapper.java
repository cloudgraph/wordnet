package org.cloudgraph.examples.wordnet.imprt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.service.CassandraGraphService;
import org.cloudgraph.hbase.mapreduce.GraphServiceDelegate;


public class CassandraImportMapper extends ImportMapper  {
	private static Log log = LogFactory.getLog(CassandraImportMapper.class);


	public CassandraImportMapper() {
		this.service = new CassandraGraphService();
	}

}
