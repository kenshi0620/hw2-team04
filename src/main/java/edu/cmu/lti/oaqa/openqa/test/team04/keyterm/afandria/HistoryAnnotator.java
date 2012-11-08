package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.FileUtils;

/**
 * Filters current annotations (Gene's) and promotes their scores if they existed in our sample set.
 * 
 * @author afandria
 */
public class HistoryAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * Score to increase by if found in history
   */
  private static int HISTORY_BONUS = 6;

  /**
   * Where our history is stored (default)
   */
  private static String DICT_PATH = "src/main/resources/data/sample.out";

  /**
   * Retrieves a set of gold standard gene mentions, formatted in a specific way (sample.out), and
   * stores them in a set
   * 
   * @return Set of strings contained by history in DICT_PATH
   * @throws AnalysisEngineProcessException
   */
  private Set<String> getDictionary() throws AnalysisEngineProcessException {
    Set<String> dictionary = new HashSet<String>();
    try {
      String[] lines = FileUtils.file2String(new File(DICT_PATH)).split("\\n");
      for (int i = 0; i < lines.length; i++) {
        if (lines[i].indexOf(";") != 0) {
          dictionary.add(lines[i].split("|")[3]);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException();
    }
    return dictionary;
  }

  /**
   * Goes through annotations and boosts annotation confidence based on history membership. It will
   * boost confidence by HISTORY_BONUS.
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    if (DEBUG)
      System.out.println("History Annotator!");
    // get the named entity history
    Set<String> dictionary = getDictionary();
    // get annotations
    AnnotationIndex<Annotation> annotations = arg0.getAnnotationIndex(Gene.type);
    Iterator<Annotation> annotationIterator = annotations.iterator();
    while (annotationIterator.hasNext()) {
      Gene a = (Gene) annotationIterator.next();
      // increment confidence if it's in the history
      if (dictionary.contains(a.getContent())) {
        a.setConfidence(a.getConfidence() + HISTORY_BONUS);
      }
      if (DEBUG)
        System.out.println(a.getConfidence() + " " + a.getContent());
    }
  }

}
