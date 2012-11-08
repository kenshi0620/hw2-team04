package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;

/**
 * @author afandria
 *
 */
/**
 * @author afandria
 * 
 */
public class AfandriaKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * Debug flag to toggle print statements.
   */
  private static boolean DEBUG = false;

  /**
   * This is where HW1 entities are (default)
   */
  private String HW1_ENTITIES = "src/main/resources/hw1.entities";

  /**
   * This is where CMUDict is (default)
   */
  private String DICT_PATH = "src/main/resources/cmudict.0.7a.txt";

  /**
   * Where our history is stored (default)
   */
  private String HMM_MODEL_PATH = "src/main/resources/ne-en-bio-genetag.HmmChunker";

  /**
   * LingPipe chunker instance
   */
  private Chunker chunker;

  /**
   * CMUDict instance
   */
  private Set<String> cmuDict;

  /**
   * HW1 entities instance
   */
  private Set<String> history;

  /**
   * Retrieves a set of words found in CMUDict
   * 
   * @param path
   * @return Set of strings contained by history in path
   */
  private Set<String> getDictionary(String path) {
    Set<String> dictionary = new HashSet<String>();
    try {
      String[] lines = FileUtils.file2String(new File(path)).split("\\n");
      for (int i = 0; i < lines.length; i++) {
        if (lines[i].indexOf(";") != 0) {
          if (lines[i].indexOf(' ') < 0) {
            dictionary.add(lines[i]);
          } else {
            dictionary.add(lines[i].substring(0, lines[i].indexOf(" ")));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return dictionary;
  }

  /**
   * Gets parameterized paths and initializes LingPipe chunker, CMU dictionary, and HW1 history
   * 
   * @param c
   *          the UimaContext to grab param values from
   */
  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    // get variables
    HMM_MODEL_PATH = (String) c.getConfigParameterValue("lingpipe_model_path");
    DICT_PATH = (String) c.getConfigParameterValue("dict_path");
    HW1_ENTITIES = (String) c.getConfigParameterValue("history_path");
    // init LingPipe chunker
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(new File(HMM_MODEL_PATH));
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceInitializationException();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new ResourceInitializationException();
    }
    // init CMUDict
    cmuDict = getDictionary(DICT_PATH);
    // init history
    history = getDictionary(HW1_ENTITIES);
  }

  /**
   * Add terms from LingPipe's HMM Chunker
   * 
   * @param query
   *          the query to get stuff from
   * @param keyterms
   *          the keyterm list to add to
   * @return number of items added
   */
  private int addFromLingPipeChunker(String query, List<Keyterm> keyterms) {
    Set<Chunk> spans = chunker.chunk(query).chunkSet();
    Iterator<Chunk> spanIterator = spans.iterator();
    int count = 0;
    while (spanIterator.hasNext()) {
      Chunk c = spanIterator.next();
      // add everything to the index
      keyterms.add(new Keyterm(query.substring(c.start(), c.end())));
      count++;
    }
    return count;
  }

  /**
   * Add terms from history
   * 
   * @param keyterms
   *          list to add to
   * @return number of items added
   */
  private int addFromHw1History(String query, List<Keyterm> keyterms) {
    Set<String> existingKeyterms = new HashSet<String>();
    for (Keyterm keyterm : keyterms) {
      existingKeyterms.add(keyterm.getText());
    }
    int count = 0;
    for (String entry : history) {
      // TODO (afandria): adding spaces on either side
      if ((query.contains(" " + entry + " ")) && !existingKeyterms.contains(entry)) {
        keyterms.add(new Keyterm(entry));
        count++;
      }
    }
    return count;
  }

  /**
   * Filter by CMUDict
   * 
   * @param keyterms
   *          list of terms to filter
   * @return number of items removed
   */
  private int filterByCmuDict(List<Keyterm> keyterms) {
    Collection<Keyterm> exclusion = new HashSet<Keyterm>();
    for (Keyterm keyterm : keyterms) {
      String[] miniterms = keyterm.getText().split(" ");
      int count = 0;
      for (String miniterm : miniterms) {
        if (cmuDict.contains(miniterm)) {
          count++;
        }
      }
      if ((float) count >= (float) miniterms.length * (0.75)) {
        exclusion.add(keyterm);
      }
    }
    keyterms.removeAll(exclusion);
    return exclusion.size();
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
    addFromLingPipeChunker(arg0, keyterms);
    filterByCmuDict(keyterms);
    addFromHw1History(arg0, keyterms);
    return keyterms;
  }

}
