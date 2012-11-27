package edu.cmu.lti.oaqa.openqa.test.team04.keyterm.austinma;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

/**
 * An example annotator that discovers Person Titles in text and classifies them into three
 * categories - Civilian (e.g. Mr.,Ms.), Military (e.g. Lt. Col.) , and Government (e.g. Gov.,
 * Sen.). The titles are detected using simple string matching. The strings that are matched are
 * determined by the <code>CivilianTitles</code>, <code>MilitaryTitles</code>, and
 * <code>GovernmentTitles</code> configuration parameters.
 * <p>
 * If the <code>ContainingAnnotationType</code> parameter is specified, this annotator will only
 * look for titles within existing annotations of that type. This feature can be used, for example,
 * to only match person titles within existing Person Name annotations, discovered by some annotator
 * that has run previously.
 * 
 * 
 */
public class GeneAnnotator extends CasAnnotator_ImplBase {
  /**
   * The Type of Annotation that we will be creating when we find a match.
   */
  private Type mGeneAnnotationType;

  /**
   * The Annotation Type within which we will search for Person Titles (optional).
   */
  private Type mContainingType;

  /**
   * Show warning message just once
   */
  private boolean warningMsgShown = false;

  /**
   * File location of NER model
   */
  File nerModelFile;

  /**
   * Ling pipe chunker that will do the NER for us
   */
  Chunker chunker;

  HashSet geneDictionary;

  private Logger logger;

  /**
   * Performs initialization logic. This implementation just reads values for the configuration
   * parameters.
   * 
   * @see org.apache.uima.analysis_engine.annotator.BaseAnnotator#initialize(AnnotatorContext)
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // read configuration parameter values

    // write log messages
    logger = getContext().getLogger();
    logger.log(Level.CONFIG, "GeneAnnotator initialized");

    // Set up lingpipe
    nerModelFile = new File("src/main/resources/ne-en-bio-genetag.HmmChunker");
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(nerModelFile);
    } catch (IOException e) {
      System.err.println("IOException in creating chunker");
      throw new ResourceInitializationException("Unable to load NER model file",
              "load_ner_model_error", new Object[] { nerModelFile }, e);
    } catch (ClassNotFoundException e) {
      System.err.println("ClassNotFoundException in creating chunker");
      throw new ResourceInitializationException("Unable to load NER model file",
              "load_ner_model_error", new Object[] { nerModelFile }, e);
    }

    // Read in the geneDictionary
    geneDictionary = new HashSet<String>();
    try {
      File geneDictionaryFile = new File("src/main/resources/ref.dic");
      BufferedReader reader = new BufferedReader(new FileReader(geneDictionaryFile));
      String line;
      try {
        while ((line = reader.readLine()) != null)
          geneDictionary.add(line);
      } catch (IOException e) {
        System.err.println("IOException in reading gene dictionary file");
        throw new ResourceInitializationException("Unable to load gene dictionary file",
                "load_gene_dic_error", new Object[] {}, e);
      }
    } catch (FileNotFoundException e) {
      System.err.println("FileNotFoundException in reading gene dictionary file");
      throw new ResourceInitializationException("Unable to find gene dictionary file",
              "load_gene_dic_error", new Object[] {}, e);
    }
  }

  /**
   * Called whenever the CAS type system changes. Acquires references to Types and Features.
   * 
   * @see org.apache.uima.analysis_engine.annotator.BaseAnnotator#typeSystemInit(TypeSystem)
   */
  public void typeSystemInit(TypeSystem aTypeSystem) throws AnalysisEngineProcessException {
    // Get a reference to the "GeneAnnotation" Type
    mGeneAnnotationType = aTypeSystem.getType("GeneAnnotation");
    if (mGeneAnnotationType == null) {
      throw new AnalysisEngineProcessException(AnnotatorInitializationException.TYPE_NOT_FOUND,
              new Object[] { getClass().getName(), "GeneAnnotation" });
    }

    // Get the value for the "ContainingType" parameter if there is one
    String containingTypeName = (String) getContext().getConfigParameterValue(
            "ContainingAnnotationType");
    if (containingTypeName != null) {
      mContainingType = aTypeSystem.getType(containingTypeName);
      if (mContainingType == null) {
        throw new AnalysisEngineProcessException(AnnotatorInitializationException.TYPE_NOT_FOUND,
                new Object[] { getClass().getName(), containingTypeName });
      }
    }
  }

