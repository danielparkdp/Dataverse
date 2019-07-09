package edu.brown.cs.game_generics;

public abstract class BinaryNode {
  private transient BinaryNode leftChild;
  private transient BinaryNode rightChild;
  private double value;

  // can make a gamenode interface that these implement.
  // give each node a unique id?

  /**
   *
   * @param val
   *          the information we want this node to hold.
   */
  public BinaryNode(double val) {
    this.value = val;
  }

  /**
   *
   * @param left
   *          set the left pointer of this node to this.
   */
  public void setLeft(BinaryNode left) {
    this.leftChild = left;
  }

  /**
   *
   * @param right
   *          set the right pointer of this node to this.
   */
  public void setRight(BinaryNode right) {
    this.rightChild = right;
  }

  /**
   *
   * @return the right child if it exists else a nullpointer exception
   */
  public BinaryNode getRight() {
    if (this.hasRight()) {
      return this.rightChild;
    }
    throw new NullPointerException(
        "Node [ " + this.toString() + "] has no right child");
  }

  /**
   *
   * @return the left child if it exists else a nullpointer exception
   */
  public BinaryNode getLeft() {
    if (this.hasLeft()) {
      return this.leftChild;
    }
    throw new NullPointerException(
        "Node [" + this.toString() + "] has no left child");
  }

  /**
   *
   * @return if this node has a right child
   */
  public boolean hasRight() {
    if (this.rightChild == null) {
      return false;
    }
    return true;
  }

  /**
   *
   * @return if this node has a left child.
   */
  public boolean hasLeft() {
    if (this.leftChild == null) {
      return false;
    }
    return true;
  }

  /**
   *
   * @return whether this node has neither a left nor right child.
   */
  public boolean isLeaf() {
    if (!this.hasLeft() && !this.hasRight()) {
      return true;
    }
    return false;
  }

  /**
   *
   * @return the information this node holds.
   */
  public double getValue() {
    return this.value;
  }

  // when should two nodes be considered equal?

  @Override
  public String toString() {
    return "Binary Node holding value " + Double.toString(this.value);
  }

}
