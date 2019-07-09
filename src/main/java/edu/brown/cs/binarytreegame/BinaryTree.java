package edu.brown.cs.binarytreegame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//can use prüfer encoding to generate random trees!
//if i want to implement kd trees later i can just make it its own thing. not as extensible but whatever.
//actually kd tree can technically extend binary tree with the creation and insertion being overridden.

//factoring out basically everything so I can send it to the frontend and they can display every step.
public class BinaryTree {

  private BinaryTreeNode root;
  // the deepest the tree goes.
  private int depth = 0;

  // stores a list of all the numbers it contains.
  private List<Double> valuesInTree;

  // is it better to build trees with BinaryTreeNodes or with doubles?
  public BinaryTree(List<Double> nodeValues) {
    // b stands for base
    this.root = this.buildTree(nodeValues, 0, "b");
    this.valuesInTree = nodeValues;
  }

  // pause at each step and send it to the frontend?
  // no^ build it all first and just display parts of it to the frontend.
  // needs to be a list so I can sort it.
  public BinaryTreeNode buildTree(List<Double> nodeValues, int depth,
      String id) {
    if (nodeValues.size() == 0) {
      return null;
    }
    this.sortList(nodeValues);
    int med_index = this.findMedianIndex(nodeValues);
    double med_val = nodeValues.get(med_index);
    BinaryTreeNode node = new BinaryTreeNode(med_val, depth, id);
    List<Double> left_data = nodeValues.subList(0, med_index);
    List<Double> right_data = nodeValues.subList(med_index + 1,
        nodeValues.size());
    node.setLeft(this.buildTree(left_data, depth + 1, id + "l"));
    node.setRight(this.buildTree(right_data, depth + 1, id + "r"));
    // updating depth
    if (depth > this.depth) {
      this.depth = depth;
    }
    return node;
  }

  /**
   * returns a root node holding a copy of the tree but only up to a certain
   * depth.
   *
   * @param depth
   *          an int >=0
   * @return a node holding this tree but only up to a certain depth
   */
  public BinaryTreeNode getSubTreeUpToDepth(int depth) {
    BinaryTreeNode rootCopy = new BinaryTreeNode(this.root.getValue(), 0, "r");
    // perform a BFS on the tree an stop when you see a node whose depth exceeds
    // the requested depth
    // these are being looped through in parallel
    Queue<BinaryTreeNode> nodeQueue = new LinkedList<>();
    Queue<BinaryTreeNode> copyQueue = new LinkedList<>();
    BinaryTreeNode currNode = this.root;
    BinaryTreeNode currCopy = rootCopy;
    nodeQueue.add(root);
    copyQueue.add(currCopy);
    while (!nodeQueue.isEmpty() && currNode.getDepth() < depth) {
      currNode = nodeQueue.poll();
      currCopy = copyQueue.poll();
      BinaryTreeNode leftCopy = new BinaryTreeNode(
          (BinaryTreeNode) currNode.getLeft());
      BinaryTreeNode rightCopy = new BinaryTreeNode(
          (BinaryTreeNode) currNode.getRight());
      currCopy.setLeft(leftCopy);
      currCopy.setRight(rightCopy);
      nodeQueue.add((BinaryTreeNode) currNode.getLeft());
      nodeQueue.add((BinaryTreeNode) currNode.getLeft());
      copyQueue.add(leftCopy);
      copyQueue.add(rightCopy);
    }

    return rootCopy;
  }

  /**
   *
   * @param id
   *          a unique id of the form b[lr]*
   * @return the node at that id.
   */
  public BinaryTreeNode getNodeById(String id) {
    if (id.charAt(0) != 'b') {
      throw new IllegalArgumentException("Id must start with b");
    }
    BinaryTreeNode currNode = this.root;
    int currChar = 1;
    while (currChar < id.length()) {
      char currInstruction = id.charAt(currChar);
      if (currInstruction == 'l') {
        currNode = (BinaryTreeNode) currNode.getLeft();
      } else if (currInstruction == 'r') {
        currNode = (BinaryTreeNode) currNode.getRight();
      } else {
        throw new IllegalArgumentException("id must be of form b[lr]*");
      }
      currChar++;
    }
    return currNode;
  }

  public void addValue(double val) {
    BinaryTreeNode curr = this.root;
    boolean stop = false;
    while (!stop) {
      if (val > curr.getValue()) {
        if (curr.hasRight()) {
          curr = (BinaryTreeNode) curr.getRight();
        } else {
          // make this the right node
          int depth = curr.getDepth() + 1;
          String id = curr.getID() + "r";
          curr.setRight(new BinaryTreeNode(val, depth, id));
          stop = true;
        }
      } else {
        if (curr.hasLeft()) {
          curr = (BinaryTreeNode) curr.getLeft();
        } else {
          // make this the right node
          int depth = curr.getDepth() + 1;
          String id = curr.getID() + "l";
          curr.setLeft(new BinaryTreeNode(val, depth, id));
          stop = true;
        }
      }
    }
  }

  /**
   *
   * @return the root of the tree.
   */
  public BinaryTreeNode getRoot() {
    return this.root;
  }

  /**
   *
   * @return the maximum depth of the tree.
   */
  public int getDepth() {
    return this.depth;
  }

  public List<Double> getValuesInTree() {
    return this.valuesInTree;
  }

  /**
   * helper method to sort a list of doubles.
   *
   * @param nodeValues
   *          the list
   */
  private void sortList(List<Double> nodeValues) {
    Collections.sort(nodeValues);
  }

  /**
   * helper method to find index of median element of a sorted list.
   *
   * @return the index
   */
  private int findMedianIndex(List<Double> sortedList) {
    int median_index = sortedList.size() / 2;
    return median_index;
  }

}
