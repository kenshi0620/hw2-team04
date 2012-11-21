package edu.cmu.lti.oaqa.openqa.test.team04.passage.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class WordTokenizer {

	private StringTokenizer tokenizer;
  
	public WordTokenizer(String text) {
		super();
		tokenizer = new StringTokenizer(text);
	}
  
	
	public boolean hasMoreTokens() {
		return tokenizer.hasMoreTokens();
	}
  
	public Token nextToken() throws Exception {
		return new Token(tokenizer.nextToken(), TokenType.WORD);
	}
}
