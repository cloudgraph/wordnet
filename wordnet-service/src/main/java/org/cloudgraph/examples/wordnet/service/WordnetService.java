package org.cloudgraph.examples.wordnet.service;

import java.util.List;

import org.plasma.config.DataAccessProviderName;

public interface WordnetService {

	public DataAccessProviderName getProvider();
	public Wordnet getAllRelations(String lemma);
	public Wordnet getAllRelations(String lemma, long wordid);

	public Wordnet getSynonyms(String lemma);
	public Wordnet getSynonyms(String lemma, long wordid);
}
