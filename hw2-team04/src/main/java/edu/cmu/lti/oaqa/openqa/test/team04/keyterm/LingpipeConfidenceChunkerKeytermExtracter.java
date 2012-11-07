package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;


public class LingpipeConfidenceChunkerKeytermExtracter extends AbstractKeytermExtractor{
  
  public static final String PARAM_MODELFILE = "model_file";
  private ConfidenceChunker chunker = null;
  
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    if (chunker == null) {
      File modelFile = new File(((String) aContext.getConfigParameterValue(PARAM_MODELFILE)).trim());
      try {
        chunker = (ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    
    char[] quest = question.toCharArray();
    Iterator<Chunk> it = chunker.nBestChunks(quest, 0, quest.length, 5);
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    
    while (it.hasNext()) {
      Chunk chunk = it.next();
      String keyterm = question.substring(chunk.start(), chunk.end());
      double confidence = Math.pow(2.0, chunk.score());
      if (confidence >= 0.8) keyterms.add(new Keyterm(keyterm));
      else if (confidence < 0.8 && confidence >= 0.6) {
        try {
          if (isKeyterm(keyterm)) keyterms.add(new Keyterm(keyterm));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return keyterms;
  }
  
  private boolean isKeyterm ( String geneName ) throws IOException {
    
    String newGeneName = changeWhiteSpaceTo20percent(geneName);   
    String requestURL = "http://bergmanlab.smith.man.ac.uk:8081/?text="+newGeneName;
  
    URL url = new URL(requestURL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    InputStream in = url.openStream();
    BufferedReader bin = new BufferedReader(new InputStreamReader(in, "GB2312"));
    String response = bin.readLine();
    
    if (response != null)   return true;
    else return false;
  }
  
  private String changeWhiteSpaceTo20percent (String s) {
    String result = new String();
    String[] list = new String[20];
    
    list = s.split(" ");
    for (String w: list) {
      result = result + "%20" + w;
    }
    result = result.substring(3);
    return result;
  }
}
