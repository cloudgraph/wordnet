package org.cloudgraph.examples.wordnet.service;

import java.util.List;

import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.examples.wordnet.model.Words;
import org.plasma.sdo.core.CoreNode;

public class Wordnet {
    private Words word;
    private List<WordRelations> relations;
    
    
	public Wordnet(Words word, List<WordRelations> relations) {
		super();
		this.word = word;
		this.relations = relations;
	}
	
	public Words getWord() {
		return word;
	}
	public List<WordRelations> getRelations() {
		return relations;
	}
    
	public long getGraphAssemblyTime()
	{
		CoreNode node = (CoreNode)this.word;
		Object obj = node.getValueObject().get(CloudGraphConstants.GRAPH_ASSEMBLY_TIME);    	
    	if (obj != null && obj instanceof Long)
    		return ((Long)obj).longValue();
    	else
    		return 0;
	}
	
	public long getGraphNodeCount()
	{
		CoreNode node = (CoreNode)this.word;
		Object obj = node.getValueObject().get(CloudGraphConstants.GRAPH_NODE_COUNT);    	
    	if (obj != null && obj instanceof Long)
    		return ((Long)obj).longValue();
    	else
    		return 0;
	}
	
	public float getGraphNodeAssemblyTime() {
	    return (float)getGraphAssemblyTime() / (float)getGraphNodeCount();
    }

	public long getGraphDepth()
	{
		CoreNode node = (CoreNode)this.word;
		Object obj = node.getValueObject().get(CloudGraphConstants.GRAPH_DEPTH);    	
    	if (obj != null && obj instanceof Long)
    		return ((Long)obj).longValue();
    	else
    		return 0;
	}
	
	public long getGraphThreadCount()
	{
		CoreNode node = (CoreNode)this.word;
		Object obj = node.getValueObject().get(CloudGraphConstants.GRAPH_THREAD_COUNT);    	
    	if (obj != null && obj instanceof Long)
    		return ((Long)obj).longValue();
    	else
    		return 0;
	}
	
}
