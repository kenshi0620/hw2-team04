package edu.cmu.lti.oaqa.openqa.test.team04.ner_afandria;
import java.util.Map;
import java.util.TreeMap;

public class AnnotationUtilities {

  /**
   * How much score/confidence to give for all named entities recognized
   */
  public static int THRESHOLD_SCORE = 3;

  /**
   * Converts Gene type structure to format as described in "sample.out"
   * 
   * @param g
   *          Gene to get string
   * @return A formatted gene mention string
   */
  @SuppressWarnings("deprecation")
  public static String geneToString(Gene g, boolean newLine) {
    StringBuilder lineBuilder = new StringBuilder();
    lineBuilder.append(g.getIdentifier());
    lineBuilder.append("|");
    lineBuilder.append(g.getStart());
    lineBuilder.append(" ");
    lineBuilder.append(g.getEnd());
    lineBuilder.append("|");
    lineBuilder.append(g.getContent());
    if (newLine)
      lineBuilder.append("\n");
    return lineBuilder.toString();
  }

  /**
   * @param s
   *          String to find spaces in
   * @return Number of spaces found in s
   */
  public static int getSpaces(String s) {
    int x = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == ' ')
        x++;
    }
    return x;
  }

  /**
   * Retrieve sentences formatted per sample.in from a string representing the entire document to be
   * annotated
   * 
   * @param document
   * @return Map from sentence identifiers to sentence contents
   */
  public static Map<String, String> getSentences(String document) {
    String[] lines = document.split("\\n");
    Map<String, String> sentences = new TreeMap<String, String>();
    for (int i = 0; i < lines.length; i++) {
      // TODO (afandria): this is pretty ugly Java; find a more succinct way to say the following
      sentences.put(lines[i].substring(0, lines[i].indexOf(' ')),
              lines[i].substring(lines[i].indexOf(' ') + 1));
    }
    return sentences;
  }
}
