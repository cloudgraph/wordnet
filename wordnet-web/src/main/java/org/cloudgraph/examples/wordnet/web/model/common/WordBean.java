package org.cloudgraph.examples.wordnet.web.model.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.config.QueryFetchType;
import org.cloudgraph.examples.wordnet.model.Words;
import org.cloudgraph.examples.wordnet.service.WordRelations;
import org.cloudgraph.examples.wordnet.service.Wordnet;
import org.cloudgraph.examples.wordnet.service.WordnetService;
import org.cloudgraph.examples.wordnet.service.WordnetServiceImpl;
import org.cloudgraph.examples.wordnet.web.model.ModelBean;
import org.cloudgraph.examples.wordnet.web.model.cache.ReferenceDataCache;
import org.plasma.config.DataAccessProviderName;
import org.plasma.sdo.helper.PlasmaXMLHelper;
import org.plasma.sdo.xml.DefaultOptions;
import org.primefaces.event.SelectEvent;

import commonj.sdo.DataGraph;
import commonj.sdo.helper.XMLDocument;

@ManagedBean(name="WordBean")
@SessionScoped
public class WordBean extends ModelBean {
 	private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(WordBean.class);
    private String word;
    protected WordnetService hbase;
    protected WordnetService rdbms;
    protected WordnetService cassandra;
    private ReferenceDataCache cache;
    private static List<WordRelations> EMPTY_RELATION_LIST = new ArrayList<WordRelations>();
    private Map<DataAccessProviderName, Wordnet> relations = new HashMap<DataAccessProviderName, Wordnet>();
    private int tabIndex;
    private String fetchType = QueryFetchType.SERIAL.name();
    
	public WordBean() {
	  	this.hbase = new WordnetServiceImpl(DataAccessProviderName.HBASE);
	  	this.rdbms = new WordnetServiceImpl(DataAccessProviderName.JDBC);
	  	this.cassandra = new WordnetServiceImpl(DataAccessProviderName.CASSANDRA);
	  	this.cache = this.beanFinder.findReferenceDataCache();
    }
        
    public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	public String getWord() {
		return this.word;
	}
	
	public void setWord(String value) {
		if (this.word != null && value != null && !this.word.equals(value))
			this.relations.clear();
		this.word = value.toLowerCase();
	}
    
	public boolean getHasWord() {
		return this.word != null;
	}
	
    public String getDisplayWord() {
    	StringBuilder buf = new StringBuilder();
    	String[] tokens = this.word.split("\\s+");
    	for (String token : tokens) {
    		buf.append(token.substring(0,1).toUpperCase());
    		buf.append(token.substring(1));
    		buf.append(" ");
    	}
		return buf.toString();
	}
    
	public void handleWordChange(SelectEvent event) {  
        try {   
        } catch (Throwable t) {
        	log.error(t.getMessage(), t);
 	        FacesMessage msg = new FacesMessage("Internal Error");  	       
	        FacesContext.getCurrentInstance().addMessage(null, msg);          	
        } finally {
        }       		
	}
	
    public String getFetchType() {
		return fetchType;
	}

	public void setFetchType(String type) {
		if (type != null && !type.equals(this.fetchType)) { 
		    this.fetchType = type;
		    this.relations.clear();
		}
	}
	
	public void handleFetchTypeChange(AjaxBehaviorEvent event) {
		this.relations.clear();
	}
	
	/*
	public void handleFetchTypeChange(ActionEvent event) {
		this.word = null;
		this.relations.clear();
	}
	public void handleFetchTypeChange() {
		this.word = null;
		this.relations.clear();
	}
	*/
	private List<SelectItem> fetchTypeItems;
	public List<SelectItem> getFetchTypeItems() {
		if (fetchTypeItems == null) {
			fetchTypeItems = new ArrayList<SelectItem>();
			fetchTypeItems.add(new SelectItem(QueryFetchType.SERIAL.name(), 
					"synchronous"));
			fetchTypeItems.add(new SelectItem(QueryFetchType.PARALLEL.name(), 
					"parallel"));
		}
		return fetchTypeItems;
	}
	
