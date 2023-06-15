package testLiveSolverClasses;

import liveSolverClasses.MoveSorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoveSorterTest {
    // These are just constants that reflect a chip at the bottom of each column
    private static final long col0 = 0x1L;
    private static final long col1 = 0x80L;
    private static final long col2 = 0x4000L;
    private static final long col3 = 0x200000L;
    private static final long col4 = 0x10000000L;
    private static final long col5 = 0x800000000L;
    private static final long col6 = 0x40000000000L;
    private static final long[] columnBottoms = {
            col0,
            col1,
            col2,
            col3,
            col4,
            col5,
            col6
    };

    MoveSorter moveSorter;

    @BeforeEach
    void setUp() {
        moveSorter = new MoveSorter();
    }

    // Tests the fact that if we put in all ties, it will function as a stack
    @Test
    void testStableSort() {
        for (long columnBottom : columnBottoms) {
            moveSorter.add(columnBottom, 0);
        }

        for (int i = columnBottoms.length - 1; i >= 0; i--) {
            assertEquals(columnBottoms[i], moveSorter.getNext());
        }
    }

    // Here the scores are all different and in a random order
    @Test
    void testNormalSort() {
        int [] scores = {3, 2, 6, 7, 5, 4, 1};
        int [] expectedOrder = {3, 2, 4, 5, 0, 1, 6};

        for (int i = 0; i < columnBottoms.length; i++) {
            moveSorter.add(columnBottoms[i], scores[i]);
        }

        for (int i = 0; i < columnBottoms.length; i++) {
            assertEquals(columnBottoms[expectedOrder[i]], moveSorter.getNext());
        }
    }

    // Some ties, some different scores
    // Expect to sort as much as possible, then treat the ties like a stack
    @Test
    void testHybridSort() {
        int [] scores = {3, 3, 6, 7, 5, 5, 1};
        int [] expectedOrder = {3, 2, 5, 4, 1, 0, 6};

        for (int i = 0; i < columnBottoms.length; i++) {
            moveSorter.add(columnBottoms[i], scores[i]);
        }

        for (int i = 0; i < columnBottoms.length; i++) {
            assertEquals(columnBottoms[expectedOrder[i]], moveSorter.getNext());
        }
    }

    // Get next works as an iterator, which ends with 0
    @Test
    void testIteratorEnds() {
        for (long columnBottom : columnBottoms) {
            moveSorter.add(columnBottom, 0);
        }

        for (long columnBottom : columnBottoms) {
            assertTrue(moveSorter.getNext() != 0L);
        }

        assertEquals(0, moveSorter.getNext());

    }
}
