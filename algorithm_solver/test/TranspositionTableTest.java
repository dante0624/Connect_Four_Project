import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
// We have 5 asserts we can use now
// assertEquals
// assertTrue
// assertFalse
// assertNotNull
// assertNull

public class TranspositionTableTest {
    static TranspositionTable table;

    // Create the table, and allocate the memory once
    @BeforeAll
    static void createTable() {
        table = new TranspositionTable();
    }

    // Reset the table before each test
    // And give it some simple data
    @BeforeEach
    void resetTable() {
        table.resetTable();
        table.put(0xF9F3EFCF87L, 1);
    }

    @Test
    void testPutGet() {
        assertEquals(1, table.get(0xF9F3EFCF87L));
    }

    @Test
    void testCollision() {
        // This was already in there
        assertEquals(1, table.get(0xF9F3EFCF87L));

        // This should cause a collision
        table.put(0xF8F3EFCF87L, 2);
        assertEquals(2, table.get(0xF8F3EFCF87L));

        // Assert that if we look for the old value, we get 0 because it was erased
        assertEquals(0, table.get(0xF9F3EFCF87L));
    }

    @Test
    void testReset() {
        table.resetTable();
        assertEquals(0, table.get(0xF9F3EFCF87L));
    }
}
