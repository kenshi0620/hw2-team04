package edu.cmu.lti.oaqa.openqa.test.team04.passage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.IdfIndexer;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.VectorGenerator;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.finder.Team04PassageCandidateFinder;

public class Team04PassageExtrator extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
	  if (keyterms.size() == 0 || documents.size() == 0)
		  return new ArrayList<PassageCandidate>();
	  
	  Team04PassageCandidateFinder finder;
	  try {
		  finder = new Team04PassageCandidateFinder(keyterms, documents, wrapper);
		  return finder.extractPassages();
	  } catch (Exception e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
	  return null;
	
	  
	  
  }

}
