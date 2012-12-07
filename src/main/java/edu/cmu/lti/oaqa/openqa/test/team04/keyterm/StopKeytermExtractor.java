package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * @author afandria
 *
 */
/**
 * @author afandria
 * 
 */
public class StopKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  private Set<String> stopList;

  /**
   * Gets parameterized paths and initializes LingPipe chunker, CMU dictionary, and HW1 history
   * 
   * @param c
   *          the UimaContext to grab param values from
   */
  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    stopList = new HashSet<String>();
    try {
      File stopFile = new File("src/main/resources/stoplist.txt");
      BufferedReader reader = new BufferedReader(new FileReader(stopFile));
      String line;
      try {
        while ((line = reader.readLine()) != null)
          stopList.add(line.replace("(", "").replace(")", "").replace("?", "").replace(".", "").replace(",", "").toLowerCase().trim());
        reader.close();
      } catch (IOException e) {
        System.err.println("IOException in reading stoplist");
        throw new ResourceInitializationException("Unable to load stoplist", "load_stoplist_error",
                new Object[] {}, e);
      }
    } catch (FileNotFoundException e) {
      System.err.println("FileNotFoundException in reading stoplist");
      throw new ResourceInitializationException("Unable to find stoplist", "load_stoplist_error",
              new Object[] {}, e);
    }
    super.initialize(c);
  }

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
    for (String s : arg0.split(" ")) {
      s = s.replace("(", "").replace(")", "").replace("?", "").replace(".", "").replace(",", "").toLowerCase().trim();
      if (!stopList.contains(s)) {
        keyterms.add(new Keyterm(s));
      }
    }
    return keyterms;
  }

}
