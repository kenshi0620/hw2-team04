package edu.cmu.lti.oaqa.openqa.test.team04.retrieval;

import java.util.ArrayList;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.tartarus.martin.Stemmer;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.retrieval.SimpleSolrRetrievalStrategist;

public class Team04RetrievalStrategist extends SimpleSolrRetrievalStrategist {

  protected List<RetrievalResult> retrieveDocuments(String queryString) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrQuery query = new SolrQuery();
      query.setQuery(wrapper.escapeQuery(queryString));
      query.setRows(hitListSize);
      query.setFields("*", "score");
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);

      for (SolrDocument doc : docs) {

        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), queryString);
        result.add(r);
        System.out.println(" SUP DOC: " + doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }
  
  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    for (Keyterm keyterm : keyterms) {
      result.append(keyterm.getText() + " ");
      System.out.println(" TRANSFORM: " + keyterm.getText() + " --> " + stem(keyterm.getText()));
    }
    String query = result.toString();
    System.out.println(" QUEREAL: " + query);
    return query;
  }
  
  private String stem(String s)
  {
    Stemmer stemmer = new Stemmer();
    stemmer.add(s.toCharArray(), s.length());
    stemmer.stem();
    return stemmer.toString();
  }

}
