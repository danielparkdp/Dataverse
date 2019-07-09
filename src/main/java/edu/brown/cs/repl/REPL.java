package edu.brown.cs.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * This is the generalized REPL class, used to loop through needed parse/eval.
 * functions. Can be expanded to add new projects!
 *
 */
public class REPL {

  private boolean runWithGui;

  /**
   * REPL constructor, just instantiate needed REPL types.
   */
  public REPL() {
    runWithGui = false;
  }

  /**
   * Set the gui checker.
   *
   * @param b
   *          gui or not
   */
  public void setGui(boolean b) {
    runWithGui = b;
  }

  /**
   * Get the gui checker.
   *
   * @return b gui or not
   */
  public boolean getGui() {
    return runWithGui;
  }

  /**
   * Loops through the parse and eval functions of all repls.
   */
  public void loopRead() {
    // use bufferedreader to read inputs
    BufferedReader read = null;
    String cmd;
    String noSpace;
    int evaluated;

    // add project repls as needed

    try {
      read = new BufferedReader(new InputStreamReader(System.in));
      // get command each time, trimming within individual repls.
      // do not trim in this class because it's possible that a
      // repl might use whitespace to alter commands.
      while ((cmd = read.readLine()) != null) {
        noSpace = cmd.replaceAll("\\s+", " ").trim();
        evaluated = 0;

        // check if trimmed string is zero, (no input -> continue)
        if (noSpace.length() == 0) {
          continue;
        }

        // if no evaluations were made, return the first word given, which is
        // the command
        if (evaluated == 0) {
          System.err.println("ERROR: unknown command - ".concat(noSpace
              .split(" ")[0]));
        }

      }
    } catch (IOException exception) {
      // if I/O Exception, print message and continue
      System.err
          .println("ERROR: Input/Output Exception reading from input stream");
    } finally {
      try {
        // close repls

        if (read != null) {
          // close file if needed
          read.close();
        }
      } catch (IOException exception) {
        System.err
            .println("ERROR: Input/Output Exception in closing reading stream");
      }
    }

  }

}
