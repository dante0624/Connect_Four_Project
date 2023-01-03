// Store each entry in the table as a long (64 bits, 8 bytes), within a massive array
// The upper 56 bits of this will be for the position key
// The lower 8 bits will be for the position evaluation

// Have a complete transposition table of size 64 MB, or 2^26 byes
// This means we will 2^23 entries

// We take the 23 least significant bits of the position key to determine the index within the table (hashing)
// These are bits 9-31 of each entry

import java.util.Arrays;

public class TranspositionTable {
    // Global about the table
    // Wanted 2 ^ 23 to be the real number of entries
    // Each entry is 5 bytes (4 key and 1 eval), so this would lead to a 40MB table.
    // Needed to add 9 entries more entries, so numEntries is a prime number
    // In practice, could have just added 1 so that it is odd, but prime is better
    public static final int numEntries = (1 << 23) + 9;

    // Private variables (table itself)
    // We have two large arrays, one for keys and one for evaluations.
    // They are the same size and paired up by index, so we can imagine one table of pairs
    private final int[] keys;
    private final byte[] evals;

    // Private methods
    private static int getIndex(long key) {
        // Should not get any signed bit problems
        // Keys are 49 bits, and stored as longs (64 bits) so they are always positive
        // Then we modulo but numEntries, and get a new positive number
        // At most, this positive number is (numEntries - 1)
        // Storing that largest possible number only takes 24 bits, so it will always cast to a positive int
        return (int) (key % numEntries);
    }

    // Public Methods
    public void resetTable() {
        Arrays.fill(keys, 0);
        Arrays.fill(evals, (byte) 0);
    }

    public void put(long key, int eval) {
        int index = getIndex(key);

        // Keys are always positive, so this cast really only uses 31 bits
        keys[index] = (int) key;
        evals[index] = (byte) eval;
    }

    public int get(long key) {
        int index = getIndex(key);

        if (keys[index] == (int) key) {
            return evals[index];
        }
        return 0;
    }

    // Constructors
    // Someone can pass in their own arrays, if they already know information
    // They can also use it to track the arrays after solving a position, then serialize the arrays
    TranspositionTable(int[] initialKeys, byte[] initialEvals) {
        keys = initialKeys;
        evals = initialEvals;
    }
    TranspositionTable() {
        // Java ensures that this starts off as all 0's
       this(new int[numEntries], new byte[numEntries]);
    }
}
