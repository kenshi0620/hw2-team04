package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class AustinMaKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * File location of NER model
   */
  File nerModelFile;
  
  /**
   * Ling pipe chunker that will do the NER for us
   */
  Chunker chunker;
  
  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    
    nerModelFile = new File("src/main/resources/ne-en-bio-genetag.HmmChunker");
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(nerModelFile);
    } catch (IOException e) {
      System.err.println("IOException in creating chunker");
      throw new ResourceInitializationException("Unable to load NER model file", "load_ner_model_error",
              new Object[] { nerModelFile }, e);
    } catch (ClassNotFoundException e) {
      System.err.println("ClassNotFoundException in creating chunker");
      throw new ResourceInitializationException("Unable to load NER model file", "load_ner_model_error",
              new Object[] { nerModelFile }, e);
    }
  }

  @Override
  protected List<Keyterm> getKeyterms(String text) {
    List<Keyterm> keyTerms = new ArrayList<Keyterm>();
    Chunking chunking = chunker.chunk(text);
    for (Chunk chunk : chunking.chunkSet()) {
      int start = chunk.start();
      int end = chunk.end();
      String term = text.substring(start, end);
      keyTerms.add(new Keyterm(term));
    }
    return keyTerms;
  }
}
