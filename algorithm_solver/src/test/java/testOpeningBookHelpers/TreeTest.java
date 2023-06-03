package testOpeningBookHelpers;

import openingBookHelpers.Tree;
import openingBookHelpers.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TreeTest {
    Tree tree;

    @BeforeEach
    void createTree() {
        // The tree will have all keys = values for simplicity
        // Root is 4, left child is 2, right child is 6
        // This allows for inserting 1, 3, 5, 7 for testing
        tree = new Tree();
        tree.root = new TreeNode(4L, (byte) 4);
        tree.root.left = new TreeNode(2L, (byte) 2);
        tree.root.right = new TreeNode(6L, (byte) 6);
    }

    @Test
    void testGetRoot() {
        TreeNode root = tree.getRoot();
        assertEquals(4L, root.key);
        assertEquals((byte) 4, root.value);
    }

    @Test
    void testInsertNode() {
        tree.insertNode(1L, (byte) 1);
        assertEquals(1L, tree.root.left.left.key);
        assertEquals((byte) 1, tree.root.left.left.value);

        tree.insertNode(3L, (byte) 3);
        assertEquals(3L, tree.root.left.right.key);
        assertEquals((byte) 3, tree.root.left.right.value);

        tree.insertNode(5L, (byte) 5);
        assertEquals(5L, tree.root.right.left.key);
        assertEquals((byte) 5, tree.root.right.left.value);

        tree.insertNode(7L, (byte) 7);
        assertEquals(7L, tree.root.right.right.key);
        assertEquals((byte) 7, tree.root.right.right.value);
    }

    @Test
    void testInsertNodeException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> tree.insertNode(2L, (byte) 2)
        );
        assertEquals("Tree already contains a node with key 2", thrown.getMessage());
    }

    @Test
    void testSearchNode() {
        TreeNode node = tree.searchNode(4L);
        assertEquals(tree.root, node);

        node = tree.searchNode(2L);
        assertEquals(tree.root.left, node);

        node = tree.searchNode(6L);
        assertEquals(tree.root.right, node);

        node = tree.searchNode(1L);
        assertNull(node);
    }
}
