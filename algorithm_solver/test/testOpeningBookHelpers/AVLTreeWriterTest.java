package testOpeningBookHelpers;

import openingBookHelpers.AVLTreeWriter;
import openingBookHelpers.TreeNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class AVLTreeWriterTest {
    // Class variables for testing insertion
    AVLTreeWriter treeWriter;
    AVLTreeWriter expectedLeftTree;
    AVLTreeWriter expectedRightTree;

    // Class variables for writing to a file
    Path path;
    File file;
    FileOutputStream out;
    FileInputStream in;

    /* This directory and the files created in it will be deleted after
     * tests are run, even in the event of failures or exceptions.
     */
    @TempDir
    Path tempDir;

    // Helper methods for checking if trees are equivalent
    // Check if nodes are equal based on their key, value, and height attributes
    // Assume that we are calling on non-null nodes
    private boolean nodeEquality(TreeNode node1, TreeNode node2) {
        return node1.key == node2.key &&
                node1.value == node2.value &&
                node1.height == node2.height;
    }

    // Checks if entire subtrees are equal based on each of their key, value, and height attributes
    // Uses simply recursion
    private boolean treeEquality(TreeNode node1, TreeNode node2) {
        // Edge case of both null subtrees
        if (node1 == null && node2 == null) {
            return true;
        }
        // Edge case where one, but not both, are null
        if (node1 == null || node2 == null) {
            return false;
        }

        return nodeEquality(node1, node2) &&
                treeEquality(node1.left, node2.left) &&
                treeEquality(node1.right, node2.right);
    }

    @BeforeEach
    void createTrees() {
        // The tree will have all keys = values for simplicity
        // Root is 4, left child is 2, right child is 6
        // This allows for inserting 1, 3, 5, 7 for testing
        treeWriter = new AVLTreeWriter();
        treeWriter.root = new TreeNode(4L, (byte) 4);
        treeWriter.root.height = 1;

        treeWriter.root.left = new TreeNode(2L, (byte) 2);
        treeWriter.root.right = new TreeNode(6L, (byte) 6);


        // Expected tree after inserting a couple nodes to the left side
        expectedLeftTree = new AVLTreeWriter();
        expectedLeftTree.root = new TreeNode(4L, (byte) 4);
        expectedLeftTree.root.height = 2;

        expectedLeftTree.root.left = new TreeNode(1L, (byte) 1);
        expectedLeftTree.root.left.height = 1;

        expectedLeftTree.root.left.left = new TreeNode(0L, (byte) 0);
        expectedLeftTree.root.left.right = new TreeNode(2L, (byte) 2);
        expectedLeftTree.root.right = new TreeNode(6L, (byte) 6);


        // Expected tree after inserting a couple nodes to the right side
        expectedRightTree = new AVLTreeWriter();
        expectedRightTree.root = new TreeNode(4L, (byte) 4);
        expectedRightTree.root.height = 2;

        expectedRightTree.root.right = new TreeNode(7L ,(byte) 7);
        expectedRightTree.root.right.height = 1;

        expectedRightTree.root.right.right = new TreeNode(8L, (byte) 8);
        expectedRightTree.root.right.left = new TreeNode(6L, (byte) 6);
        expectedRightTree.root.left = new TreeNode(2L, (byte) 2);
    }

    // Insert double left
    @Test
    void testInsertLeftLeft() {
        treeWriter.insertNode(1L, (byte) 1);

        // This line should force some re-balancing
        treeWriter.insertNode(0L, (byte) 0);

        assertTrue(treeEquality(expectedLeftTree.root, treeWriter.root));
    }

    // Insert left, then right of that
    @Test
    void testInsertLeftRight() {
        treeWriter.insertNode(0L, (byte) 0);

        // This line should force some re-balancing
        treeWriter.insertNode(1L, (byte) 1);

        assertTrue(treeEquality(expectedLeftTree.root, treeWriter.root));
    }

    // Insert double right
    @Test
    void testInsertRightRight() {
        treeWriter.insertNode(7L, (byte) 7);

        // This line should force some re-balancing
        treeWriter.insertNode(8L, (byte) 8);

        assertTrue(treeEquality(expectedRightTree.root, treeWriter.root));
    }

    // Insert right, then left of that
    @Test
    void testInsertRightLeft() {
        treeWriter.insertNode(8L, (byte) 8);

        // This line should force some re-balancing
        treeWriter.insertNode(7L, (byte) 7);

        assertTrue(treeEquality(expectedRightTree.root, treeWriter.root));
    }

    @Test
    void testHardInsert() {
        AVLTreeWriter hardExpectedTree = new AVLTreeWriter();
        hardExpectedTree.root = new TreeNode(2L, (byte) 2);
        hardExpectedTree.root.height = 2;

        hardExpectedTree.root.left = new TreeNode(1L, (byte) 1);
        hardExpectedTree.root.left.height = 1;
        hardExpectedTree.root.right = new TreeNode(4L, (byte) 4);
        hardExpectedTree.root.right.height = 1;

        hardExpectedTree.root.left.left = new TreeNode(0L, (byte) 0);
        hardExpectedTree.root.right.left = new TreeNode(3L, (byte) 3);
        hardExpectedTree.root.right.right = new TreeNode(6L, (byte) 6);

        // Do the inserting
        treeWriter.insertNode(0L, (byte) 0);

        // This line should force some simple re-balancing
        treeWriter.insertNode(1L, (byte) 1);

        // This line forces hard re-balancing
        treeWriter.insertNode(3L, (byte) 3);

        assertTrue(treeEquality(hardExpectedTree.root, treeWriter.root));
    }

    @Test
    void testWriteContent() throws IOException {
        // Set up a temporary directory and file
        try {
            path = tempDir.resolve("testWriteContent.bin");
        } catch (InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName());
        }

        file = path.toFile();

        // Write the content
        out = new FileOutputStream(file);
        expectedLeftTree.writeContent(out);
        out.close();

        // Prepare to read content
        byte[] expected = new byte[] { // every line of this is a node
                0, 0, 0, 0, 0, 2, 9, 0, 0, 3,
                0, 0, 0, 0, 0, 0, -125, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 1, 4, 0, 0, 0,
                0, 0, 0, 0, 0, 3, 12, 0, 0, 0
        };
        byte[] actual = new byte[50];
        in = new FileInputStream(file);

        // Assert that 50 bytes were read
        assertEquals(50, in.read(actual));
        in.close();

        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }



}
