package edu.brown.cs.game_generics;

public class IllegalGameStateException extends Exception {

  private String description;

  public IllegalGameStateException(String string) {
    this.description = string;
  }

  @Override
  public String getMessage() {
    return this.description;
  }

  /**
   * eclipse wanted this
   */
  private static final long serialVersionUID = 1L;

}
