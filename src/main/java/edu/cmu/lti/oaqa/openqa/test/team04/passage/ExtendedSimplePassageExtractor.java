package edu.cmu.lti.oaqa.openqa.test.team04.passage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.KeytermListExtendor;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.finder.ExtendedSimplePassageCandidateFinder;

public class ExtendedSimplePassageExtractor extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    
    KeytermListExtendor myKLE = new  KeytermListExtendor(); 
    try {
      keyterms = myKLE.KeytermListExtendor(keyterms);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    if (keyterms.size() == 0 || documents.size() == 0)
      return new ArrayList<PassageCandidate>();

    ExtendedSimplePassageCandidateFinder finder;
    try {
      finder = new ExtendedSimplePassageCandidateFinder(keyterms, documents, wrapper);
      return finder.extractPassages();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
    }
  }