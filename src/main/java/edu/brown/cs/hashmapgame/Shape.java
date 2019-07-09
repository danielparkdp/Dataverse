package edu.brown.cs.hashmapgame;

import java.util.Objects;

public class Shape {

  private int color;
  private int shape;
  private int size;
  private int value;
  private final int VALUE_RANGE = 100;

  public Shape(int color, int shape, int size, int value) {
    this.color = color;
    this.shape = shape;
    this.size = size;
    this.value = value;
  }

  // makes a shape with random characteristics
  public Shape() {
    this.size = (int) (Math.random() * SIZE.values().length);
    this.shape = (int) (Math.random() * SHAPES.values().length);
    this.color = (int) (Math.random() * COLOR.values().length);
    this.value = (int) (Math.random() * VALUE_RANGE);
  }

  public int getSize() {
    return this.size;
  }

  public int getColor() {
    return this.color;
  }

  public int getShape() {
    return this.shape;
  }

  public int getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    Shape s = (Shape) o;
    if (this.color == s.getColor() && this.shape == s.getShape()
        && this.size == s.getSize() && this.value == s.getValue()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.size, this.color, this.shape, this.value);
  }

  @Override
  public String toString() {
    return Integer.toString(this.color) + Integer.toString(this.shape)
        + " of size " + Integer.toString(this.size) + " holding the value "
        + Integer.toString(this.value);
  }

}
