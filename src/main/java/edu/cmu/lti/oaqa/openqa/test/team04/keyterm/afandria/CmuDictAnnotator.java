package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.FileUtils;

public class CmuDictAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * This is where CMUDict is (default)
   */
  private static String DICT_PATH = "src/main/resources/data/cmudict.0.7a.txt";

  /**
   * Retrieves a set of words found in CMUDict
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
          dictionary.add(lines[i].substring(0, lines[i].indexOf(" ")));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException();
    }
    return dictionary;
  }

  /**
   * Depending on CMUDict membership + heuristics, we will either increase or decrease the
   * confidence of the mention.
   * 
   * @param dictionary
   *          CMUDict
   * @param a
   *          gene mention
   */
  private void modifyConfidence(Set<String> dictionary, Gene a) {
    // decrement confidence if it has some invalid characters
    if (a.getContent().contains("%") || a.getContent().contains("@")) {
      a.setConfidence(a.getConfidence() - 3);
    }
    // increment confidence if it's just one letter
    if (a.getContent().length() == 1) {
      char x = a.getContent().toCharArray()[0];
      if (x >= 'a' && x <= 'z') {
        a.setConfidence(a.getConfidence() - 10);
      } else {
        a.setConfidence(a.getConfidence() + 1);
      }
      return;
    }
    // decrement confidence if in CMUDict
    if (dictionary.contains(a.getContent().toUpperCase())) {
      a.setConfidence(a.getConfidence() - 4);
    } else {
      if (dictionary.contains(a.getContent().split(" ")[0].toUpperCase())) {
        // decrement confidence if first word is in CMUDict
        a.setConfidence(a.getConfidence() - 0);
      } else {
        a.setConfidence(a.getConfidence() + 1);
      }
    }
    // increment confidence if all words are not in CMUDict
    String[] words = a.getContent().split(" ");
    Collection<String> wordCollection = new Vector<String>();
    for (int i = 0; i < words.length; i++) {
      wordCollection.add(words[i].toUpperCase());
    }
    if (!dictionary.containsAll(wordCollection)) {
      a.setConfidence(a.getConfidence() + 2);
    } else {
      a.setConfidence(a.getConfidence() - 1);
    }
    if (DEBUG)
      System.out.println(a.getConfidence() + " " + a.getContent());
  }

  /**
   * Goes through annotations and modifies annotation confidence based on heuristics and CMUDict
   * membership
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    if (DEBUG)
      System.out.println("CMU Dict Annotator!");
    // TODO (afandria): sourced CMUDict from
    // https://cmusphinx.svn.sourceforge.net/svnroot/cmusphinx/trunk/cmudict/cmudict.0.7a
    // maybe we should use the very opaque FreeTTS CMUDict import somehow
    Set<String> dictionary = getDictionary();
    // get annotations
    AnnotationIndex<Annotation> annotations = arg0.getAnnotationIndex(Gene.type);
    Iterator<Annotation> annotationIterator = annotations.iterator();
    while (annotationIterator.hasNext()) {
      Gene a = (Gene) annotationIterator.next();
      modifyConfidence(dictionary, a);
    }
  }
}
