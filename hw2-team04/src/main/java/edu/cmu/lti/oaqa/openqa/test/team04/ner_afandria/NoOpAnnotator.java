package edu.cmu.lti.oaqa.openqa.test.team04.ner_afandria;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * This annotator does absolutely nothing. Arguably this is fluff and is pointless, but it provides
 * a null baseline that runs basically invariably.
 * 
 * @author afandria
 */
public class NoOpAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * Does nothing!!!
   * 
   * @param arg0
   *          JCas
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void process(JCas arg0) throws AnalysisEngineProcessException {
    if (DEBUG)
      System.out.println("No op!");
  }

}
