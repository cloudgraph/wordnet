package org.cloudgraph.examples.wordnet.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.cassandra.service.CassandraGraphService;


public class CassandraImportMapper extends ImportMapper  {
	private static Log log = LogFactory.getLog(CassandraImportMapper.class);


	public CassandraImportMapper() {
		this.service = new CassandraGraphService();
	}

}
