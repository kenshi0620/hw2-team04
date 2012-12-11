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
import edu.cmu.lti.oaqa.openqa.test.team04.passage.finder.SentencedPassageCandidateFinder;

/**
 * Returns an Passage Candidates with their offset and passage text
 * Call SentencedPassageCandidateFinder to find passage candidates.
 * 
 */
public class SentencedPassageCandidateExtractor extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    
    if (keyterms.size() == 0 || documents.size() == 0)
      return new ArrayList<PassageCandidate>();

    SentencedPassageCandidateFinder finder;
    try {
      finder = new SentencedPassageCandidateFinder(keyterms, documents, wrapper);
      return finder.extractPassages();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
    }
  }