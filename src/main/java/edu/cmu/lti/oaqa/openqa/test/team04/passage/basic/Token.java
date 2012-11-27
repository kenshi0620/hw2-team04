package edu.cmu.lti.oaqa.openqa.test.team04.passage.basic;

public class Token {

	  public Token() {
	    super();
	  }
	  
	  public Token(String value, TokenType type) {
	    this();
	    setValue(value);
	    setType(type);
	  }
	  
	  private String value;
	  private TokenType type;
	  
	  public void setValue(String value) {
		  this.value = value;
	  }
	  
	  public void setType(TokenType type) {
		  this.type = type;
	  }
	  
	  public String getValue() {
		  return value;
	  }
	  
	  public TokenType getType() {
		  return type;
	  }
	 
	  @Override
	  public String toString() {
	    return value + " (" + type + ")";
	  }
	}
