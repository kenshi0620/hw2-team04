package edu.cmu.lti.oaqa.openqa.test.team04.retrieval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.tartarus.martin.Stemmer;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.KeytermListExtendor;

public class Team04RetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
    String query = formulateQuery(questionText, keyterms);
    return retrieveDocuments(query);
  };

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
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
    List<Keyterm> shrunk = new ArrayList<Keyterm>();
    for (Keyterm keyterm : keyterms){
      if (keyterm.toString().equals("th"))
      {
        continue;
      }
      shrunk.add(keyterm);
    }
    List<Keyterm> extended = new ArrayList<Keyterm>();
    System.err.println("0: " + shrunk.toString());
    // wikipedia redirect
    try {
      extended = KeytermListExtendor.wikipediaRedirect(shrunk);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      extended = new ArrayList<Keyterm>();
    }
    System.err.println("1: " + extended.toString());
    // random website
    try {
      extended = KeytermListExtendor.KeytermListExtendor(shrunk);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      extended = new ArrayList<Keyterm>();
    }
    System.err.println("2: " + extended.toString());
    // add back originals
    for (Keyterm keyterm : shrunk) {
      extended.add(keyterm);
    }
    for (Keyterm keyterm : extended) {
      System.out.println(" TRANSFORM: " + keyterm.getText() + " --> " + stem(keyterm.getText()));
      // System.out.println(" TRANSFORM: " + keyterm.getText() + " --> "
      // + MorphaStemmer.stemToken(keyterm.getText()));
      result.append("\"" + stem(keyterm.getText()) + "*\" ");
    }
    result.append(questionText);
    String query = result.toString();
    System.out.println(" QUERY: " + query);
    return query;
  }

  private String stem(String s) {
    Stemmer stemmer = new Stemmer();
    stemmer.add(s.toCharArray(), s.length());
    stemmer.stem();
    return stemmer.toString();
  }

}
