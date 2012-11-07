package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.austinma;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

public class GeneAnnotation extends Annotation {
  public final static int typeIndexID = JCasRegistry.register(GeneAnnotation.class);
  public final static int type = typeIndexID;

  public int getTypeIndexID() {
    return typeIndexID;
  }

  protected GeneAnnotation() {
  }

  public GeneAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  public GeneAnnotation(JCas jcas) {
    super(jcas);
    readObject();
  }

  public GeneAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }

  private void readObject() {
  }
  //*--------------*
  //* Feature: SentenceID

  /** getter for SentenceID - gets The ID of the sentence from which this annotation was extracted
   * @generated */
  public String getSentenceID() {
    if (GeneAnnotation_Type.featOkTst && ((GeneAnnotation_Type)jcasType).casFeat_SentenceID == null)
      jcasType.jcas.throwFeatMissing("SentenceID", "GeneAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneAnnotation_Type)jcasType).casFeatCode_SentenceID);}
    
  /** setter for SentenceID - sets The ID of the sentence from which this annotation was extracted 
   * @generated */
  public void setSentenceID(String v) {
    if (GeneAnnotation_Type.featOkTst && ((GeneAnnotation_Type)jcasType).casFeat_SentenceID == null)
      jcasType.jcas.throwFeatMissing("SentenceID", "GeneAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneAnnotation_Type)jcasType).casFeatCode_SentenceID, v);}    
   
    
  //*--------------*
  //* Feature: TextSpan

  /** getter for TextSpan - gets The text span coresponding to this annotation

   * @generated */
  public String getTextSpan() {
    if (GeneAnnotation_Type.featOkTst && ((GeneAnnotation_Type)jcasType).casFeat_TextSpan == null)
      jcasType.jcas.throwFeatMissing("TextSpan", "GeneAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GeneAnnotation_Type)jcasType).casFeatCode_TextSpan);}
    
  /** setter for TextSpan - sets The text span coresponding to this annotation
 
   * @generated */
  public void setTextSpan(String v) {
    if (GeneAnnotation_Type.featOkTst && ((GeneAnnotation_Type)jcasType).casFeat_TextSpan == null)
      jcasType.jcas.throwFeatMissing("TextSpan", "GeneAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((GeneAnnotation_Type)jcasType).casFeatCode_TextSpan, v);}    
  }
