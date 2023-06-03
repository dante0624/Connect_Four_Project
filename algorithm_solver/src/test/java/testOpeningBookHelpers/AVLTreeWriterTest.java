package testOpeningBookHelpers;

import openingBookHelpers.AVLTreeWriter;
import openingBookHelpers.TreeNode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AVLTreeWriterTest {
    // Class variables for testing insertion
    AVLTreeWriter treeWriter;

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

    // Creates some good trees for testing
    private AVLTreeWriter createLeftTree() {
        /* Tree has the structure:
         *              4
         *            /   \
         *           /     \
         *          1       6
         *         / \
         *        /   \
         *       0     2
         */
        AVLTreeWriter leftTree = new AVLTreeWriter();
        leftTree.root = new TreeNode(4L, (byte) 4);
        leftTree.root.height = 2;

        leftTree.root.left = new TreeNode(1L, (byte) 1);
        leftTree.root.left.height = 1;

        leftTree.root.left.left = new TreeNode(0L, (byte) 0);
        leftTree.root.left.right = new TreeNode(2L, (byte) 2);
        leftTree.root.right = new TreeNode(6L, (byte) 6);
        return leftTree;
    }
    private AVLTreeWriter createRightTree() {
        /* Tree has the structure:
         *              4
         *            /   \
         *           /     \
         *          2       7
         *                 / \
         *                /   \
         *               6     8
         */
        AVLTreeWriter rightTree = new AVLTreeWriter();
        rightTree.root = new TreeNode(4L, (byte) 4);
        rightTree.root.height = 2;

        rightTree.root.right = new TreeNode(7L ,(byte) 7);
        rightTree.root.right.height = 1;

        rightTree.root.right.right = new TreeNode(8L, (byte) 8);
        rightTree.root.right.left = new TreeNode(6L, (byte) 6);
        rightTree.root.left = new TreeNode(2L, (byte) 2);
        return rightTree;
    }

    @BeforeEach
    void setUp() throws FileNotFoundException {
        /* The tree will have all keys = values for simplicity
         * Tree has the structure:
         *              4
         *            /   \
         *           /     \
         *          2       6
         */
        // This allows for inserting 1, 3, 5, 7 for testing
        treeWriter = new AVLTreeWriter();
        treeWriter.root = new TreeNode(4L, (byte) 4);
        treeWriter.root.height = 1;

        treeWriter.root.left = new TreeNode(2L, (byte) 2);
        treeWriter.root.right = new TreeNode(6L, (byte) 6);

        // Create file for writing and reading content as well
        // Set up a temporary directory and file
        try {
            path = tempDir.resolve("testWriteContent.bin");
        } catch (InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName());
        }

        file = path.toFile();

        // Set up both I/O Streams
        out = new FileOutputStream(file);
        in = new FileInputStream(file);
    }

    @AfterEach
    void closeStreams() throws IOException {
        in.close();
        out.close();
    }

    // Insert double left
    @Test
    void testInsertLeftLeft() {
        AVLTreeWriter leftTree = createLeftTree();
        treeWriter.insertNode(1L, (byte) 1);

        // This line should force some re-balancing
        treeWriter.insertNode(0L, (byte) 0);

        assertTrue(treeEquality(leftTree.root, treeWriter.root));
    }

    // Insert left, then right of that
    @Test
    void testInsertLeftRight() {
        AVLTreeWriter leftTree = createLeftTree();
        treeWriter.insertNode(0L, (byte) 0);

        // This line should force some re-balancing
        treeWriter.insertNode(1L, (byte) 1);

        assertTrue(treeEquality(leftTree.root, treeWriter.root));
    }

    // Insert double right
    @Test
    void testInsertRightRight() {
        AVLTreeWriter rightTree = createRightTree();
        treeWriter.insertNode(7L, (byte) 7);

        // This line should force some re-balancing
        treeWriter.insertNode(8L, (byte) 8);

        assertTrue(treeEquality(rightTree.root, treeWriter.root));
    }

    // Insert right, then left of that
    @Test
    void testInsertRightLeft() {
        AVLTreeWriter rightTree = createRightTree();
        treeWriter.insertNode(8L, (byte) 8);

        // This line should force some re-balancing
        treeWriter.insertNode(7L, (byte) 7);

        assertTrue(treeEquality(rightTree.root, treeWriter.root));
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
        // Write the content
        AVLTreeWriter leftTree = createLeftTree();
        leftTree.writeContent(out);

        // Prepare to read content
        byte[] expected = new byte[] { // every line of this is a node
                0, 0, 0, 0, 0, 2, 9, 0, 0, 3,
                0, 0, 0, 0, 0, 0, -125, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 1, 4, 0, 0, 0,
                0, 0, 0, 0, 0, 3, 12, 0, 0, 0
        };
        byte[] actual = new byte[50];

        // Assert that 50 bytes were read
        assertEquals(50, in.read(actual));
        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }

    @Test
    void testWriteContentNegative() throws IOException {
        /* Writes content to a file,
         * Depicted are the evals.
         * Note that keys are negative, but only the 49 least significant bits will be written,
         * which is the equivalent of writing a large positive long.
         * Tree has the structure:
         *             -4
         *            /  \
         *           /    \
         *          -6    -1
         *                /
         *               /
         *              -2
         */
        AVLTreeWriter negativeTree = new AVLTreeWriter();
        negativeTree.root = new TreeNode(-4L, (byte) -4);
        negativeTree.root.height = 2;

        negativeTree.root.right = new TreeNode(-1L ,(byte) -1);
        negativeTree.root.right.height = 1;

        negativeTree.root.right.left = new TreeNode(-2L, (byte) -2);
        negativeTree.root.left = new TreeNode(-6L, (byte) -6);

        // Write the tree
        negativeTree.writeContent(out);


        // This data matches the tree described above
        byte[] expected = new byte[] { // every line of this is a node
                -1, -1, -1, -1, -1, -2, 121, 0, 0, 1,
                -1, -1, -1, -1, -1, -3, 116, 0, 0, 0,
                -1, -1, -1, -1, -1, -1, -2, 0, 0, 1,
                -1, -1, -1, -1, -1, -1, 124, 0, 0, 0,
        };
        byte[] actual = new byte[40];

        // Assert that 50 bytes were read
        assertEquals(40, in.read(actual));
        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }

}