  /**
   * Annotates a document. This annotator searches for person titles using simple string matching.
   * 
   * @param aCAS
   *          CAS containing document text and previously discovered annotations, and to which new
   *          annotations are to be written.
   * 
   * @see CasAnnotator_ImplBase#process(CAS)
   */
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    try {
      // If the ResultSpec doesn't include the PersonTitle type, we have
      // nothing to do.
      if (!getResultSpecification().containsType("GeneAnnotation", aCAS.getDocumentLanguage())) {
        if (!warningMsgShown) {
          String m = String.format(
                  "No output is being produced by the GeneAnnotator because the Result Specification did not contain"
                          + " a request for the type GeneAnnotation with the language '%s'%n"
                          + "  (Note: this message will only be shown once.)%n",
                  aCAS.getDocumentLanguage());
          System.err.println(m);
          logger.log(Level.WARNING, m);
          warningMsgShown = true;
        }
        return;
      }

      if (mContainingType == null) {
        // Search the whole document for gene annotations
        String text = aCAS.getDocumentText();
        String[] lines = text.split("\n");
        for (String line : lines) {
          annotateRange(aCAS, line, 0);
        }
      } else {
        // Search only within annotations of type mContainingType

        // Get an iterator over the annotations of type mContainingType.
        FSIterator it = aCAS.getAnnotationIndex(mContainingType).iterator();
        // Loop over the iterator.
        while (it.isValid()) {
          // Get the next annotation from the iterator
          AnnotationFS annot = (AnnotationFS) it.get();
          // Get text covered by this annotation
          String coveredText = annot.getCoveredText();
          String[] lines = coveredText.split("\n");
          for (String line : lines) {
            // Get begin position of this annotation
            int annotBegin = annot.getBegin();
            // search for matches within this
            annotateRange(aCAS, line, annotBegin);
          }
          // Advance the iterator.
          it.moveToNext();
        }
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected int getOffset(String sentence, int pos) {
    int offset = 0;
    for (int i = 0; i < pos; i++)
      if (sentence.charAt(i) == ' ')
        offset++;
    return offset;
  }

  /**
   * A utility method that searches a part of the document for Gene names.
   * 
   * @param aCAS
   *          the CAS in which to create new annotations
   * @param aText
   *          the substring of the document text within which to search
   * @param aBeginPos
   *          the position of this substring relative to the start of the document
   */
  protected void annotateRange(CAS aCAS, String aText, int aBeginPos) {
    int firstSpace = aText.indexOf(" ");
    int firstNewLine = aText.indexOf("\n");
    String sentenceID = aText.substring(0, firstSpace);
    String sentenceText = aText.substring(firstSpace + 1);
    HashSet<Span> annotatedSpans = new HashSet<Span>();
    Chunking chunking = chunker.chunk(sentenceText);

    for (Chunk chunk : chunking.chunkSet()) {

      int start = chunk.start();
      int end = chunk.end();
      String spanText = sentenceText.substring(start, end);

      createAnnotation(aCAS, sentenceID, start, end, spanText, sentenceText);
      System.out.println(String.format("%s|%d %d|%s", sentenceID, start, end, spanText));
    }

    for (int start = 0; start < sentenceText.length(); start++) {
      if (start != 0 && sentenceText.charAt(start - 1) != ' ')
        continue;

      for (int end = start + 1; end < sentenceText.length(); end++) {
        if (end != sentenceText.length() - 1 && sentenceText.charAt(end + 1) != ' ')
          continue;

        String spanText = sentenceText.substring(start, end);
        if (geneDictionary.contains(spanText) && !annotatedSpans.contains(new Span(start, end))) {
          annotatedSpans.add(new Span(start, end));
          createAnnotation(aCAS, sentenceID, start, end, spanText, sentenceText);
          System.out.println(String.format("%s|%d %d|%s", sentenceID, start, end, spanText));
        }
      }
    }
  }

  /**
   * Creates an PersonTitle annotation in the CAS.
   * 
   * @param aCAS
   *          the CAS in which to create the annotation
   * @param aBeginPos
   *          the begin position of the annotation relative to the start of the document
   * @param aEndPos
   *          the end position of the annotation relative to the start of the document. (Note that,
   *          as in the Java string functions, the end position is one past the last character in
   *          the annotation, so that (end - begin) = length.
   */
  protected void createAnnotation(CAS aCAS, String sentenceID, int aBeginPos, int aEndPos,
          String span, String sentenceText) {
    aBeginPos -= getOffset(sentenceText, aBeginPos);
    aEndPos -= getOffset(sentenceText, aEndPos) + 1;
    GeneAnnotation annotation = (GeneAnnotation) aCAS.createAnnotation(mGeneAnnotationType,
            aBeginPos, aEndPos);
    annotation.setSentenceID(sentenceID);
    annotation.setTextSpan(span);
    // Add the annotation to the index.
    aCAS.getIndexRepository().addFS(annotation);
  }
}
