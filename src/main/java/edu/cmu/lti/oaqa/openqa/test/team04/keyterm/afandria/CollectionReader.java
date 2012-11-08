package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;
import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;

/**
 * @author afandria
 * 
 */
public class CollectionReader extends CollectionReader_ImplBase {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * Flag indicating whether or not we have read the singular input file. False if specified file
   * DNE.
   */
  private boolean read;

  /**
   * This is where our inputs lie (default)
   */
  private static String INPUT_PATH = "src/main/resources/data/hw1.in";

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
   */
  public void initialize() throws ResourceInitializationException {
    if (DEBUG)
      System.out.println("Initializing collection reader");
    read = false;
  }

  /**
   * Gets the only document in this case.
   * 
   * @param arg0
   *          CAS
   * @throws AnalysisEngineProcessException
   */
  @Override
  public void getNext(CAS arg0) throws IOException, CollectionException {
    JCas jcas;
    try {
      jcas = arg0.getJCas();

    } catch (CASException e) {
      throw new CollectionException();
    }
    // TODO get actual config
    String fp = (String) getConfigParameterValue("InputFilePath");
    if (fp == null)
      fp = INPUT_PATH;
    File f = new File(fp);
    jcas.setDocumentText(FileUtils.file2String(f, "UTF-8"));
    read = true;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
   */
  @Override
  public void close() throws IOException {
    if (DEBUG)
      System.out.println("Closing");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
   */
  @Override
  public Progress[] getProgress() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#hasNext()
   */
  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return !read;
  }

}
