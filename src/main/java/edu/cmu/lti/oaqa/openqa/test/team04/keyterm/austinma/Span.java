package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.austinma;

public class Span {
  public int start;
  public int end;
  
  public Span(int start, int end) {
    this.start = start;
    this.end = end;
  }
  
  public int hashCode() {
    int hash = 17;
    hash = hash * 37 + start;
    hash = hash * 37 + end;
    return hash;
  }
  
  public boolean equals(Object obj) {
    if(obj == null)
      return false;
    if(obj == this)
      return true;
    if(obj.getClass() != this.getClass())
      return false;
    
    Span other = (Span)obj;
    if(other.start != this.start)
      return false;
    if(other.end != this.end)
      return false;
    return true;
  }
}
