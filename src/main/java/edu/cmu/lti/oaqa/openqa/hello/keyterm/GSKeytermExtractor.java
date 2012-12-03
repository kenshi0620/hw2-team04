package edu.cmu.lti.oaqa.openqa.hello.keyterm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * 
 * @author Xiaobo Huang
 */
public class GSKeytermExtractor extends AbstractKeytermExtractor {

  String ID = null;

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> keytermList = new ArrayList<Keyterm>();

    ID = question.substring(0, 3);

    if (question.equals("What is the role of PrnP in mad cow disease?")) {
      keytermList.add(new Keyterm("PrnP"));
      keytermList.add(new Keyterm("mad cow disease"));
    }

    else if (question.equals("What is the role of IDE in Alzheimer's disease")) {
      keytermList.add(new Keyterm("IDE"));
      keytermList.add(new Keyterm("Alzheimer's disease"));
    }

    else if (question.equals("What is the role of MMS2 in cancer?")) {
      keytermList.add(new Keyterm("MMS2"));
      keytermList.add(new Keyterm("cancer"));
    }

    else if (question
            .equals("What is the role of APC (adenomatous polyposis coli) in colon cancer?")) {
      keytermList.add(new Keyterm("APC"));
      keytermList.add(new Keyterm("adenomatous polyposis coli"));
      keytermList.add(new Keyterm("colon cancer"));
    }

    else if (question.equals("What is the role of Nurr-77 in Parkinson's disease?")) {
      keytermList.add(new Keyterm("Nurr-77"));
      keytermList.add(new Keyterm("Parkinson's disease"));
    }

    else if (question
            .equals("How do Cathepsin D (CTSD) and apolipoprotein E (ApoE) interactions contribute to Alzheimer's disease?")) {
      keytermList.add(new Keyterm("CTSD"));
      keytermList.add(new Keyterm("apolipoprotein E"));
      keytermList.add(new Keyterm("ApoE"));
      keytermList.add(new Keyterm("interactions"));
      keytermList.add(new Keyterm("contribute"));
      keytermList.add(new Keyterm("Alzheimer's disease"));
    }

    else if (question
            .equals("What is the role of Transforming growth factor-beta1 (TGF-beta1) in cerebral amyloid angiopathy (CAA)?")) {
      keytermList.add(new Keyterm("Transforming growth factor-beta1"));
      keytermList.add(new Keyterm("TGF-beta1"));
      keytermList.add(new Keyterm("cerebral amyloid angiopathy"));
      keytermList.add(new Keyterm("CAA"));
    }

    else if (question
            .equals("How does nucleoside diphosphate kinase (NM23) contribute to tumor progression?")) {
      keytermList.add(new Keyterm("nucleoside diphosphate kinase"));
      keytermList.add(new Keyterm("NM23"));
      keytermList.add(new Keyterm("contribute"));
      keytermList.add(new Keyterm("tumor progression"));
      keytermList.add(new Keyterm("tumor"));
      keytermList.add(new Keyterm("progression"));
    }

    else if (question.equals("How does BARD1 regulate BRCA1 activity?")) {
      keytermList.add(new Keyterm("BARD1"));
      keytermList.add(new Keyterm("regulate"));
      keytermList.add(new Keyterm("BRCA1"));
    }

