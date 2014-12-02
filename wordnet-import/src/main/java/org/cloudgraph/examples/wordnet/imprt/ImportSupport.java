package org.cloudgraph.examples.wordnet.imprt;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.cloudgraph.examples.wordnet.Lexlinks;
import org.cloudgraph.examples.wordnet.Linktypes;
import org.cloudgraph.examples.wordnet.Semlinks;
import org.cloudgraph.examples.wordnet.Senses;
import org.cloudgraph.examples.wordnet.Synsets;
import org.cloudgraph.examples.wordnet.Words;
import org.cloudgraph.examples.wordnet.query.QLinktypes;
import org.cloudgraph.examples.wordnet.query.QSynsets;
import org.cloudgraph.examples.wordnet.query.QWords;
import org.cloudgraph.mapreduce.GraphService;
import org.plasma.sdo.PlasmaChangeSummary;
import org.plasma.sdo.core.NullValue;
import org.plasma.sdo.helper.PlasmaDataFactory;

import commonj.sdo.DataGraph;
import commonj.sdo.Property;

public class ImportSupport {
	
 	private static Log log = LogFactory.getLog(ImportSupport.class);

	
	// load word graphs with senses
	protected Words mapWordGraph(Words copyRoot, GraphService service, Context context) throws IOException {

		 DataGraph newGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		 newGraph.getChangeSummary().beginLogging(); // log changes from this point
		 PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)newGraph.getChangeSummary();
		 Words newRoot = (Words)newGraph.createRootObject(copyRoot.getType());
		 for (Words.PROPERTY p : Words.PROPERTY.values()) {
			 Property prop = copyRoot.getType().getProperty(p.name());
			 if (prop.getType().isDataType())
		         newRoot.set(prop, copyRoot.get(prop));
		 }
		 for (Senses copySenses : copyRoot.getSenses()) {
			 Senses newSenses = newRoot.createSenses();
			 for (Senses.PROPERTY p : Senses.PROPERTY.values()) {
				 Property prop = copySenses.getType().getProperty(p.name());
				 if (prop.getType().isDataType())
					 newSenses.set(prop, copySenses.get(prop));
			 }
			 Synsets existingSynsets = copySenses.getSynsets();
			 Synsets synset = fetchSynset(existingSynsets.getSynsetid(),
					 existingSynsets.getPos(),
					 existingSynsets.getLexdomains().getLexdomainname(), service, context);
	         synset.setDataGraph(newGraph);
	         synset.setContainer(newSenses);
	         Property containmentProp2 = newSenses.getType().getProperty(Senses.PROPERTY.synsets.name());
	         synset.setContainmentProperty(containmentProp2);
	         //synset.addSenses(newRoot); // creates a directed link back to root eliminating it from traversals
	         newSenses.setSynsets(synset); // does not set opposite as modified in change summary so not comitted !!!
	         changeSummary.modified(synset, containmentProp2.getOpposite(), new NullValue());
		 }
		 
