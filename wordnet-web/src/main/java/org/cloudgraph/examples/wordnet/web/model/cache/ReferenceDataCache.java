package org.cloudgraph.examples.wordnet.web.model.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.QueryFetchType;
import org.cloudgraph.examples.wordnet.model.Senses;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.examples.wordnet.model.query.QWords;
import org.plasma.sdo.access.client.HBasePojoDataAccessClient;
import org.plasma.sdo.access.client.SDODataAccessClient;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import commonj.sdo.DataGraph;


/**
 * An application level cache which just stores
 * reference/lookup data using JSF application level
 * managed bean. 
 */
@ManagedBean(name="ReferenceDataCache")
@ApplicationScoped
public class ReferenceDataCache 
    implements Serializable 
{
    private static Log log = LogFactory.getLog(ReferenceDataCache.class);
	private static final long serialVersionUID = 1L;
    protected SDODataAccessClient service;
	private Map<String, Words> wordMap = new WeakHashMap<String, Words>();
	private static int[] ranges = {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
  

    /**
     * Only to support managed bean facility and test harnesses. NOT
     * for client code in general. 
     * Start a cache monitor Thread for expiration and eviction purposes.
     */
	public ReferenceDataCache() {
	  	this.service = new SDODataAccessClient(new HBasePojoDataAccessClient());
	}
	
	public List<String> words(String wildcard) {
		String wildcardStr = wildcard.toLowerCase() + "*";
		
		QWords query = QWords.newQuery();
		query.select(query.lemma())
		     .select(query.wordid());
		query.where(query.lemma().like(wildcardStr));
		query.setStartRange(0);
		query.setEndRange(12);
		
		List<String> result = new ArrayList<String>();
		DataGraph[] graphs = this.service.find(query);
		for (DataGraph graph : graphs) {
			Words word = (Words)graph.getRootObject();
	    	result.add(word.getLemma());
	    	wordMap.put(word.getLemma(), word);
		}		
		
		return result;
	}
	
	public Words getWord(String lemma) {
		return this.wordMap.get(lemma);
	}
	
	Map<Integer, List<Long>> hbaseAssemblyTime = new HashMap<Integer, List<Long>>();
	Map<Integer, List<Long>> hbaseParallelAssemblyTime = new HashMap<Integer, List<Long>>();
	public synchronized void addHBaseAssemblyTime(long nodeCount, long milliseconds, 
			QueryFetchType fetchType) {
		switch (fetchType) {
		case PARALLEL:
			map(nodeCount, milliseconds, hbaseParallelAssemblyTime);
			break;
		default:
			map(nodeCount, milliseconds, hbaseAssemblyTime);
		}
	}
	
	Map<Integer, List<Long>> rdbmsAssemblyTime = new HashMap<Integer, List<Long>>();
	Map<Integer, List<Long>> rdbmsParallelAssemblyTime = new HashMap<Integer, List<Long>>();
	public synchronized void addRdbmsAssemblyTime(long nodeCount, long milliseconds,
			QueryFetchType fetchType) {
		switch (fetchType) {
		case PARALLEL:
			map(nodeCount, milliseconds, rdbmsParallelAssemblyTime);
			break;
		default:
			map(nodeCount, milliseconds, rdbmsAssemblyTime);
		}
	}
	Map<Integer, List<Long>> cassandraAssemblyTime = new HashMap<Integer, List<Long>>();
	Map<Integer, List<Long>> cassandraParallelAssemblyTime = new HashMap<Integer, List<Long>>();
	public synchronized void addCassandraAssemblyTime(long nodeCount, long milliseconds,
			QueryFetchType fetchType) {
		switch (fetchType) {
		case PARALLEL:
			map(nodeCount, milliseconds, cassandraParallelAssemblyTime);
			break;
		default:
			map(nodeCount, milliseconds, cassandraAssemblyTime);
		}
	}
	
	private void map(long nodeCount, long milliseconds, Map<Integer, List<Long>> map) {
		for (int r : ranges) {
			if (nodeCount < r) {
				List<Long> values = map.get(r);
				if (values == null) {
					Integer range = new Integer(r);
					values = new ArrayList<Long>();
					map.put(range, values);
				}
				values.add(milliseconds);
				break;
			}
		}		
	}
	
	public synchronized  CartesianChartModel getHbaseCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries hbase = createSeries("HBase", this.hbaseAssemblyTime);  
        categoryModel.addSeries(hbase);  
        ChartSeries hbaseParallel = createSeries("HBase (parallel)", this.hbaseParallelAssemblyTime);  
        categoryModel.addSeries(hbaseParallel);  
        
        return categoryModel;
    }
	
	private ChartSeries createSeries(String name, Map<Integer, List<Long>> map) {
		ChartSeries series = new ChartSeries();
		series.setLabel(name);  
		for (int r : ranges) {
			List<Long> values = map.get(r);
			if (values == null)
				continue;
			long total = 0;
			for (Long value : values) {
				total += value.longValue();
			}
			 
			long average = total / values.size();
			series.set(String.valueOf(r), average);
		} 
		return series;
	}
	
	public synchronized  CartesianChartModel getRdbmsCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  

        ChartSeries rdbms = createSeries("MySql", this.rdbmsAssemblyTime);  
        categoryModel.addSeries(rdbms);  
        ChartSeries rdbmsParallel = createSeries("MySql (parallel)", this.rdbmsParallelAssemblyTime);  
        categoryModel.addSeries(rdbmsParallel);  
        
        return categoryModel;
    }	
	
	public synchronized  CartesianChartModel getCassandraCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries cassandra = createSeries("Cassandra", this.cassandraAssemblyTime);  
        categoryModel.addSeries(cassandra);  
        ChartSeries cassandraParallel = createSeries("Cassandra (parallel)", this.cassandraParallelAssemblyTime);  
        categoryModel.addSeries(cassandraParallel);  
        
        return categoryModel;
    }	

	public synchronized  CartesianChartModel getCombinedCategoryModel() {  
    	CartesianChartModel categoryModel = new CartesianChartModel();  
  
        ChartSeries cassandra = createSeries("Cassandra", this.cassandraAssemblyTime);  
        categoryModel.addSeries(cassandra);  
        ChartSeries cassandraParallel = createSeries("Cassandra (parallel)", this.cassandraParallelAssemblyTime);  
        categoryModel.addSeries(cassandraParallel);  

        ChartSeries hbase = createSeries("HBase", this.hbaseAssemblyTime);  
        categoryModel.addSeries(hbase);  
        ChartSeries hbaseParallel = createSeries("HBase (parallel)", this.hbaseParallelAssemblyTime);  
        categoryModel.addSeries(hbaseParallel);  
  
        
        return categoryModel;
    }	
}