    else if (question
            .equals("How does APC (adenomatous polyposis coli) protein affect actin assembly")) {
      keytermList.add(new Keyterm("APC"));
      keytermList.add(new Keyterm("adenomatous polyposis coli"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("actin assembly"));
      keytermList.add(new Keyterm("actin"));
      keytermList.add(new Keyterm("assembly"));
    }

    else if (question
            .equals("How does COP2 contribute to CFTR export from the endoplasmic reticulum?")) {
      keytermList.add(new Keyterm("COP2"));
      keytermList.add(new Keyterm("contribute"));
      keytermList.add(new Keyterm("CFTR export"));
      keytermList.add(new Keyterm("CFTR"));
      keytermList.add(new Keyterm("export"));
      keytermList.add(new Keyterm("endoplasmic reticulum"));
    }

    else if (question
            .equals("How does Nurr-77 delete T cells before they migrate to the spleen or lymph nodes and how does this impact autoimmunity?")) {
      keytermList.add(new Keyterm("Nurr-77"));
      keytermList.add(new Keyterm("delete"));
      keytermList.add(new Keyterm("T cells"));
      keytermList.add(new Keyterm("migrate"));
      keytermList.add(new Keyterm("spleen"));
      keytermList.add(new Keyterm("lymph nodes"));
      keytermList.add(new Keyterm("impact"));
      keytermList.add(new Keyterm("autoimmunity"));
    }

    else if (question.equals("How does p53 affect apoptosis?")) {
      keytermList.add(new Keyterm("p53"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("apoptosis"));
    }

    else if (question
            .equals("How do alpha7 nicotinic receptor subunits affect ethanol metabolism?")) {
      keytermList.add(new Keyterm("alpha7 nicotinic receptor subunits"));
      keytermList.add(new Keyterm("alpha7 nicotinic receptor"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("ethanol metabolism"));
      keytermList.add(new Keyterm("ethanol"));
      keytermList.add(new Keyterm("metabolism"));
    }

    else if (question.equals("How does BRCA1 ubiquitinating activity contribute to cancer?")) {
      keytermList.add(new Keyterm("BRCA1 ubiquitinating"));
      keytermList.add(new Keyterm("BRCA1"));
      keytermList.add(new Keyterm("ubiquitinating"));
      keytermList.add(new Keyterm("contribute"));
      keytermList.add(new Keyterm("cancer"));
    }

    else if (question.equals("How does L2 interact with L1 to form HPV11 viral capsids?")) {
      keytermList.add(new Keyterm("L2"));
      keytermList.add(new Keyterm("interact"));
      keytermList.add(new Keyterm("L1"));
      keytermList.add(new Keyterm("form"));
      keytermList.add(new Keyterm("HPV11 viral capsids"));
      keytermList.add(new Keyterm("HPV11"));
      keytermList.add(new Keyterm("viral capsids"));
    }

    else if (question
            .equals("How does Sec61-mediated CFTR degradation contribute to cystic fibrosis?")) {
      keytermList.add(new Keyterm("Sec61-mediated CFTR degradation"));
      keytermList.add(new Keyterm("Sec61-mediated"));
      keytermList.add(new Keyterm("Sec61"));
      keytermList.add(new Keyterm("mediated"));
      keytermList.add(new Keyterm("CFTR degradation"));
      keytermList.add(new Keyterm("CFTR"));
      keytermList.add(new Keyterm("degradation"));
      keytermList.add(new Keyterm("contribute"));
      keytermList.add(new Keyterm("cystic fibrosis"));
    }

    else if (question.equals("How do Bop-Pes interactions affect cell growth?")) {
      keytermList.add(new Keyterm("Bop-Pes interactions"));
      keytermList.add(new Keyterm("Bop"));
      keytermList.add(new Keyterm("Pes"));
      keytermList.add(new Keyterm("interactions"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("cell growth"));
    }

    else if (question
            .equals("How do interactions between insulin-like GFs and the insulin receptor affect skin biology?")) {
      keytermList.add(new Keyterm("interactions"));
      keytermList.add(new Keyterm("insulin-like GFs"));
      keytermList.add(new Keyterm("insulin receptor"));
      keytermList.add(new Keyterm("affect"));
    }

    else if (question
            .equals("How do interactions between HNF4 and COUP-TF1 suppress liver function?")) {
      keytermList.add(new Keyterm("skin biology"));
      keytermList.add(new Keyterm("interactions"));
      keytermList.add(new Keyterm("HNF4"));
      keytermList.add(new Keyterm("COUP-TF1"));
      keytermList.add(new Keyterm("suppress"));
      keytermList.add(new Keyterm("liver function"));
    }

    else if (question.equals("How do Ret-GDNF interactions affect liver development?")) {
      keytermList.add(new Keyterm("Ret-GDNF interactions"));
      keytermList.add(new Keyterm("Ret"));
      keytermList.add(new Keyterm("GDNF"));
      keytermList.add(new Keyterm("interactions"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("liver development"));
    }

    else if (question
            .equals("How do mutations in the Huntingtin gene affect Huntington's disease?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("Huntingtin gene"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("Huntington's disease"));
    }

    else if (question
            .equals("How do  mutations in Sonic Hedgehog genes affect developmental disorders?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("Sonic Hedgehog genes"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("developmental disorders"));
    }

    else if (question.equals("How do  mutations in the NM23 gene affect tracheal development?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("NM23 gene"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("tracheal development"));
    }

    else if (question.equals("How do  mutations in the Pes gene affect cell growth?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("Pes gene"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("cell growth"));
    }

    else if (question
            .equals("How do  mutations in the hypocretin receptor 2 gene affect narcolepsy?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("hypocretin receptor 2 gene"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("narcolepsy"));
    }

    else if (question
            .equals("How do  mutations in the Presenilin-1 gene affect Alzheimer's disease?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("Presenilin-1 gene"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("Alzheimer's disease"));
    }

    else if (question
            .equals("How do  mutations in familial hemiplegic migraine type 1 (FHM1) gene affect calcium ion influx in hippocampal neurons?")) {
      keytermList.add(new Keyterm("mutations"));
      keytermList.add(new Keyterm("familial hemiplegic migraine type 1"));
      keytermList.add(new Keyterm("FHM1"));
      keytermList.add(new Keyterm("affect"));
      keytermList.add(new Keyterm("calcium ion influx"));
      keytermList.add(new Keyterm("calcium ion"));
      keytermList.add(new Keyterm("influx"));
      keytermList.add(new Keyterm("hippocampal neurons"));
    }

    return keytermList;
  }
}
