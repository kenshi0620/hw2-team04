package edu.cmu.lti.oaqa.openqa.test.team04.ner_afandria;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * This annotator uses the Stanford NER to extract noun phrases (NNPs, well all named entities
 * should be NNPs) as a jumping point for our gene mention tagger.
 * 
 * @author afandria
 */
public class NNPAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * How much score/confidence to give for all named entities recognized
   */
  private static int DEFAULT_SCORE = 6;

  /**
   * Iterates through sentences and passes them to the NER, extracting gene mentions. From here, we
   * will add them to the CAS' annotation list. Each gene mention will receive the DEFAULT_SCORE.
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    if (DEBUG)
      System.out.println("Noun Phrase Annotator!");
    PosTagNamedEntityRecognizer ner;
    try {
      ner = new PosTagNamedEntityRecognizer();
    } catch (ResourceInitializationException e) {
      throw new AnalysisEngineProcessException();
    }
    // get annotations
    AnnotationIndex<Annotation> annotations = arg0.getAnnotationIndex(Gene.type);
    // get sentences
    Map<String, String> sentences = AnnotationUtilities.getSentences(arg0.getDocumentText());
    Iterator<String> keyIterator = sentences.keySet().iterator();
    while (keyIterator.hasNext()) {
      String k = keyIterator.next();
      Set<Map.Entry<Integer, Integer>> spans = ner.getGeneSpans(sentences.get(k)).entrySet();
      Iterator<Map.Entry<Integer, Integer>> spanIterator = spans.iterator();
      while (spanIterator.hasNext()) {
        Map.Entry<Integer, Integer> me = spanIterator.next();
        // TODO (afandria): this is also pretty ugly Java; find a more succinct way to say the
        // following
        Integer normalizedBegin = me.getKey()
                - AnnotationUtilities.getSpaces(sentences.get(k).substring(0, me.getKey()));
        if (normalizedBegin < 0)
          normalizedBegin = 0;
        Integer normalizedEnd = me.getValue()
                - AnnotationUtilities.getSpaces(sentences.get(k).substring(0, me.getValue())) - 1;
        if (me.getKey() == me.getValue())
          normalizedEnd = normalizedBegin;
        // add everything to the index
        Gene g = new Gene(arg0);
        g.setBegin(normalizedBegin);
        g.setEnd(normalizedEnd);
        g.setContent(sentences.get(k).substring(me.getKey(), me.getValue()));
        g.setIdentifier(k);
        g.setConfidence(DEFAULT_SCORE);
        if (!annotations.contains((FeatureStructure) g))
          g.addToIndexes();
        if (DEBUG)
          System.out.println(sentences.get(k).substring(me.getKey(), me.getValue()));
      }
    }
  }
}
