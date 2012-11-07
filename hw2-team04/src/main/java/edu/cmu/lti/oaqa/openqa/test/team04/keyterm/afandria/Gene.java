package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.afandria;


/* First created by JCasGen Sun Oct 14 23:34:49 EDT 2012 */

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Oct 16 20:52:51 EDT 2012
 * XML source: C:/scratch/eclipse_workspace/hw1-afandria/src/main/resources/GeneAnnotationTypeSystemDescriptor.xml
 * @generated */
public class Gene extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Gene.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Gene() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Gene(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Gene(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Gene(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: content

  /** getter for content - gets 
   * @generated */
  public String getContent() {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "Gene");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Gene_Type)jcasType).casFeatCode_content);}
    
  /** setter for content - sets  
   * @generated */
  public void setContent(String v) {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "Gene");
    jcasType.ll_cas.ll_setStringValue(addr, ((Gene_Type)jcasType).casFeatCode_content, v);}    
   
    
  //*--------------*
  //* Feature: identifier

  /** getter for identifier - gets 
   * @generated */
  public String getIdentifier() {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "Gene");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Gene_Type)jcasType).casFeatCode_identifier);}
    
  /** setter for identifier - sets  
   * @generated */
  public void setIdentifier(String v) {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "Gene");
    jcasType.ll_cas.ll_setStringValue(addr, ((Gene_Type)jcasType).casFeatCode_identifier, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets use this to prune out some genes
   * @generated */
  public int getConfidence() {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "Gene");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Gene_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets use this to prune out some genes 
   * @generated */
  public void setConfidence(int v) {
    if (Gene_Type.featOkTst && ((Gene_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "Gene");
    jcasType.ll_cas.ll_setIntValue(addr, ((Gene_Type)jcasType).casFeatCode_confidence, v);}    
  }

    