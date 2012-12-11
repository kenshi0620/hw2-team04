package edu.cmu.lti.oaqa.openqa.test.team04.retrieval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.tartarus.martin.Stemmer;

import scala.actors.threadpool.Arrays;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria.AnnotationUtilities;
import edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria.PosTagNamedEntityRecognizer;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.KeytermListExtendor;

/**
 * @author afandria
 * 
 *         Team 04's Document Retrieval Strategist, written by afandria. We do query expansion by
 *         finding synonyms of keyterms using Wikipedia redirect (courtesy of Yibin Lin's group) and
 *         Big Huge Thesaurus. We also support stemming, wilcarding, overriding, and grouping query
 *         terms. There are more details below. To reduce load on the passage extractor, we do
 *         pre-passage extraction filtering of documents using a loose match probability
 *         enhancement.
 */
public class Team04RetrievalStrategist extends AbstractRetrievalStrategist {

  /**
   * The factor to boost a document's probability by if a loose match occurs. It doesn't matter if
   * document_prob * (LOOSE_MATCH_BOOST ^ n) > 1.0
   */
  private static final double LOOSE_MATCH_BOOST = 1.5;

  /**
   * Stem keyterms if this flag is set
   */
  private static boolean STEM = false;

  /**
   * Override keyterms with POS tagged entities if this flag is set
   */
  private static boolean OVERRIDE_KEYTERMS_WITH_NN = false;

  /**
   * Use wildcards on the ends of keyterms if this flag is set
   */
  protected static boolean ASTERISK = false;

  /**
   * Minimum length for a single keyterm (excludes stuff like AS, but will check for things like L2)
   */
  private static int MINIMUM_KEYTERM_LENGTH = 2;

  /**
   * How many documents to retrieve from SOLR
   */
  protected Integer hitListSize;

  /**
   * How many documents to return to passage extractor
   */
  protected Integer hitListFinal;

  /**
   * This is the wrapper for SOLR
   */
  protected SolrWrapper wrapper;

  /**
   * This is our NER
   */
  protected PosTagNamedEntityRecognizer ner;

