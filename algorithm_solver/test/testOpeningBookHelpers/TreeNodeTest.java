package testOpeningBookHelpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import openingBookHelpers.TreeNode;

import static org.junit.jupiter.api.Assertions.*;

public class TreeNodeTest {
    Path path;
    File file;
    FileOutputStream out;
    FileInputStream in;
    TreeNode node;

    /* This directory and the files created in it will be deleted after
     * tests are run, even in the event of failures or exceptions.
     */
    @TempDir
    Path tempDir;

    /* executed before every test: create a file */
    @BeforeEach
    public void setUp() {
        try {
            path = tempDir.resolve("testWriteContent.bin");
        } catch (InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName());
        }

        file = path.toFile();
    }

    // Test ability to write content to a file
    // In this test, all bytes and node attributes are positive, and both children exist
    @Test
    public void testWriteContent() throws IOException {
        /* This is set up such that the ten bytes written to
         the byte file simply count from 1 to 10 */
        node = new TreeNode(0x10203040506L << 1, (byte) 3);

        // Make the left node have a heavy weight, and the right node exist
        TreeNode heavyLeftNode = new TreeNode(0, (byte) 0);
        heavyLeftNode.weight = 0x8090A;
        node.left = heavyLeftNode;
        node.right = new TreeNode(0, (byte) 0);

        // Write the content
        out = new FileOutputStream(file);
        node.writeContent(out);
        out.close();

        // Prepare to read content
        byte[] expected = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] actual = new byte[10];
        in = new FileInputStream(file);

        // Assert that 10 bytes were read
        assertEquals(10, in.read(actual));
        in.close();

        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }

    // Tests When neither children exist
    @Test
    public void testNoChildrenWriteContent() throws IOException {
        /* Both the key and value are positive, but no children */
        node = new TreeNode(0x10203040506L << 1, (byte) 3);

        // Write the content
        out = new FileOutputStream(file);
        node.writeContent(out);
        out.close();

        // Prepare to read content
        byte[] expected = new byte[] { 1, 2, 3, 4, 5, 6, 6, 0, 0, 0 };
        byte[] actual = new byte[10];
        in = new FileInputStream(file);

        // Assert that 10 bytes were read
        assertEquals(10, in.read(actual));
        in.close();

        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }

    // Tests that we can handle when all the bytes are negative
    @Test
    public void testNegativeBytesWriteContent() throws IOException {
        /* This is set up such that the ten bytes written to
         the byte file simply count from -1 to -10 */
        node = new TreeNode(
                (0xFFFEFDFCFBFAL << 1) + 1,
                (byte) -4
        );

        // Make the left node have a heavy weight, and the right node exist
        TreeNode heavyLeftNode = new TreeNode(0, (byte) 0);
        heavyLeftNode.weight = 0xF8F7F6;
        node.left = heavyLeftNode;
        node.right = new TreeNode(0, (byte) 0);

        // Write the content
        out = new FileOutputStream(file);
        node.writeContent(out);
        out.close();

        // Prepare to read content
        byte[] expected = new byte[] { -1, -2, -3, -4, -5, -6, -7, -8, -9, -10 };
        byte[] actual = new byte[10];
        in = new FileInputStream(file);

        // Assert that 10 bytes were read
        assertEquals(10, in.read(actual));
        in.close();

        // Assert that the content of the file is as expected
        assertArrayEquals(expected, actual);
    }
}
