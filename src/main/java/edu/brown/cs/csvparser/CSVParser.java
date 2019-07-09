package edu.brown.cs.csvparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the CSVParser class, used to take a csv from a file path and parse
 * it. for evaluation. It stores the lines and individual words in line1 and
 * words1.
 */
public class CSVParser {

  private String file1;

  private List<String> line1;
  private List<String[]> words1;

  /**
   * The initial method called when a CSVParser is created.
   *
   * @param file
   *          A String containing the path to a csv file
   */
  public CSVParser(String file) {

    // store base values for instance variables
    file1 = file;
    line1 = new ArrayList<String>();
    words1 = new ArrayList<String[]>();

    // call parseLines to begin parsing
    parseLines();
  }

  /**
   * parseLines() is the method called to parse the file stored in file1. It
   * reads the input, mapping it to line1 entries while watching for any error
   * situations.
   */
  private void parseLines() {
    // create buffered reader to be used to read file
    BufferedReader read = null;
    try {
      // try reading file, catching any error from the process
      read = new BufferedReader(new FileReader(file1));
    } catch (FileNotFoundException exception) {
      // error handling for file not existing
      System.err.println("ERROR: File Not Found");
      // set line1 and words1 to null on error, so we can tell when it failed
      line1 = null;
      words1 = null;
      return;
    }
    try {
      // try reading each line from the BufferedReader into line1 if not null
      String line;
      while (read != null) {
        line = read.readLine();
        if (line == null) {
          break;
        }
        line1.add(line);
      }
    } catch (IOException exception) {
      // watch for error in Input/Output
      System.err
          .println("ERROR: Input/Output Exception on reading lines in file");
      // set line1 and words1 to null on error, so we can tell when it failed
      line1 = null;
      words1 = null;
      return;
    } finally {
      try {
        // regardless, we should try to close the reader
        if (read != null) {
          read.close();
        }
      } catch (IOException exception) {
        // if this closing failed, send error message
        System.err.println("ERROR: Input/Output Exception on closing reader");
        // set line1 and words1 to null on error, so we can tell when it failed
        line1 = null;
        words1 = null;
        return;
      }
    }
    // call parseWords() to move on to parsing the individual words
    parseWords();
  }

  /**
   * This is what is called after the lines are parsed. The words are taken from
   * each line and stored into words1.
   */
  private void parseWords() {
    // for each line, parse words by commas and store in words1
    for (int i = 0; i < line1.size(); i++) {
      words1.add(line1.get(i).split(","));
    }

  }

  /**
   * getLines method, accessing the lines from the file.
   *
   * @return line1 List<String> containing the lines
   */
  public List<String> getLines() {
    return line1;
  }

  /**
   * getWords() method, accessing the words from the file.
   *
   * @return words1 List<String[]> containing the lines broken down into words
   */
  public List<String[]> getWords() {
    return words1;
  }

}