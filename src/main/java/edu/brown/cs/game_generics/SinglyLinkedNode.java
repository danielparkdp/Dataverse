//package edu.brown.cs.game_generics;
//
///**
// * A class for a node with a next pointer.
// *
// * @author csaueres
// *
// */
//public abstract class SinglyLinkedNode {
//
//  private Object value;
//  private SinglyLinkedNode next;
//
//  /**
//   *
//   * @param value
//   *          the information we want this Node to hold.
//   */
//  public SinglyLinkedNode(Object value) {
//    this.value = value;
//  }
//
//  /**
//   *
//   * @return the information this node holds.
//   */
//  public Object getValue() {
//    return this.value;
//  }
//
//  /**
//   *
//   * @return the next pointer for this node if it has one, otherwise throw a
//   *         nullpointer exception.
//   */
//  public SinglyLinkedNode getNext() {
//    if (this.hasNext()) {
//      return this.next;
//    } else {
//      throw new NullPointerException("Node has no next");
//    }
//  }
//
//  /**
//   *
//   * @return true if Node has a next pointer pointing to another node.
//   */
//  public boolean hasNext() {
//    if (this.next == null) {
//      return true;
//    }
//    return false;
//  }
//
//  /**
//   *
//   * @return true if this node doesnt have a next pointer.
//   */
//  public boolean isLast() {
//    return !this.hasNext();
//  }
//
//}
