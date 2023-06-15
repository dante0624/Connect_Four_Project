package liveSolverClasses;
/* Idea is that you insert a move and its score (already determined)
As inserted, it will place this new move into its sorted position
Then you can "get" the moves one by one in order of greatest to least
In the case of a tie (all scores are equal), this will function as a stack */

public class MoveSorter {
    // Stores each entry (move and score concatenated) as a single long
    public static final int scoreBits = 8; // This number of bits represents the score.

    private final long[] entries;
    private int size;

    private int getScore(int index) {
        return (int) (entries[index] % (1 << scoreBits));
    }
    private long getMove(int index) {
        return entries[index] >> scoreBits;
    }

    // Insert a move into an already sorted array
    public void add(long move, int score) {
        // The index we are looking at (initially, the first empty index)
        int vacantIndex = size++;

        // As long as the previous entry is strictly bigger, shift that right once
        for (; (vacantIndex != 0) && getScore(vacantIndex-1) > score; --vacantIndex) {
            entries[vacantIndex] = entries[vacantIndex-1];
        }

        // Place our entry at the now empty spot
        entries[vacantIndex] = (move << scoreBits) + score;
    }

    public long getNext() {
        if (size != 0) {
            return getMove(--size);
        }

        // This 0 marks the end of the iteration
        return 0L;
    }

    public MoveSorter() {
        entries = new long[Position.WIDTH];
        size = 0;
    }
}
