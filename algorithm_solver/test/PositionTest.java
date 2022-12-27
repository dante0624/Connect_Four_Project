import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
// We have 5 asserts we can use now
// assertEquals
// assertTrue
// assertFalse
// assertNotNull
// assertNull

class PositionTest {
    Position blankPosition;
    Position complexPosition;

    @BeforeEach
    void setUp() {
        blankPosition = new Position();
        complexPosition = new Position(
                0x1073228E01L,
                0xF9F3EFCF87L,
                29
        );
    }

    // Tests that a position will fit into 63 (unsigned) long bits
    @Test
    void testBitboardSize() {
        // These needs to be true so that the position keys can go nicely into the transposition table
        assertTrue(Position.WIDTH * (Position.HEIGHT + 1) <= 56);
    }

    // Some simple "Getters First"
    @Test
    void testGetPosition() {
        assertEquals(0L, blankPosition.getPosition());
        assertEquals(0x1073228E01L, complexPosition.getPosition());
    }

    @Test
    void testGetMask() {
        assertEquals(0L, blankPosition.getMask());
        assertEquals(0xF9F3EFCF87L, complexPosition.getMask());
    }

    @Test
    void testGetMovesPlayed() {
        assertEquals(0, blankPosition.getMovesPlayed());
        assertEquals(29, complexPosition.getMovesPlayed());
    }

    @Test
    void testGetKey() {
        assertEquals(0L, blankPosition.getKey());
        assertEquals(0x10A67125D88L, complexPosition.getKey());
    }

    // Testing Can Play
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6 })
    void testCanPlayBlank(int col) {
        assertTrue(blankPosition.canPlay(col));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 3, 4, 5, 6 })
    void testCanPlayComplex(int col) {
        assertTrue(complexPosition.canPlay(col));
    }

    @Test
    void testCannotPlay() {
        assertFalse(complexPosition.canPlay(2));
    }

    // Testing is Winning Move, which implicitly tests many things at once
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 4, 6 })
    void testIsWinningMoveComplex(int col) {
        assertTrue(complexPosition.isWinningMove(col));
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 5 })
    void testIsNotWinningMoveComplex(int col) {
        assertFalse(complexPosition.isWinningMove(col));
    }


    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6 })
    void testIsNotWinningMoveBlank(int col) {
        assertFalse(blankPosition.isWinningMove(col));
    }

    // Tests that if we play, then we swap colors as well and now 0 is winning
    @Test
    void testPlay() {
        complexPosition.play(3);
        assertTrue(complexPosition.isWinningMove(5));
    }

    // Tests the constructor which makes a copy of another position
    @Test
    void testCopy() {
        Position copy = new Position(complexPosition);
        assertEquals(complexPosition.getPosition(), copy.getPosition());
        assertEquals(complexPosition.getMask(), copy.getMask());
        assertEquals(complexPosition.getMovesPlayed(), copy.getMovesPlayed());
    }
}
