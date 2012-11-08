package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.austinma;

/* First created by JCasGen Tue Oct 16 13:27:04 PDT 2012 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** A Gene Annotation
 * Updated by JCasGen Tue Oct 16 13:27:10 PDT 2012
 * @generated */
public class GeneAnnotation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (GeneAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = GeneAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new GeneAnnotation(addr, GeneAnnotation_Type.this);
  			   GeneAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new GeneAnnotation(addr, GeneAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = GeneAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("GeneAnnotation");
 
  /** @generated */
  final Feature casFeat_SentenceID;
  /** @generated */
  final int     casFeatCode_SentenceID;
  /** @generated */ 
  public String getSentenceID(int addr) {
        if (featOkTst && casFeat_SentenceID == null)
      jcas.throwFeatMissing("SentenceID", "GeneAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_SentenceID);
  }
  /** @generated */    
  public void setSentenceID(int addr, String v) {
        if (featOkTst && casFeat_SentenceID == null)
      jcas.throwFeatMissing("SentenceID", "GeneAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_SentenceID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_TextSpan;
  /** @generated */
  final int     casFeatCode_TextSpan;
  /** @generated */ 
  public String getTextSpan(int addr) {
        if (featOkTst && casFeat_TextSpan == null)
      jcas.throwFeatMissing("TextSpan", "GeneAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_TextSpan);
  }
  /** @generated */    
  public void setTextSpan(int addr, String v) {
        if (featOkTst && casFeat_TextSpan == null)
      jcas.throwFeatMissing("TextSpan", "GeneAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_TextSpan, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public GeneAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_SentenceID = jcas.getRequiredFeatureDE(casType, "SentenceID", "uima.cas.String", featOkTst);
    casFeatCode_SentenceID  = (null == casFeat_SentenceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_SentenceID).getCode();

 
    casFeat_TextSpan = jcas.getRequiredFeatureDE(casType, "TextSpan", "uima.cas.String", featOkTst);
    casFeatCode_TextSpan  = (null == casFeat_TextSpan) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_TextSpan).getCode();

  }
}



    