	public List<WordRelations> getHbaseRelations() {
		try {
		    return getRelations(this.hbase);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return EMPTY_RELATION_LIST;
		}
	}
	
	public List<WordRelations> getRdbmsRelations() {
		try {
		    return getRelations(this.rdbms);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return EMPTY_RELATION_LIST;
		}
	}
	
	public List<WordRelations> getCassandraRelations() {
		try {
		    return getRelations(this.cassandra);
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
			return EMPTY_RELATION_LIST;
		}
	}
	
	private List<WordRelations> getRelations(WordnetService service) {
		
		Wordnet wordnet = this.relations.get(service.getProvider());
		
		if (this.word == null)
			return EMPTY_RELATION_LIST;
		if (wordnet != null)
			return wordnet.getRelations();		
		
		try  {
            Words wd = this.cache.getWord(this.word);
            if (wd != null) {            	
            	wordnet = service.getAllRelations(wd.getLemma(), 
            			QueryFetchType.valueOf(this.fetchType));
            }
            else {
            	wordnet = service.getAllRelations(this.word,
            			QueryFetchType.valueOf(this.fetchType));
            }
            if (wordnet == null) {
    	        FacesMessage msg = new FacesMessage("No results found for '" + this.word + "'");  	       
    	        FacesContext.getCurrentInstance().addMessage(null, msg);  
    	        return EMPTY_RELATION_LIST;
            }
            else {
            	this.relations.put(service.getProvider(), wordnet);
            	switch (service.getProvider()) {
            	case HBASE:
            		this.cache.addHBaseAssemblyTime(wordnet.getGraphNodeCount(), 
            				wordnet.getGraphAssemblyTime(),
            				QueryFetchType.valueOf(this.fetchType));
            		break;
            	case JDBC:
            		this.cache.addRdbmsAssemblyTime(wordnet.getGraphNodeCount(), 
            				wordnet.getGraphAssemblyTime(),
            				QueryFetchType.valueOf(this.fetchType));
            		break;
            	case CASSANDRA:
            		this.cache.addCassandraAssemblyTime(wordnet.getGraphNodeCount(), 
            				wordnet.getGraphAssemblyTime(),
            				QueryFetchType.valueOf(this.fetchType));
            		break;
            	default:
            	}
            	
            	return wordnet.getRelations();
            }
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return EMPTY_RELATION_LIST;
	}
 	
    public Wordnet getHbaseWordnet() {
		Wordnet wordnet = this.relations.get(this.hbase.getProvider());
		return wordnet;
	}
    
    public boolean getHasHbaseWordnet() {
		Wordnet wordnet = this.relations.get(this.hbase.getProvider());
    	return wordnet != null;
    }
    
    public Wordnet getRdbmsWordnet() {
		Wordnet wordnet = this.relations.get(this.rdbms.getProvider());
		return wordnet;
	}
    
    public boolean getHasRdbmsWordnet() {
		Wordnet wordnet = this.relations.get(this.rdbms.getProvider());
    	return wordnet != null;
    }
    
    public Wordnet getCassandraWordnet() {
		Wordnet wordnet = this.relations.get(this.cassandra.getProvider());
		return wordnet;
	}
    
    public boolean getHasCassandraWordnet() {
		Wordnet wordnet = this.relations.get(this.cassandra.getProvider());
    	return wordnet != null;
    }

	protected String serializeGraph(DataGraph graph) throws IOException
    {
        DefaultOptions options = new DefaultOptions(
        		graph.getRootObject().getType().getURI());
        options.setRootNamespacePrefix("test");
        
        XMLDocument doc = PlasmaXMLHelper.INSTANCE.createDocument(graph.getRootObject(), 
        		graph.getRootObject().getType().getURI(), 
        		null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PlasmaXMLHelper.INSTANCE.save(doc, os, options);        
        os.flush();
        os.close(); 
        String xml = new String(os.toByteArray());
        return xml;
    }
}
