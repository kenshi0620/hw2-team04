package edu.cmu.lti.oaqa.openqa.test.team04.passage.basic;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;


//import com.mycompany.myapp.recognizers.AbbreviationRecognizer;
//import com.mycompany.myapp.recognizers.BoundaryRecognizer;
//import com.mycompany.myapp.recognizers.ContentWordRecognizer;
//import com.mycompany.myapp.recognizers.IRecognizer;
//import com.mycompany.myapp.recognizers.PhraseRecognizer;
//import com.mycompany.myapp.recognizers.RecognizerChain;
//import com.mycompany.myapp.recognizers.StopwordRecognizer;
//import com.mycompany.myapp.tokenizers.Token;
//import com.mycompany.myapp.tokenizers.TokenType;
//import com.mycompany.myapp.tokenizers.WordTokenizer;

/**
 * Generate the word occurence vector for a document collection.
 */
public class VectorGenerator {

	private HashMap<String, HashMap<String, Integer>> dataSource;
	private SolrWrapper wrapper;
  
	private Map<Integer,String> wordIdValueMap = 
			new HashMap<Integer,String>();
	private Map<Integer,String> documentIdNameMap = 
			new HashMap<Integer,String>();
	private TreeSet<String> keyterms;
	private int numWords;
	private RealMatrix matrix;

	@Required
	public void setDataSource(HashMap<String, HashMap<String, Integer>> dataSource, List<Keyterm> keyterms) {
		this.dataSource = dataSource;
		this.keyterms = new TreeSet<String>();
		for (int i = 0; i < keyterms.size(); i++) {
			this.keyterms.add(keyterms.get(i).getText());
		}
		this.numWords = keyterms.size();
	}
	
	@Required
	public void setWrapper(SolrWrapper wrapper) {
		this.wrapper = wrapper;
	}

	public void generateVector(List<RetrievalResult> documents) 
			throws Exception {
		Map<String,Bag> documentWordFrequencyMap = 
				new HashMap<String,Bag>();
		SortedSet<String> wordSet = new TreeSet<String>();
		Integer docId = 0;
		int numDocs = documents.size();
		double[][] data = new double[numWords][numDocs];
		for (RetrievalResult document : documents) {
			String key = document.getDocID();
			String text = getText(key);
			int keyId = 0;
			for (String keyterm: keyterms) {
				System.out.println("##" + keyterm);
				Pattern p = Pattern.compile(keyterm);
				Matcher m = p.matcher(text);
				double cnt = 0;
				while ( m.find() ) {
					cnt++;
				}
				data[keyId][docId] = cnt;
				keyId++;
			}
		}
		if (numWords == 0 || numDocs == 0)
			return;
		matrix = new RealMatrixImpl(data);
	}

	public RealMatrix getMatrix() {
		return matrix;
	}
  
	public String[] getDocumentNames() {
		String[] documentNames = new String[documentIdNameMap.keySet().size()];
		for (int i = 0; i < documentNames.length; i++) {
			documentNames[i] = documentIdNameMap.get(i);
		}
		return documentNames;
	}
  
	public String[] getWords() {
		String[] words = new String[wordIdValueMap.keySet().size()];
		for (int i = 0; i < words.length; i++) {
			String word = wordIdValueMap.get(i);
			if (word.contains("|||")) {
				// phrases are stored with length for other purposes, strip it off
				// for this report.
				word = word.substring(0, word.indexOf("|||"));
			}
			words[i] = word;
		}
		return words;
	}

	private Bag getWordFrequencies(String text) 
		throws Exception {
		Bag wordBag = new HashBag();
	    WordTokenizer wordTokenizer = new WordTokenizer(text);
	    List<Token> tokens = new ArrayList<Token>();
	    while (wordTokenizer.hasMoreTokens()) {
	      tokens.add(wordTokenizer.nextToken());
	    }
	    
	    PhraseRecognizer phraseRecognizer = new PhraseRecognizer(dataSource);
	    List<Token> recognizedTokens = phraseRecognizer.recognize(tokens);
	    for (Token recognizedToken: recognizedTokens) {
	    	wordBag.add(StringUtils.lowerCase(recognizedToken.getValue()));
	    }
    
//    RecognizerChain recognizerChain = new RecognizerChain(
//        Arrays.asList(new IRecognizer[] {
//        new BoundaryRecognizer(),
//        new AbbreviationRecognizer(dataSource),
//        new PhraseRecognizer(dataSource),
//        new StopwordRecognizer(),
//        new ContentWordRecognizer()
//    }));
//    recognizerChain.init();
//    List<String> recognizedTokens = recognizerChain.recognize(tokens);
//    for (Token recognizedToken : recognizedTokens) {
//      if (recognizedToken.getType() == TokenType.ABBREVIATION ||
//          recognizedToken.getType() == TokenType.PHRASE ||
//          recognizedToken.getType() == TokenType.CONTENT_WORD) {
//        // lowercase words to treat Human and human as the same word
//        wordBag.add(StringUtils.lowerCase(recognizedToken.getValue()));
//      }
//    }
	    return wordBag;
	}

	private String getText(String id) throws Exception {
		String text = "";
		try {
	        String htmlText = wrapper.getDocText(id);

	        // cleaning HTML text
	        text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
	        // for now, making sure the text isn't too long
	        text = text.substring(0, Math.min(5000, text.length()));
	        System.out.println(text);
	      } catch (SolrServerException e) {
	        e.printStackTrace();
	      }
		return text;
//		StringBuilder textBuilder = new StringBuilder();
//		char[] cbuf = new char[1024];
//		int len = 0;
//		while ((len = reader.read(cbuf, 0, 1024)) != -1) {
//			textBuilder.append(ArrayUtils.subarray(cbuf, 0, len));
//		}
//		reader.close();
//		return textBuilder.toString();
	}
}