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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.IdfIndexer;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.finder.Team04PassageCandidateFinder.PassageCandidateComparator;

public class SentencedPassageCandidateFinder {
  
  private RealMatrix p2dMatrix;
  
  private List<List<PassageSpan>> p2dMap;
  
  private List<RetrievalResult> documents;
  private List<String> keytermList;
  private List<String> docTexts;
  
  private SolrWrapper wrapper;
  private int numWords;
  private int numDocs;


  public SentencedPassageCandidateFinder(List<Keyterm> keyterms, List<RetrievalResult> documents, SolrWrapper wrapper) throws Exception {
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
    
    p2dMap = new ArrayList<List<PassageSpan>>();
    
    int docCnt = 0;
    for (RetrievalResult document : documents) {
      String key = document.getDocID();
      String text = getText(key);
      docTexts.add(text);
      
      // chunck doc into sentence
      final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
      final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
      final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY, SENTENCE_MODEL);
      
      Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
      java.util.Set<Chunk> sentences = chunking.chunkSet();
      
      // store the location of each passage in the document
  
        List<PassageSpan> spanList = new ArrayList<PassageSpan>();
        for (Chunk sentence : sentences) {
          int start = sentence.start();
          int end = sentence.end();
          
          System.out.println( "start:" + start  + "  end:" + end 
                  + "sentence:" + text.substring(start, end));
          
          spanList.add(new PassageSpan(start , end));
        }
        
        int keyId = 0; // keyterm ID
        for (String keyterm: keytermList) {
          // passage span?? a list of certain keyterm position (start&end)
          Pattern p = Pattern.compile(keyterm);
          Matcher m = p.matcher(text);
          
          data[keyId][docCnt] = (double)m.groupCount();
          keyId++;
        }
       // the same key of 2 para above belongs to same keyterm
        docCnt++;
        p2dMap.add(spanList);
     }
    RealMatrix dataCnt = new RealMatrixImpl(data);
    this.p2dMatrix = IdfIndexer.transform(dataCnt);
  }
  
  
  public List<PassageCandidate> extractPassages() {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    for (int i = 0; i < numDocs; i++) {
      String text = docTexts.get(i); // keyterm string
      int textSize = text.length(); // keyterm size

      // calculate every total matches and total keyterms in the whole document text
      double totalMatches = 0;
      int totalKeyterms = 0;
      
      List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
      for ( String keyterm : keytermList ) {
        Pattern p = Pattern.compile( keyterm );
        Matcher m = p.matcher( text );
        while ( m.find() ) {
          PassageSpan match = new PassageSpan( m.start() , m.end() ) ;
          matchedSpans.add( match );
          totalMatches++;
        }
        if (! matchedSpans.isEmpty() ) {
          totalKeyterms++;
        }
      }
     
      // for each possible candidate window/passage, calculate matches found and keyterm found
      List<PassageSpan> passages = p2dMap.get(i);
      for ( PassageSpan passage : passages ) {
          double matchesFound = 0;
          int keytermsFound = 0;
          
          String passageText = text.substring(passage.begin, passage.end);
          String cleanPassageText = Jsoup.parse(passageText).text().replaceAll("([\177-\377\0-\32]*)", "");
          
          int keyId = 0;
          for (String keyterm: keytermList) {
            boolean exist = false;
            Pattern p = Pattern.compile(keyterm);
            Matcher m = p.matcher(passageText);
            if (m.find()) {
              matchesFound += p2dMatrix.getEntry(keyId, i)*m.groupCount();
              exist=true;
            }
            if (exist) keytermsFound++;
            keyId++;
          }
          
          int windowSize = cleanPassageText.length();
          double offsetScore = ( (double)textSize - (double)passage.begin ) / (double)textSize;
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
            window = new PassageCandidate( documents.get(i).getDocID() , passage.begin , passage.end , (float) score , null );
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          if (window.getProbability() >=0.2) result.add( window );
         }
        }
    Collections.sort(result, new PassageCandidateComparator());
    List<PassageCandidate> newResult = new ArrayList<PassageCandidate>();
    
    double max = result.get(0).getProbability();
    double min = result.get(result.size() - 1).getProbability();
    for (PassageCandidate p: result) {
      if (p.getProbability() > 0.5 * min + 0.5 * max) {
        newResult.add(p);
      }
    }
    if (newResult.size() == 0) {
      return newResult;
    }
    
    System.out.println(newResult.size() + " " + newResult.get(newResult.size() - 1).getProbability() +"$%$%$%$" + newResult.get(0).getProbability());
    return newResult;
  }
  
  
  class PassageSpan {
    public int begin, end;
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
  class PassageCandidateComparator implements Comparator<PassageCandidate> {

    @Override
    public int compare(PassageCandidate o1, PassageCandidate o2) {
      // TODO Auto-generated method stub
      if (o1.getProbability() > o2.getProbability())
        return -1;
      if (o1.getProbability() == o2.getProbability())
        return 0;
      return 1;
    }
  }
  
  private String getText(String id) throws Exception {
    String text = "";
    try {
          String htmlText = wrapper.getDocText(id);
          text = htmlText;
        } catch (SolrServerException e) {
          e.printStackTrace();
        }
    return text;
  }
}
