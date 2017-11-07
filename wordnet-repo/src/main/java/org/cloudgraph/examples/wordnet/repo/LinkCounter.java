package org.cloudgraph.examples.wordnet.repo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.examples.wordnet.model.Lexlinks;
import org.cloudgraph.examples.wordnet.model.Semlinks;
import org.cloudgraph.examples.wordnet.model.Senses;
import org.cloudgraph.examples.wordnet.model.Synsets;
import org.cloudgraph.examples.wordnet.model.Words;
import org.plasma.sdo.PlasmaDataGraphVisitor;



import commonj.sdo.DataObject;

public class LinkCounter implements PlasmaDataGraphVisitor {

    private static Log log = LogFactory.getLog(LinkCounter.class);
	private Map<String, Integer> map = new HashMap<String, Integer>();
	private Words root;

	public LinkCounter(Words root) {
		super();
		this.root = root;
		this.root.accept(this);
	}
	
	public Map<String, Integer> getMap() {
		return map;
	}

	public Words getRoot() {
		return root;
	}

	@Override
	public void visit(DataObject target, DataObject source,
			String sourcePropertyName, int level) {
		if (target instanceof Semlinks) {
			Semlinks link = (Semlinks) target;
			String key = link.getLinktypes().getLink();
			Synsets ss = link.getSynsets();
			if (ss != null)
				mapSynsets(ss, key);
			ss = link.getSynsets1();
			if (ss != null)
				mapSynsets(ss, key);
		} else if (target instanceof Lexlinks) {
			Lexlinks link = (Lexlinks) target;
			String key = link.getLinktypes().getLink();
			Synsets ss = link.getSynsets();
			if (ss != null)
				mapSynsets(ss, key);
			ss = link.getSynsets1();
			if (ss != null)
				mapSynsets(ss, key);
		}
	}

	private void mapSynsets(Synsets ss, String key) {
		if (ss != null)
			for (Senses sense : ss.getSenses()) {
				if (sense.getWords() != null) {
					if (!map.containsKey(key)) {
						map.put(key, new Integer(1));
					}
					else {
						Integer current = map.get(key);
						map.put(key, new Integer(current.intValue() + 1));
					}
				}
			}

	}
}
