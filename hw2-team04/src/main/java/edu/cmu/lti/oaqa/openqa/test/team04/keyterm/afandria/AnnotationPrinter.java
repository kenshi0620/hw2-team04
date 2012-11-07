package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.base_cpm.CasObjectProcessor;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceProcessException;

/**
 * Prints pipeline Gene annotations to the first of the following that work: config specified file,
 * default file OUTPUT_PATH, standard out.
 * 
 * @author afandria
 * 
 */
public class AnnotationPrinter extends CasConsumer_ImplBase implements CasObjectProcessor {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * This is where we will write our output (default)
   */
  private static String OUTPUT_PATH = "src/main/resources/data/hw1.out";

  /**
   * Prints pipeline Gene annotations to the first of the following that work: config specified
   * file, default file OUTPUT_PATH, standard out.
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void processCas(CAS arg0) throws ResourceProcessException {
    JCas jcas;
    try {
      jcas = arg0.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    // TODO get actual config
    String fp = (String) getConfigParameterValue("OutputFilePath");
    if (fp == null)
      fp = OUTPUT_PATH;
    Writer fw = new OutputStreamWriter(System.out);
    try {
      fw = new FileWriter(fp);
    } catch (IOException e) {
      e.printStackTrace();
      // TODO (afandria): is this necessary?
      fw = new OutputStreamWriter(System.out);
    }
    // get annotations
    AnnotationIndex<Annotation> annotations = jcas.getAnnotationIndex(Gene.type);
    Iterator<Annotation> annotationIterator = annotations.iterator();
    while (annotationIterator.hasNext()) {
      Gene g = (Gene) annotationIterator.next();
      if (g.getConfidence() >= AnnotationUtilities.THRESHOLD_SCORE) {
        String gString = AnnotationUtilities.geneToString(g, true);
        try {
          fw.write(gString);
          fw.flush();
        } catch (IOException e) {
          // be quiet
        }
        if (DEBUG)
          System.out.println(gString);
      }
    }
  }
}
