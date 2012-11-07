package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.ecd.BaseExperimentBuilder;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class Byang1KeytermExtrator extends AbstractKeytermExtractor {
	
	
	//protected PersistenceProvider persistence;
	Chunker chunker;
	
	@Override
	public void initialize(UimaContext c) throws ResourceInitializationException {
		super.initialize(c);
		// gets chunker
		chunker = null;
		File modelFile = new File("./src/main/resources/ne-en-bio-genetag.HmmChunker");
		try {
			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected List<Keyterm> getKeyterms(String arg0) {
		
		
		// parses raw sentence with chunker
		Chunking chunking = chunker.chunk(arg0);
	    Set<Chunk> chunkSet = chunking.chunkSet();
	    List<Keyterm> keyTermList = new ArrayList<Keyterm>();
	    // transforms chunks into annotations
	    for (Chunk chunk : chunkSet) {
	    	Keyterm keyTerm = new Keyterm(arg0.substring(chunk.start(), chunk.end()));
	    	keyTermList.add(keyTerm);
	    }
		
		return keyTermList;
	}
}
