// Store each entry in the table as a long (64 bits, 8 bytes), within a massive array
// The upper 56 bits of this will be for the position key
// The lower 8 bits will be for the position evaluation

// Have a complete transposition table of size 64 MB, or 2^26 byes
// This means we will 2^23 entries

// We take the 23 least significant bits of the position key to determine the index within the table (hashing)
// These are bits 9-31 of each entry

import java.util.Arrays;

public class TranspositionTable {
    // Globals about the table
    public static final int evalBits = 8; // Each entry has its lower bits represent the position evaluation
    public static final int tableSize = 26; // Implied is 2 ^ tableSize number of bytes
    public static final int entrySize = 3; // Implied that it is 2 ^ entrySize, bytes per entry
    public static final int numEntries = tableSize - entrySize; // 2 ^ numEntries is the real number of entries


    // Private variable (table itself)
    private final long[] table;

    // Private methods
    private static long constructEntry(long key, int eval) {
        return (key << evalBits) + eval;
    }

    private static int getIndex(long key) {
        return (int) (key % (1 << numEntries));
    }

    private static int entryToEval(long entry) {
        return (int) (entry % (1 << evalBits));
    }

    private static long entryToKey(long entry) {
        return entry >> evalBits;
    }

    // Public Methods
    public void resetTable() {
        Arrays.fill(table, 0L);
    }

    public void put(long key, int eval) {
        table[getIndex(key)] = constructEntry(key, eval);
    }

    public int get(long key) {
        long entry = table[getIndex(key)];
        long entryKey = entryToKey(entry);
        int eval = entryToEval(entry);

        // Make sure that the keys actually line up, and this isn't a collision
        if (entryKey == key) {
            return eval;
        }
        else {
            return 0;
        }

    }

    // Constructor
    TranspositionTable() {
        // Java ensures that this starts off as all 0's
        table = new long[1 << numEntries];
    }
}