		 return newRoot;
	}	

	public Semlinks mapSemLinkGraph(Semlinks copyRoot, GraphService service, Context context) throws IOException {
		 DataGraph newGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		 PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)newGraph.getChangeSummary();
		 changeSummary.beginLogging(); // log changes from this point
		 Semlinks newRoot = (Semlinks)newGraph.createRootObject(copyRoot.getType());
		 for (Semlinks.PROPERTY p : Semlinks.PROPERTY.values()) {
			 Property prop = copyRoot.getType().getProperty(p.name());
			 if (prop.getType().isDataType())
				 if (copyRoot.isSet(prop))
		             newRoot.set(prop, copyRoot.get(prop));
		 }
		 
         Synsets existingSynsets = copyRoot.getSynsets();
		 Synsets synset = fetchSynset(existingSynsets.getSynsetid(),
				 existingSynsets.getPos(),
				 existingSynsets.getLexdomains().getLexdomainname(), service, context);
         synset.setDataGraph(newGraph);
         synset.setContainer(newRoot);
         Property containmentProp = newRoot.getType().getProperty(Semlinks.PROPERTY.synsets.name());
         synset.setContainmentProperty(containmentProp);
		 newRoot.setSynsets(synset);
         changeSummary.modified(synset, containmentProp.getOpposite(), new NullValue());
		 
         Synsets existingSynsets1 = copyRoot.getSynsets1();
		 Synsets synset1 = fetchSynset(existingSynsets1.getSynsetid(),
				 existingSynsets1.getPos(),
				 existingSynsets1.getLexdomains().getLexdomainname(), service, context);
		 synset1.setDataGraph(newGraph);
		 synset1.setContainer(newRoot);
         Property containmentProp1 = newRoot.getType().getProperty(Semlinks.PROPERTY.synsets1.name());
		 synset1.setContainmentProperty(containmentProp1);
		 newRoot.setSynsets1(synset1);
         changeSummary.modified(synset1, 
        		 containmentProp1.getOpposite(), new NullValue());
		 
		 Linktypes linktype = fetchLinktype(copyRoot.getLinktypes().getLinkid(), service, context);
		 linktype.setDataGraph(newGraph);
		 linktype.setContainer(newRoot);
		 linktype.setContainmentProperty(newRoot.getType().getProperty(Semlinks.PROPERTY.linktypes.name()));
		 newRoot.setLinktypes(linktype);
         //changeSummary.modified(synset1, 
         // 		 synset1.getType().getProperty(Synsets.PROPERTY.semlinks.name()), new NullValue());
		 
		 return newRoot;
    } 	
	
	protected Lexlinks mapLexLinkGraph(Lexlinks copyRoot, GraphService service, Context context) throws IOException {
		 DataGraph newGraph = PlasmaDataFactory.INSTANCE.createDataGraph();
		 PlasmaChangeSummary changeSummary = (PlasmaChangeSummary)newGraph.getChangeSummary();
		 changeSummary.beginLogging(); // log changes from this point
		 Lexlinks newRoot = (Lexlinks)newGraph.createRootObject(copyRoot.getType());
		 for (Lexlinks.PROPERTY p : Lexlinks.PROPERTY.values()) {
			 Property prop = copyRoot.getType().getProperty(p.name());
			 if (prop.getType().isDataType())
				 if (copyRoot.isSet(prop))
		             newRoot.set(prop, copyRoot.get(prop));
		 }
         Synsets existingSynsets = copyRoot.getSynsets();
		 Synsets synset = fetchSynset(existingSynsets.getSynsetid(),
				 existingSynsets.getPos(),
				 existingSynsets.getLexdomains().getLexdomainname(), service, context);
         synset.setDataGraph(newGraph);
         synset.setContainer(newRoot);
         Property containmentProp = newRoot.getType().getProperty(Lexlinks.PROPERTY.synsets.name());
         synset.setContainmentProperty(containmentProp);
         newRoot.setSynsets(synset);
         changeSummary.modified(synset, containmentProp.getOpposite(), new NullValue());
		 
         Synsets existingSynsets1 = copyRoot.getSynsets1();
		 Synsets synset1 = fetchSynset(existingSynsets1.getSynsetid(),
				 existingSynsets1.getPos(),
				 existingSynsets1.getLexdomains().getLexdomainname(), service, context);
		 synset1.setDataGraph(newGraph);
		 synset1.setContainer(newRoot);
         Property containmentProp1 = newRoot.getType().getProperty(Lexlinks.PROPERTY.synsets1.name());
		 synset1.setContainmentProperty(containmentProp1);
         newRoot.setSynsets1(synset1);
         changeSummary.modified(synset1, containmentProp1.getOpposite(), new NullValue());
		 
         Words existingWords = copyRoot.getWords();
         Words word = fetchWord(existingWords.getWordid(),
        		 existingWords.getLemma(), service, context);
         word.setDataGraph(newGraph);
         word.setContainer(newRoot);
         Property containmentPropWord = newRoot.getType().getProperty(Lexlinks.PROPERTY.words.name());
         word.setContainmentProperty(containmentPropWord);
         newRoot.setWords(word);
         changeSummary.modified(word, containmentPropWord.getOpposite(), new NullValue());
		 
         Words existingWords1 = copyRoot.getWords1();
         Words word1 = fetchWord(existingWords1.getWordid(),
        		 existingWords1.getLemma(), service, context);
         word1.setDataGraph(newGraph);
         word1.setContainer(newRoot);
         Property containmentPropWord1 = newRoot.getType().getProperty(Lexlinks.PROPERTY.words1.name());
         word1.setContainmentProperty(containmentPropWord1);
         newRoot.setWords1(word1);
         changeSummary.modified(word1, containmentPropWord1.getOpposite(), new NullValue());
         
		 Linktypes linktype = fetchLinktype(copyRoot.getLinktypes().getLinkid(), service, context);
		 linktype.setDataGraph(newGraph);
		 linktype.setContainer(newRoot);
		 linktype.setContainmentProperty(newRoot.getType().getProperty(Semlinks.PROPERTY.linktypes.name()));
		 newRoot.setLinktypes(linktype);
       return newRoot;
	}
	
	protected Synsets fetchSynset(int synsetid, String pos, String lexdomainName, GraphService service, Context context) throws IOException {
		QSynsets query = QSynsets.newQuery();
		query.select(query.synsetid())
	         .select(query.pos())
	         .select(query.lexdomains().lexdomainname())
	         //.select(query.semlinks().wildcard())
	         //.select(query.semlinks().synsets1().synsetid())
	         //.select(query.semlinks().synsets1().pos())
	         //.select(query.semlinks().synsets1().lexdomains().lexdomainname())
	    ;
		 
		query.where(query.synsetid().eq(synsetid)   
			 .and(query.pos().eq(pos)  
			 .and(query.lexdomains().lexdomainname().eq(lexdomainName))));
		
		DataGraph[] graphs = service.find(query, context);
		if (graphs == null || graphs.length != 1) { 
			if (graphs == null)
			    throw new RuntimeException("expected single Synset result, not 0");
			else
			    throw new RuntimeException("expected single Synset result, not " + graphs.length);
	    }
		Synsets result = (Synsets)graphs[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}
	
	protected Words fetchWord(int wordid, String lemma, GraphService service, Context context) throws IOException {
		QWords query = QWords.newQuery();
		query.select(query.wordid())
	         .select(query.lemma());
		query.where(query.wordid().eq(wordid) 
			 .and(query.lemma().eq(lemma)));  
		DataGraph[] graphs = service.find(query, context);
		if (graphs == null || graphs.length != 1)
			throw new RuntimeException("expected single result");
		Words result = (Words)graphs[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}	
	
	private Linktypes fetchLinktype(int linkid, GraphService service, Context context) throws IOException {
		QLinktypes query = QLinktypes.newQuery();
		query.select(query.wildcard());
		 
		query.where(query.linkid().eq(linkid));  
		DataGraph[] graphs = service.find(query, context);
		if (graphs == null || graphs.length != 1)
			throw new RuntimeException("expected single result");
		Linktypes result = (Linktypes)graphs[0].getRootObject();
		result.setDataGraph(null); // so can re parent
		return result;
	}	

}
