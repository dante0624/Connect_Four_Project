package liveSolverClasses;// Idea is that you insert a move and its score (already determined)
// As inserted, it will place this new move into its sorted position

// Then you can "get" the moves one by one in order of greatest to least
// So it is an iterator

// In the case of a tie (all scores are equal), this will function as a stack
// The first in is always the last out

public class MoveSorter {
    // Stores each entry (move and score) simply as a single long

    public static final int scoreBits = 8; // Each entry has its lower bits represent the score.

    // Private variables
    private final long[] entries;

    // Number of entries in the sorter
    private int size;

    // Private methods for getting each piece of an entry by index
    private int getScore(int index) {
        return (int) (entries[index] % (1 << scoreBits));
    }
    private long getMove(int index) {
        return entries[index] >> scoreBits;
    }

    // Public interface
    // This method inserts a move into an already sorted array
    public void add(long move, int score) {
        // pos is the index we are looking at (initially, the first empty index)
        int pos = size++;

        // As long as the previous entry is strictly bigger, shift that right once
        for (; (pos != 0) && getScore(pos-1) > score; --pos) {
            entries[pos] = entries[pos-1];
        }

        // Place our entry at the now empty spot
        entries[pos] = (move << scoreBits) + score;
    }

    public long getNext() {
        if (size != 0) {
            return getMove(--size);
        }

        // This 0 marks the end of the iteration
        return 0L;
    }

    // Constructor
    public MoveSorter() {
        entries = new long[Position.WIDTH];
        size = 0;
    }
}
