package org.cloudgraph.examples.wordnet.service;

import java.util.List;

public class SemanticRelationSet extends RelationSet {
	private List<SemanticRelation> links;

	public SemanticRelationSet(List<SemanticRelation> links, String type) {
		super(type);
		this.links = links;
	}

	public List<SemanticRelation> getLinks() {
		return links;
	}

	public void setLinks(List<SemanticRelation> links) {
		this.links = links;
	}
	

}
