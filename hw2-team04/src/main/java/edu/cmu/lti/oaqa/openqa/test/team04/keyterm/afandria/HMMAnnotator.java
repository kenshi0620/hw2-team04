package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.util.AbstractExternalizable;

/**
 * This annotator uses the LingPipe HMM Chunker to find gene mentions using a pre-trained model
 * stored in resources.
 * 
 * @author afandria
 */
public class HMMAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * How much score/confidence to give for all named entities recognized
   */
  private static int DEFAULT_SCORE = 7;

  /**
   * Where our history is stored (default)
   */
  private static String HMM_MODEL_PATH = "src/main/resources/data/ne-en-bio-genetag.HmmChunker";

  /**
   * Get a LingPipe chunker
   * 
   * @return an HMM chunker
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private Chunker getChunker() throws IOException, ClassNotFoundException {
    // TODO get actual config
    String fp = HMM_MODEL_PATH;
    File f = new File(fp);
    Chunker chunker = (Chunker) AbstractExternalizable.readObject(f);
    return chunker;
  }

  /**
   * Iterates through sentences and passes them to the HMM chunker, extracting gene mentions. From
   * here, we will add them to the CAS' annotation list. Each gene mention will receive the
   * DEFAULT_SCORE.
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    if (DEBUG)
      System.out.println("LingPipe HMM Annotator!");
    Chunker chunker;
    try {
      chunker = getChunker();
    } catch (Exception e) {
      throw new AnalysisEngineProcessException();
    }
    Map<String, String> sentences = AnnotationUtilities.getSentences(arg0.getDocumentText());
    Iterator<String> keyIterator = sentences.keySet().iterator();
    while (keyIterator.hasNext()) {
      String k = keyIterator.next();
      Set<Chunk> spans = chunker.chunk(sentences.get(k)).chunkSet();
      Iterator<Chunk> spanIterator = spans.iterator();
      while (spanIterator.hasNext()) {
        Chunk c = spanIterator.next();
        // TODO (afandria): this is also pretty ugly Java; find a more succinct way to say the
        // following
        Integer normalizedBegin = c.start()
                - AnnotationUtilities.getSpaces(sentences.get(k).substring(0, c.start()));
        if (normalizedBegin < 0)
          normalizedBegin = 0;
        Integer normalizedEnd = c.end()
                - AnnotationUtilities.getSpaces(sentences.get(k).substring(0, c.end())) - 1;
        if (c.start() == c.end())
          normalizedEnd = normalizedBegin;
        // add everything to the index
        Gene g = new Gene(arg0);
        g.setBegin(normalizedBegin);
        g.setEnd(normalizedEnd);
        g.setContent(sentences.get(k).substring(c.start(), c.end()));
        g.setIdentifier(k);
        g.setConfidence(DEFAULT_SCORE);
        g.addToIndexes();
        if (DEBUG)
          System.out.println(c.toString() + " " + sentences.get(k).substring(c.start(), c.end()));
      }
    }
  }
}
