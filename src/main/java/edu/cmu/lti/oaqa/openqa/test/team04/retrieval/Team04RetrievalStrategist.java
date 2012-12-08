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

public class Team04RetrievalStrategist extends AbstractRetrievalStrategist {

  private static boolean STEM = false;

  private static boolean OVERRIDE_KEYTERMS_WITH_NN = false;

  private static int MINIMUM_KEYTERM_LENGTH = 2;

  protected Integer hitListSize;

  protected Integer hitListFinal;

  protected SolrWrapper wrapper;

  protected PosTagNamedEntityRecognizer ner;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
      this.hitListFinal = (Integer) aContext.getConfigParameterValue("hit-list-final");
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

  @Override
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
    if (OVERRIDE_KEYTERMS_WITH_NN) {
      keyterms = getNNKeyterm(questionText);
    }
    String query = formulateQuery(questionText, keyterms);
    return filterDocuments(questionText, keyterms, retrieveDocuments(query), hitListFinal);
  };

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }

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
      System.out.println(" NNK: " + questionText.substring(me.getKey(), me.getValue()));
    }
    return keyterms;
  }

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

  private float looseMatchBoost(String docText, Pattern p) {
    boolean m = p.matcher(docText).matches();
    if (m) {
      System.out.println("GOT EM");
    }
    return (float) (m ? 1.5 : 1);
  }

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
    // sort raw
    Collections.sort(rawRetrievalResults, RetrievalResultComparator);
    for (RetrievalResult result : rawRetrievalResults) {
      RetrievalResult result2 = new RetrievalResult(result.getDocID(), result.getProbability(),
              questionText);
      // get text
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
      System.out.println(" FDOC: " + filteredResults.get(i).getDocID() + " ranked at "
              + filteredResults.get(i).getRank() + " or "
              + (Float) filteredResults.get(i).getProbability());
    }
    return filteredResults;
  }

  protected List<RetrievalResult> retrieveDocuments(String queryString) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      System.out.println(" QQ: " + queryString);
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
        System.out.println(" DOC: " + doc.getFieldValue("id") + " ranked at " + r.getRank()
                + " or " + (Float) doc.getFieldValue("score"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    // get only original keyterms
    List<Keyterm> shrunk = new ArrayList<Keyterm>();
    for (Keyterm keyterm : keyterms) {
      shrunk.add(keyterm);
    }
    keyterms = new ArrayList<Keyterm>();
    List<Keyterm> extended = new ArrayList<Keyterm>();
    System.err.println("0: " + shrunk.toString());
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
    System.err.println("1: " + extended.toString());
    // random website
    try {
      extended = KeytermListExtendor.KeytermListExtendor(shrunk);
    } catch (IOException e) {
      e.printStackTrace();
      extended = new ArrayList<Keyterm>();
    }
    for (Keyterm keyterm : extended) {
      keyterms.add(keyterm);
    }
    System.err.println("2: " + extended.toString());
    // add back originals
    for (Keyterm keyterm : shrunk) {
      keyterms.add(keyterm);
    }
    // filter uniques only
    Set<Keyterm> uniques = new HashSet<Keyterm>(Arrays.asList(keyterms.toArray()));
    // remove keyterms of only two letters (except those like L2)
    Pattern twoPattern = Pattern.compile("[A-Z][0-9]");
    for (Keyterm keyterm : uniques) {
      if (keyterm.getText().length() > MINIMUM_KEYTERM_LENGTH
              || twoPattern.matcher(keyterm.getText()).matches()) {
        if (STEM) {
          System.out
                  .println(" TRANSFORM: " + keyterm.getText() + " --> " + stem(keyterm.getText()));
          result.append("\"" + stem(keyterm.getText()) + "*\" ");
          // System.out.println(" TRANSFORM: " + keyterm.getText() + " --> "
          // + MorphaStemmer.stemToken(keyterm.getText()));
        } else {
          result.append("\"" + keyterm.getText() + "\" ");
        }
      }
    }
    // result.append(getNN(questionText, " "));
    result.append(questionText);
    String query = result.toString();
    System.out.println(" QUERY: " + query);
    return query;
  }

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
