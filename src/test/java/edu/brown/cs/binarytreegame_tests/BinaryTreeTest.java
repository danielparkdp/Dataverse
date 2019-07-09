package edu.brown.cs.binarytreegame_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import edu.brown.cs.binarytreegame.BinaryTree;
import edu.brown.cs.binarytreegame.BinaryTreeNode;

public class BinaryTreeTest {

  @Test
  public void testInstantiation() {
    Double[] numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 12.0, 14.0, 17.0
    };
    BinaryTree t = new BinaryTree(Arrays.asList(numbers));
    assertNotNull(t);
    assertEquals(t.getRoot(), new BinaryTreeNode(8.0, 0, "b"));
  }

  @Test
  public void testCorrectConstruction() {
    Double[] numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 12.0, 14.0, 17.0
    };
    BinaryTree t = new BinaryTree(Arrays.asList(numbers));
    assertEquals(t.getRoot(), new BinaryTreeNode(8.0, 0, "b"));
    assertEquals(t.getRoot().getLeft(), new BinaryTreeNode(4.0, 1, "bl"));
    assertEquals(t.getRoot().getRight(), new BinaryTreeNode(14.0, 1, "br"));
    assertEquals(t.getRoot().getLeft().getRight(),
        new BinaryTreeNode(6.0, 2, "blr"));
    assertEquals(t.getRoot().getRight().getLeft(),
        new BinaryTreeNode(12.0, 2, "brl"));
  }

  @Test
  public void testCorrectAdding() {
    Double[] numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 9.0, 12.0, 14.0, 17.0
    };
    BinaryTree t = new BinaryTree(Arrays.asList(numbers));
    // if even select right number
    assertEquals(t.getRoot(), new BinaryTreeNode(9.0, 1, "b"));
    t.addValue(15.0);
    assertEquals(t.getRoot().getRight().getRight().getLeft(),
        new BinaryTreeNode(15.0, 3, "brrl"));
  }

  @Test
  public void testNodeById() {
    Double[] numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 12.0, 14.0, 17.0
    };
    BinaryTree t = new BinaryTree(Arrays.asList(numbers));
    String id = "bl";
    assertEquals(t.getNodeById(id), new BinaryTreeNode(4.0, 1, "bl"));
    id = "blr";
    assertEquals(t.getNodeById(id), new BinaryTreeNode(6.0, 2, "blr"));
  }

  @Test
  public void testCorrectDepth() {
    Double[] numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 12.0, 14.0, 17.0
    };
    BinaryTree t = new BinaryTree(Arrays.asList(numbers));
    assertTrue(t.getDepth() == 2);
    numbers = new Double[] {
        3.0, 4.0, 6.0, 8.0, 12.0, 14.0, 17.0, 22.0, -12.0
    };
    t = new BinaryTree(Arrays.asList(numbers));
    assertTrue(t.getDepth() == 3);
  }

}
