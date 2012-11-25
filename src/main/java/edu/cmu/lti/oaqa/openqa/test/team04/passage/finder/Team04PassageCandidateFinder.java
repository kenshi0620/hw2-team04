package edu.cmu.lti.oaqa.openqa.test.team04.passage.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.*;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.test.team04.passage.basic.IdfIndexer;

public class Team04PassageCandidateFinder {
	
//	private String text;
//	private String docId;
//	private SortedSet<String> keyterms;
//	private HashMap<String, Integer> keyMap;
	
//	private KeytermWindowScorer scorer;
	
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
	
	
	public Team04PassageCandidateFinder(List<Keyterm> keyterms, List<RetrievalResult> documents, SolrWrapper wrapper) throws Exception {
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
			Map<Integer, Boolean> leftMap = new HashMap<Integer, Boolean>();
			Map<Integer, Boolean> rightMap = new HashMap<Integer, Boolean>();
			docTexts.add(text);
			
			Map<String, List<PassageSpan>> keyMap = new HashMap<String, List<PassageSpan>>();			
			int keyId = 0;
			for (String keyterm: keytermList) {
				List<PassageSpan> spanList = new ArrayList<PassageSpan>();
				Pattern p = Pattern.compile(keyterm);
				Matcher m = p.matcher(text);
				while ( m.find() ) {
					leftMap.put(m.start(), true);
					rightMap.put(m.end(), true);
					spanList.add(new PassageSpan(m.start() , m.end()));
				}
				data[keyId][docCnt] = (double)spanList.size();
				keyMap.put(keyterm, spanList);
				keyId++;
			}
			leftEdgesDocs.add(new ArrayList<Integer>(leftMap.keySet()));
			rightEdgesDocs.add(new ArrayList<Integer>(rightMap.keySet()));
			
			p2dMap.add(keyMap);
			docCnt++;
		}
		
		// idf matrix
		this.p2dMatrix = IdfIndexer.transform(new RealMatrixImpl(data));
	}
	
	public List<PassageCandidate> extractPassages() {
		List<PassageCandidate> result = new ArrayList<PassageCandidate>();
		
		// For every possible window, calculate keyterms found, matches found; score window, and create passage candidate.
		for (int i = 0; i < numDocs; i++) {
			// create set of left edges and right edges which define possible windows.
			List<Integer> leftEdges = leftEdgesDocs.get(i);
			List<Integer> rightEdges = rightEdgesDocs.get(i);
			String text = docTexts.get(i);
			Map<String, List<PassageSpan>> spanMap = p2dMap.get(i);
			
			for ( Integer begin : leftEdges ) {
				for ( Integer end : rightEdges ) {
					if ( end <= begin ) continue;
					
					double score = 0;
					int cnt = 0;
					
					
					String passageText = text.substring(begin, end);
					StringTokenizer tokenizer = new StringTokenizer(text);
					int wordCnt = tokenizer.countTokens();
					
					int keyId = 0;
					int type = 0;
					for (String keyterm: keytermList) {
						List<PassageSpan> spans = spanMap.get(keyterm);
						int exist = 0;
						for (PassageSpan span: spans) {
							if (span.containedIn(begin, end)) {
								cnt++;
								exist++;
							}
						}
						if (exist > 0) {
							score += p2dMatrix.getEntry(keyId, i);
							type++;
						}
						keyId++;
					}
//					if (score == 0) {
//						System.out.println(" begin:" + begin + " end: " + end + "%%%%%" + text.substring(begin, end));
//						keyId = 0;
//						for (String keyterm: keytermList) {
//							List<PassageSpan> spans = spanMap.get(keyterm);
//							for (PassageSpan span: spans) {
//								if (text.substring(begin, end).contains(keyterm)) {
//									System.out.println("@@##@@" + " begin:" + span.begin + " end: " + span.end + text.substring(span.begin, span.end));
//								}
//							}
//							keyId++;
//						}
//					}
					if (score != 0)
						// score = score * ((double)cnt / (double)(cnt * wordCnt));
						// score = score / (double)(wordCnt);
						score = score - ((double)type) * Math.log(end - begin + 1);
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
//		Collections.sort(result, new PassageCandidateComparator());
//		
//		System.out.println(result.size() + " " + result.get(result.size() - 1).getProbability() +"$%$%$%$" + result.get(0).getProbability());
//		if (result.size() <= 10)
//			return result;
//		
//		List<PassageCandidate> top10 = new ArrayList<PassageCandidate>();
//		for (int i = 0; i < 10; i++) {
//			top10.add(result.get(i));
//		}
//		return top10;
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

	        // cleaning HTML text
	        text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
	        // for now, making sure the text isn't too long
	        text = text.substring(0, Math.min(5000, text.length()));
	      } catch (SolrServerException e) {
	        e.printStackTrace();
	      }
		return text;
	}

}
