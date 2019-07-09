package edu.brown.cs.game_generics;

public class InvalidMoveException extends Exception {
  private String errorDesciption;
  /**
   * i guess it wanted me to do this.
   */
  private static final long serialVersionUID = 1L;

  public InvalidMoveException(String reason) {
    this.errorDesciption = reason;
  }

  @Override
  public String getMessage() {
    return this.errorDesciption;
  }

}