  /*
   * (non-Javadoc)
   * 
   * @see edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent#initialize(org.apache.uima.UimaContext)
   */
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
      this.hitListFinal = (Integer) aContext.getConfigParameterValue("hit-list-final");
      this.ASTERISK = (Boolean) aContext.getConfigParameterValue("asterisk");
      this.STEM = (Boolean) aContext.getConfigParameterValue("stem");
      this.OVERRIDE_KEYTERMS_WITH_NN = (Boolean) aContext.getConfigParameterValue("override");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
      this.hitListFinal = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-final"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
      this.ner = new PosTagNamedEntityRecognizer();
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * This function will return matching documents based on keyterms and the question.
   * 
   * @param questionText
   *          question text from user
   * @param keyterms
   *          keyterms from keyterm extractor
   * @return list of retrieval results
   */
  @Override
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
    // this ignores keyterms parameter
    if (OVERRIDE_KEYTERMS_WITH_NN) {
      keyterms = getNNKeyterm(questionText);
    }
    // first get the SOLR query string
    String query = formulateQuery(questionText, keyterms);
    // then filter the documents and return only 'hitListFinal' amount of documents
    return filterDocuments(questionText, keyterms, retrieveDocuments(query), hitListFinal);
  };

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.analysis_component.AnalysisComponent_ImplBase#collectionProcessComplete()
   */
  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }

  /**
   * This compares retrieval results and returns a rank order
   */
  private static Comparator<RetrievalResult> RetrievalResultComparator = new Comparator<RetrievalResult>() {
    public int compare(RetrievalResult x, RetrievalResult y) {
      if (x.getProbability() < y.getProbability()) {
        return 1;
      } else if (x.getProbability() == y.getProbability()) {
        return 0;
      } else {
        return -1;
      }
    }
  };

  /**
   * This compares text spans based on its start position. Ties are not broken.
   */
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
   * This function uses the NER to get POS tagged entities in our question text. This is meant to
   * override the keyterm extractor in hopes of better performance.
   * 
   * @param questionText
   *          question text from user
   * @return a list of POS tagged keyterms
   */
  private List<Keyterm> getNNKeyterm(String questionText) {
    // initialize list
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    Set<Map.Entry<Integer, Integer>> spans = ner.getGeneSpans(questionText).entrySet();
    List<Map.Entry<Integer, Integer>> spanList = new ArrayList<Map.Entry<Integer, Integer>>(spans);
    Collections.sort(spanList, SpanComparator);
    Iterator<Map.Entry<Integer, Integer>> spanIterator = spanList.iterator();
    while (spanIterator.hasNext()) {
      Map.Entry<Integer, Integer> me = spanIterator.next();
      keyterms.add(new Keyterm(questionText.substring(me.getKey(), me.getValue())));
      System.out.println(" NNKeyterm: " + questionText.substring(me.getKey(), me.getValue()));
    }
    return keyterms;
  }

  /**
   * This function returns a string of NNs from a delim-delimited question text
   * 
   * @param questionText
   *          question text from user that is delim-delimited
   * @param delim
   *          what is the delimiter between terms?
   * @return a string of just NNs
   */
  private String getNN(String questionText, String delim) {
    String nns = "";
    Set<Map.Entry<Integer, Integer>> spans = ner.getGeneSpans(questionText).entrySet();
    List<Map.Entry<Integer, Integer>> spanList = new ArrayList<Map.Entry<Integer, Integer>>(spans);
    Collections.sort(spanList, SpanComparator);
    Iterator<Map.Entry<Integer, Integer>> spanIterator = spanList.iterator();
    while (spanIterator.hasNext()) {
      Map.Entry<Integer, Integer> me = spanIterator.next();
      nns += questionText.substring(me.getKey(), me.getValue()) + delim;
      System.out.println(" NN: " + questionText.substring(me.getKey(), me.getValue()));
    }
    return nns.trim();
  }

  /**
   * @param docText
   *          document text to match against
   * @param p
   *          what pattern to match to the document
   * @return a multiplier >1 if there is a match
   */
  private float looseMatchBoost(String docText, Pattern p) {
    boolean m = p.matcher(docText).matches();
    return (float) (m ? LOOSE_MATCH_BOOST : 1);
  }

  /**
   * This takes the raw documents from a SOLR query and filters it using loose matches and produces
   * a new rank of a subset of documents accordingly.
   * 
   * @param questionText
   *          question text from user
   * @param keyterms
   *          keyterms from keyterm extractor
   * @param rawRetrievalResults
   *          a set of documents from SOLR (unfiltered) and of magnitude 'hitListSize'
   * @param maxReturn
   *          how many documents to return -- this must be less than 'hitListSize'
   * @return
   */
  private List<RetrievalResult> filterDocuments(String questionText, List<Keyterm> keyterms,
          List<RetrievalResult> rawRetrievalResults, int maxReturn) {
    List<RetrievalResult> filteredResults = new ArrayList<RetrievalResult>();
    List<Pattern> boosterPatterns = new ArrayList<Pattern>();
    // get a loose match of question text (hard)
    boosterPatterns.add(Pattern.compile(".*" + questionText.replace(" ", " .* ") + ".*"));
    // get a loose match of noun phrases (medium)
    boosterPatterns.add(Pattern.compile(".*" + getNN(questionText, "!!!").replace("!!!", " .* ")
            + ".*"));
    // get a loose match of keyterms (easier)
    if (keyterms.size() > 1) {
      String keytermRegex = ".*";
      for (Keyterm k : keyterms) {
        keytermRegex += k.getText() + " .* ";
      }
      boosterPatterns.add(Pattern.compile(keytermRegex.trim()));
    }
    // sort raw documents
    Collections.sort(rawRetrievalResults, RetrievalResultComparator);
    for (RetrievalResult result : rawRetrievalResults) {
      RetrievalResult result2 = new RetrievalResult(result.getDocID(), result.getProbability(),
              questionText);
      // get document text from SOLR
      try {
        String docText = wrapper.getDocText(result2.getDocID());
        for (Pattern p : boosterPatterns) {
          result2.setProbablity(looseMatchBoost(docText, p) * result2.getProbability());
        }
        filteredResults.add(result2);
        // match
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    // check filters
    Collections.sort(filteredResults, RetrievalResultComparator);
    filteredResults = filteredResults.subList(0, maxReturn);
    for (int i = 0; i < filteredResults.size(); i++) {
      filteredResults.get(i).setRank(i);
      System.out.println(" FilteredDocument: " + filteredResults.get(i).getDocID() + " ranked at "
              + filteredResults.get(i).getRank() + " or "
              + (Float) filteredResults.get(i).getProbability());
    }
    return filteredResults;
  }

  /**
   * Retrieve the raw set of documents from SOLR, using 'hitListSize' as how many we get.
   * 
   * @param queryString
   *          the query string that we had formulated
   * @return that list of documents
   */
  protected List<RetrievalResult> retrieveDocuments(String queryString) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      System.out.println(" Query: " + queryString);
      SolrQuery query = new SolrQuery();
      query.setQuery(wrapper.escapeQuery(queryString));
      query.setRows(hitListSize);
      query.setFields("*", "score");
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      int i = 0;
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), queryString);
        r.setRank(i++);
        result.add(r);
        System.out.println(" Document: " + doc.getFieldValue("id") + " ranked at " + r.getRank()
                + " or " + (Float) doc.getFieldValue("score"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  /**
   * Generate a query given the question text and keyterms from the keyterm extractor. This will
   * call wikipedia redirect and big huge thesaurus to expand the keyterms. This will add asterisks
   * if parameterized and stem if parameterized as well.
   * 
   * @param questionText
   *          question text from user
   * @param keyterms
   *          keyterms from keyterm extractor
   * @return
   */
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    // get only original keyterms
    List<Keyterm> shrunk = new ArrayList<Keyterm>();
    for (Keyterm keyterm : keyterms) {
      shrunk.add(keyterm);
    }
    keyterms = new ArrayList<Keyterm>();
    List<Keyterm> extended = new ArrayList<Keyterm>();
    System.out.println(" Formulate phase 0: " + shrunk.toString());
    // wikipedia redirect
    try {
      extended = KeytermListExtendor.wikipediaRedirect(shrunk);
    } catch (IOException e) {
      e.printStackTrace();
      extended = new ArrayList<Keyterm>();
    }
    for (Keyterm keyterm : extended) {
      keyterms.add(keyterm);
    }
    System.out.println(" Formulate phase 1: " + extended.toString());
    // random website (big huge thesaurus)
    try {
      extended = KeytermListExtendor.KeytermListExtendor(shrunk);
    } catch (IOException e) {
      e.printStackTrace();
      extended = new ArrayList<Keyterm>();
    }
    for (Keyterm keyterm : extended) {
      keyterms.add(keyterm);
    }
    System.out.println(" Formulate phase 2: " + extended.toString());
    // add back originals
    for (Keyterm keyterm : shrunk) {
      keyterms.add(keyterm);
    }
    // filter uniques only
    @SuppressWarnings("unchecked")
    Set<Keyterm> uniques = new HashSet<Keyterm>(Arrays.asList(keyterms.toArray()));
    // remove keyterms of only two letters (except those like L2)
    Pattern twoPattern = Pattern.compile("[A-Z][0-9]");
    for (Keyterm keyterm : uniques) {
      if (keyterm.getText().length() > MINIMUM_KEYTERM_LENGTH
              || twoPattern.matcher(keyterm.getText()).matches()) {
        // stemming always invokes wildcard
        if (STEM) {
          System.out.println(" Stemming: " + keyterm.getText() + " --> " + stem(keyterm.getText()));
          result.append("\"" + stem(keyterm.getText()) + "*\" ");
        } else {
          // are we wildcarding?
          if (ASTERISK) {
            result.append("\"" + keyterm.getText() + "*\" ");
          } else {
            result.append("\"" + keyterm.getText() + "\" ");
          }
        }
      }
    }
    // finally append the original question text, because this sadly improves performance a TON
    result.append(questionText);
    // prepare query
    String query = result.toString();
    System.out.println(" Formulated query: " + query);
    return query;
  }

  /**
   * Calls the Porter stemmer on strings of at least 'MINIMUM_KEYTERM_LENGTH'
   * 
   * @param s
   *          string to stem
   * @return the stemmed string
   */
  private String stem(String s) {
    if (s.length() > MINIMUM_KEYTERM_LENGTH) {
      Stemmer stemmer = new Stemmer();
      stemmer.add(s.toCharArray(), s.length());
      stemmer.stem();
      return stemmer.toString();
    } else {
      return s;
    }
  }
}
