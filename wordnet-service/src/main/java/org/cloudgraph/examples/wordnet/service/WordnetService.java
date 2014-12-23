package org.cloudgraph.examples.wordnet.service;

import org.cloudgraph.config.QueryFetchType;
import org.plasma.config.DataAccessProviderName;

public interface WordnetService {

	public DataAccessProviderName getProvider();
	public Wordnet getAllRelations(String lemma);
	public Wordnet getAllRelations(String lemma, long wordid);
	public Wordnet getAllRelations(String lemma, QueryFetchType fetchType);

	public Wordnet getSynonyms(String lemma);
	public Wordnet getSynonyms(String lemma, long wordid);
}
