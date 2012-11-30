package edu.cmu.lti.oaqa.openqa.test.team04.passage.finder;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.IdfIndexer;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.finder.Team04PassageCandidateFinder.PassageSpan;

public class ExtendedSimplePassageCandidateFinder {
  private RealMatrix p2dMatrix;
  private List<Map<String, List<PassageSpan>>> p2dMap;
  private List<String> docTexts;
  private List<List<Integer>> leftEdgesDocs;
  private List<List<Integer>> rightEdgesDocs;
  private List<RetrievalResult> documents;
  private List<String> keytermList;
  private SolrWrapper wrapper;
  private int numWords;
  private int numDocs;


  public ExtendedSimplePassageCandidateFinder(List<Keyterm> keyterms, List<RetrievalResult> documents, SolrWrapper wrapper) throws Exception {
    this.wrapper = wrapper;
    
    keytermList = new ArrayList<String>();
    for (Keyterm keyterm: keyterms) {
      keytermList.add(keyterm.getText());
    }
    
    this.numDocs = documents.size();
    this.numWords = keyterms.size();
    
    double[][] data = new double[numWords][numDocs];
    docTexts = new ArrayList<String>();
    this.documents = documents;
    p2dMap = new ArrayList<Map<String, List<PassageSpan>>>();
    leftEdgesDocs = new ArrayList<List<Integer>>();
    rightEdgesDocs = new ArrayList<List<Integer>>();
    
    int docCnt = 0;
    for (RetrievalResult document : documents) {
      String key = document.getDocID();
      String text = getText(key);
  
      /*
       * get output the document to see the html files
      String OutputDocumentName = "/Users/zhangweijia/Documents/" + docCnt;
      File output = new File (OutputDocumentName);
      FileWriter fileWriter = new FileWriter(output);
      fileWriter.write(text);
      fileWriter.flush();
      */
  
      Map<Integer, Boolean> leftMap = new HashMap<Integer, Boolean>(); // all keywords start position
      Map<Integer, Boolean> rightMap = new HashMap<Integer, Boolean>(); // all keywords end position
      // the same key of the two map above belongs to the same keyword.
      
      docTexts.add(text);
      
      Map<String, List<PassageSpan>> keyMap = new HashMap<String, List<PassageSpan>>();
      // store the location of each passage in the document
      
      int keyId = 0; // keyterm ID
      for (String keyterm: keytermList) {
        List<PassageSpan> spanList = new ArrayList<PassageSpan>();
        // passage span?? a list of certain keyterm position (start&end)
        Pattern p = Pattern.compile(keyterm);
        Matcher m = p.matcher(text);
        while ( m.find() ) {
          leftMap.put(m.start(), true);
          rightMap.put(m.end(), true);
          spanList.add(new PassageSpan(m.start() , m.end())); // add every keyterm in spanlist
        }
        data[keyId][docCnt] = (double)spanList.size();
        keyMap.put(keyterm, spanList);
        keyId++;
      }
      
      leftEdgesDocs.add(new ArrayList<Integer>(leftMap.keySet())); // all keyterms start points in this document
      rightEdgesDocs.add(new ArrayList<Integer>(rightMap.keySet())); // all keyterms end points in this document
      // the same key of 2 para above belongs to same keyterm
      
      p2dMap.add(keyMap);// all keyterm and its position, no document info
      docCnt++;
    }
    RealMatrix dataCnt = new RealMatrixImpl(data);
    this.p2dMatrix = IdfIndexer.transform(dataCnt);
  }
  
  public List<PassageCandidate> extractPassages() {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    // For every possible window, calculate keyterms found, matches found; score window, and create passage candidate.
    for (int i = 0; i < numDocs; i++) {
      // create set of left edges and right edges which define possible windows.
      List<Integer> leftEdges = leftEdgesDocs.get(i); // keyterm start point
      List<Integer> rightEdges = rightEdgesDocs.get(i); // keytrm end point
      String text = docTexts.get(i); // keyterm string
      int textSize = text.length(); // keyterm size
      
      // calculate every total matches and total keyterms in the whole document text
      double totalMatches = 0;
      int totalKeyterms = 0;
      List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
      List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
      for ( String keyterm : keytermList ) {
        Pattern p = Pattern.compile( keyterm );
        Matcher m = p.matcher( text );
        while ( m.find() ) {
          PassageSpan match = new PassageSpan( m.start() , m.end() ) ;
          matchedSpans.add( match );
          totalMatches++;
        }
        if (! matchedSpans.isEmpty() ) {
          matchingSpans.add( matchedSpans );
          totalKeyterms++;
        }
      }
     
      // for each possible candidate window/passage, calculate matches found and keyterm found
      Map<String, List<PassageSpan>> spanMap = p2dMap.get(i);
      for ( Integer begin : leftEdges ) {
        for ( Integer end : rightEdges ) {
          if ( end <= begin ) continue;
          double matchesFound = 0;
          int keytermsFound = 0;
          
          String passageText = text.substring(begin, end);
          String cleanPassageText = Jsoup.parse(passageText).text().replaceAll("([\177-\377\0-\32]*)", "");
          
          int keyId = 0;
          for (String keyterm: keytermList) {
            List<PassageSpan> spans = spanMap.get(keyterm);
            boolean exist = false;
            for (PassageSpan span: spans) {
              if (span.containedIn(begin, end)) {
                matchesFound += p2dMatrix.getEntry(keyId, i);
                exist=true;
                }
            }
            if (exist) keytermsFound++;
            keyId++;
          }
          int windowSize = cleanPassageText.length();
          double offsetScore = ( (double)textSize - (double)begin ) / (double)textSize;
          double score = .25d *( (double)matchesFound / (double)totalMatches ) 
          + .25d * ( (double)keytermsFound / (double)totalKeyterms) 
          + .25d * ( 1 - ( (double)windowSize / (double)textSize ) + .25d * offsetScore );
          
          /*
           * get output the document to see the html files
          String OutputDocumentName = "/Users/zhangweijia/Documents/" + docCnt;
          File output = new File (OutputDocumentName);
          FileWriter fileWriter = new FileWriter(output);
          fileWriter.write(text);
          fileWriter.flush();
          */
          
          PassageCandidate window = null;
          try {
            window = new PassageCandidate( documents.get(i).getDocID() , begin , end , (float) score , null );
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          result.add( window );
         }
        }
      }
    return result;
  }
  
  
  class PassageSpan {
    private int begin, end;
    public PassageSpan( int begin , int end ) {
      this.begin = begin;
      this.end = end;
    }
    public boolean containedIn ( int begin , int end ) {
      if ( begin <= this.begin && end >= this.end ) {
        return true;
      } else {
        return false;
      }
    }
  }
  
  private String getText(String id) throws Exception {
    String text = "";
    try {
          String htmlText = wrapper.getDocText(id);

          // cleaning HTML text
          //text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
          // for now, making sure the text isn't too long
          
          // try to clean the text by starting from the first passage in the document
          Pattern p = Pattern.compile( "<P>" );
          Matcher m = p.matcher( text );
          if ( m.find() ) {
            text = text.substring(m.start());
          }
          
          
          text = htmlText;
          text = text.substring(0, Math.min(5000, text.length()));
        } catch (SolrServerException e) {
          e.printStackTrace();
        }
    return text;
  }
}
