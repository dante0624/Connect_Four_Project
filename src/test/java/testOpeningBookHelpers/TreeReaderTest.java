package testOpeningBookHelpers;

import openingBookHelpers.TreeReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TreeReaderTest {
    // Class variables for writing and reading a file
    Path path;
    File file;
    FileOutputStream out;

    /* This directory and the files created in it will be deleted after
     * tests are run, even in the event of failures or exceptions.
     */
    @TempDir
    Path tempDir;

    // The actual reader object we a testing
    TreeReader treeReader;

    // Helper functions that write important tree data to a file
    void writePositiveTree() throws IOException {
        /* Writes content to a file,
         * equivalent to the expectedLeftTree from AVLTreeWriterTest.
         * Tree has the structure:
         *              4
         *            /   \
         *           /     \
         *          1       6
         *         / \
         *        /   \
         *       0     2
         */
        byte[] positiveTreeData = new byte[] { // every line of this is a node
                0, 0, 0, 0, 0, 2, 9, 0, 0, 3,
                0, 0, 0, 0, 0, 0, -125, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 1, 4, 0, 0, 0,
                0, 0, 0, 0, 0, 3, 12, 0, 0, 0
        };
        out.write(positiveTreeData);
    }
    void writeNegativeTree() throws IOException {
        /* Writes content to a file,
         * Depicted are the evals.
         * Note that keys cannot be negative (because they use 49 bits of 64 long bits)
         * So the key -1 is simply the largest possible key, and -2 is one less than that etc.
         * Tree has the structure:
         *             -4
         *            /  \
         *           /    \
         *          -6    -1
         *                /
         *               /
         *              -2
         */
        byte[] negativeTreeData = new byte[] { // every line of this is a node
                -1, -1, -1, -1, -1, -2, 121, 0, 0, 1,
                -1, -1, -1, -1, -1, -3, 116, 0, 0, 0,
                -1, -1, -1, -1, -1, -1, -2, 0, 0, 1,
                -1, -1, -1, -1, -1, -1, 124, 0, 0, 0,
        };
        out.write(negativeTreeData);
    }

    @BeforeEach
    void setUP() throws FileNotFoundException {
        // Sets up a temporary file that can be written to and read from
        try {
            path = tempDir.resolve("depth0Book.bin");
        } catch (InvalidPathException ipe) {
            System.err.println(
                    "error creating temporary test file in " +
                            this.getClass().getSimpleName());
        }

        file = path.toFile();

        // Set up both I/O Streams
        out = new FileOutputStream(file);

        // Set up the tree reader with a made up depth of 0
        treeReader = new TreeReader(file);
    }

    @AfterEach
    void closeStreams() throws IOException {
        out.close();
    }

    @ParameterizedTest
    @ValueSource(ints = { 4, 1, 0, 2, 6 })
    void testGetPositive(int eval) throws IOException {
        writePositiveTree();
        assertEquals(eval, treeReader.get(eval));
    }

    @ParameterizedTest
    @ValueSource(ints = { -4, -6, -1, -2 })
    void testGetNegative(int eval) throws IOException {
        /* Key's can't actually be negative because they only use 49 bits of the long
         * But all 49 bits of 1's is what we store in place of -1
         */
        writeNegativeTree();
        assertEquals(eval, treeReader.get((long) eval & 0x1FFFFFFFFFFFFL));
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 5, 7 })
    void testGetException(int eval) throws IOException {
        // These should all throw errors as they are not in the positive tree
        writePositiveTree();
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> treeReader.get(eval)
        );
        assertEquals("No key of " + eval + " found", thrown.getMessage());

    }

    @Test
    void testIteratorPositive() throws IOException {
        writePositiveTree();
        long[] expectedKeys = { 4, 1, 0, 2, 6 };
        int i = 0;
        for (long key : treeReader) {
            assertEquals(expectedKeys[i], key);
            i++;
        }

    }

	@Test
	void testGetMaxBookDepth() {
		assertEquals(12, TreeReader.getMaxBookDepth());
	}


}
