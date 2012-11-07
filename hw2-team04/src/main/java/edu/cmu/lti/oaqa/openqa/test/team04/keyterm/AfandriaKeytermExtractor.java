package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class AfandriaKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
  }

  @Override
  protected List<Keyterm> getKeyterms(String arg0) {
    // TODO Auto-generated method stub
    Keyterm keyterm = new Keyterm("hello");
    List<Keyterm> list = new ArrayList<Keyterm>();
    list.add(keyterm);
    return list;
  }

}
