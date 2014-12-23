package org.cloudgraph.examples.wordnet.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.rdb.service.RDBGraphService;


public class RDBImportMapper extends ImportMapper  {
	private static Log log = LogFactory.getLog(RDBImportMapper.class);


	public RDBImportMapper() {
		this.service = new RDBGraphService();
	}

}
