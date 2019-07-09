package edu.brown.cs.binarytreegame;

import java.util.Objects;

import edu.brown.cs.game_generics.BinaryNode;

public class BinaryTreeNode extends BinaryNode {

  // can be used to uniquely identify this nodes position within the tree.
  private String id;
  private int depth;

  public BinaryTreeNode(double val, int depth, String id) {
    super(val);
    this.depth = depth;
    this.id = id;
  }

  /**
   * copy constructor
   *
   * @param other
   *          the node to make a deep copy of. This doesnt include children.
   */
  public BinaryTreeNode(BinaryTreeNode other) {
    super(other.getValue());
    this.depth = other.getDepth();
    this.id = other.getID();
  }

  public int getDepth() {
    return this.depth;
  }

  public String getID() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    BinaryTreeNode n = (BinaryTreeNode) o;
    if (n.getID().equals(this.id) && n.getValue() == this.getValue()) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.getValue());
  }

  @Override
  public String toString() {
    return "[BinaryTreeNode " + id + ", holding value"
        + Double.toString(this.getValue()) + "]";
  }

}
