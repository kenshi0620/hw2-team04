package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria.PosTagNamedEntityRecognizer;

/**
 * @author afandria
 *
 */
/**
 * @author afandria
 * 
 */
public class NNKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  private PosTagNamedEntityRecognizer ner;
  /**
   * Gets parameterized paths and initializes LingPipe chunker, CMU dictionary, and HW1 history
   * 
   * @param c
   *          the UimaContext to grab param values from
   */
  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    this.ner = new PosTagNamedEntityRecognizer();
  }

  private static Comparator<Map.Entry<Integer, Integer>> SpanComparator = new Comparator<Map.Entry<Integer, Integer>>() {
    public int compare(Map.Entry<Integer, Integer> x, Map.Entry<Integer, Integer> y) {
      if (x.getKey() > y.getKey()) {
        return 1;
      } else if (x.getKey() == y.getKey()) {
        return 0;
      } else {
        return -1;
      }
    }
  };
  
  /**
   * Extracts keyterms from a query (from afandria's HW1)
   * 
   * @param arg0
   *          is the query to get keyterms from
   * @return list of keyterms found in arg0
   */
  @Override
  protected List<Keyterm> getKeyterms(String arg0) {
    // initialize list
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    Set<Map.Entry<Integer, Integer>> spans = ner.getGeneSpans(arg0).entrySet();
    List<Map.Entry<Integer, Integer>> spanList = new ArrayList<Map.Entry<Integer, Integer>>(spans);
    Collections.sort(spanList, SpanComparator);
    Iterator<Map.Entry<Integer, Integer>> spanIterator = spanList.iterator();
    while (spanIterator.hasNext()) {
      Map.Entry<Integer, Integer> me = spanIterator.next();
      keyterms.add(new Keyterm(arg0.substring(me.getKey(), me.getValue())));
      System.out.println(" NN: " + arg0.substring(me.getKey(), me.getValue()));
    }
    return keyterms;
  }

}
