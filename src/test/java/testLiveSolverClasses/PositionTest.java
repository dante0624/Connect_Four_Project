package testLiveSolverClasses;

import liveSolverClasses.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    Position blankPosition;
    Position complexPosition;
    Position vertical;
    Position horizontal;
    Position diagonal1;
    Position diagonal2;
	Position drawn;
	Position connect20;

    @BeforeEach
    void setUp() {
        blankPosition = new Position();
        /* Complex Position:
			* * 0 * * * *
			* 1 0 1 0 0 *
			* 1 1 1 0 0 *
			0 1 0 0 1 0 *
			0 0 1 0 1 1 *
			1 0 0 1 1 0 *
         */
        complexPosition = new Position(0x1073228E01L, 0xF9F3EFCF87L, 29);
        vertical = new Position(0x7L, 0x387L, 6);
        horizontal = new Position(0x10204000L, 0x3060C000L, 6);
        diagonal1 = new Position(0x20A28500L, 0x30E3C781L, 14);
        diagonal2 = new Position(0x50A0A08000L, 0x478F0E0C000L, 14);
		/* Drawn Position:
		   0 0 0 1 0 0 1
		   0 1 1 0 1 1 1
		   0 0 0 1 0 0 1
		   1 1 1 0 1 1 0
		   1 0 0 1 0 0 0
		   1 1 1 0 1 1 0
		*/
		drawn = new Position(0x1DEA54D352A46L, 0x1DEA54D352A46L, 42);
		/* Connect 20:
		   0 1 0 * 0 1 *
		   0 0 1 * 1 0 0
		   1 1 1 1 1 1 1
		   0 0 1 1 1 0 0
		   0 1 0 1 0 1 0
		   1 0 0 1 0 0 1
		*/
		connect20 = new Position(0x58AA3008CAB6L, 0x7DFBF1EFDFBFL, 39);
    }

    // Tests that a position will fit into 63 (unsigned) long bits
    @Test
    void testBitboardSize() {
        // This needs to be true for the Chinese Remainder Theorem to hold in the transposition table
        // We really only store 31 bits of the key
        // We index based on numEntries, which is roughly 2^23
        // So to make the Chinese Remainder Theorem hold, the true key size must be <= (31 + 23 = 54 bits)
        assertTrue(Position.WIDTH * (Position.HEIGHT + 1) <= 54);
    }

    @Test
    void testColMask() {
        assertEquals(0x3FL, Position.colMask(0));
        assertEquals(0x7E00000L, Position.colMask(3));
    }

	@Test
	void testPriorPlayerHasWon() {
		Position vertialComplexWin = new Position(complexPosition);
		vertialComplexWin.playCol(1);
		assertTrue(vertialComplexWin.priorPlayerHasWon());

		Position horizontalComplexWin = new Position(complexPosition);
		horizontalComplexWin.playCol(0);
		assertTrue(horizontalComplexWin.priorPlayerHasWon());

		Position diagonalComplexWin1 = new Position(complexPosition);
		diagonalComplexWin1.playCol(4);
		assertTrue(diagonalComplexWin1.priorPlayerHasWon());

		Position diagonalComplexWin2 = new Position(complexPosition);
		diagonalComplexWin2.playCol(6);
		assertTrue(diagonalComplexWin2.priorPlayerHasWon());

		// Now show that neither player won the complex position prior to these moves
		assertFalse(complexPosition.priorPlayerHasWon());
		complexPosition.playCol(3);
		assertFalse(complexPosition.priorPlayerHasWon());

		// Extra check
		assertFalse(drawn.priorPlayerHasWon());
	}

	@Test
	void testPriorPlayerAlignments() {
		// Has the maximal amount of connect 3's, but no connect 4's
		int[] noAlignments = {};
		assertArrayEquals(noAlignments, drawn.priorPlayerAlignments());
		
		// Has the most amount of things connected possible
		int[] manyAlignments = {
			35, 14,
			29, 15, 1,
			23, 16, 9,
			38, 31, 24, 17,
			25, 18, 11,
			33, 19, 5,
			41, 20,
		};
		assertArrayEquals(manyAlignments, connect20.priorPlayerAlignments());
	}

    @Test
    void testGetKey() {
        assertEquals(0L, blankPosition.getKey());
        assertEquals(0x10A67125D88L, complexPosition.getKey());
    }

    @Test
    void testGetMirrorKey() {
        /* The mirrored complex position should look like:
                * * * * 0 * *
                * 0 0 1 0 1 *
                * 0 0 1 1 1 *
                * 0 1 0 0 1 0
                * 1 1 0 1 0 0
                * 0 1 1 0 0 1
         */
        Position mirrorComplex = new Position(0x4E0A321C100L, 0x1CFBF3E7CF80L, 29);

        // Proves that the positions are different (non symmetrical position)
        assertNotEquals(complexPosition.getKey(), mirrorComplex.getKey());

        // But they are actually mirrors of each other
        assertEquals(mirrorComplex.getKey(), complexPosition.getMirrorKey());
        assertEquals(complexPosition.getKey(), mirrorComplex.getMirrorKey());

        // This tests that a position which is symmetrical
        for (int i = 0; i < Position.WIDTH; i++) {
            blankPosition.playCol(i);
        }
        assertEquals(blankPosition.getKey(), blankPosition.getMirrorKey());
        blankPosition.playCol(Position.WIDTH / 2);
        assertEquals(blankPosition.getKey(), blankPosition.getMirrorKey());
    }

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
    void testPlayCol() {
        complexPosition.playCol(3);
        assertTrue(complexPosition.isWinningMove(5));
    }

    @Test
    void testPlayMove() {
        complexPosition.playMove(0x4000000L);
        assertTrue(complexPosition.isWinningMove(5));
    }

    // Test canWinNext in each possible way
    @Test
    void testCanWinNextVertical() {
        assertTrue(vertical.canWinNext());

        // block off the winning column
        vertical.playCol(1);
        vertical.playCol(0);
        assertFalse(vertical.canWinNext());
    }

    @Test
    void testCanWinNextHorizontal() {
        assertTrue(horizontal.canWinNext());

        // block off one winning column
        horizontal.playCol(3);
        horizontal.playCol(1);
        assertTrue(horizontal.canWinNext());

        // block off the other
        horizontal.playCol(1);
        horizontal.playCol(5);
        assertFalse(horizontal.canWinNext());

    }

    @Test
    void testCanWinNextD1() {
        assertTrue(diagonal1.canWinNext());

        // block off one winning column
        diagonal1.playCol(0);
        diagonal1.playCol(1);
        assertTrue(diagonal1.canWinNext());

        // block off the other
        diagonal1.playCol(6);
        diagonal1.playCol(5);
        assertFalse(diagonal1.canWinNext());
    }

    @Test
    void testCanWinNextD2() {
        assertTrue(diagonal2.canWinNext());

        // block off one winning column
        diagonal2.playCol(6);
        diagonal2.playCol(5);
        assertTrue(diagonal2.canWinNext());

        // block off the other
        diagonal2.playCol(0);
        diagonal2.playCol(1);
        assertFalse(diagonal2.canWinNext());
    }

    // Test possible NonLosing Moves in several ways
    @Test
    void testPossibleNonLosingMoves() {
        // Every Move is playable
        assertEquals(0x40810204081L, blankPosition.possibleNonLosingMoves());

        // The playable moves are simply non-full columns
        complexPosition.playCol(5);
        complexPosition.playCol(6);
        assertEquals(0x80204001008L, complexPosition.possibleNonLosingMoves());

        // The only playable move blocks an opponent's winning move
        vertical.playCol(1);
        assertEquals(0x8L, vertical.possibleNonLosingMoves());

        // Opponent can win in two spots, so we are done
        horizontal.playCol(0);
        assertEquals(0x0L, horizontal.possibleNonLosingMoves());

        // Do not build under an opponent and let them win
        horizontal.playCol(1);
        horizontal.playCol(1);
        horizontal.playCol(5);
        horizontal.playCol(5);
        horizontal.playCol(3);
        assertEquals(0x42001000202L, horizontal.possibleNonLosingMoves());

        // You play a move to block a direct win, but that builds under a different win
        horizontal.playCol(6);
        horizontal.playCol(2);
        horizontal.playCol(6);
        horizontal.playCol(1);
        assertEquals(0x0L, horizontal.possibleNonLosingMoves());

    }

    @Test
    void testMoveScore() {
        assertEquals(2, diagonal2.moveScore(0x1L));

        // Flip whose move it is by playing a useless move
        diagonal2.playMove(0x1L);

        // Check creating an alignment versus one that does not
        assertEquals(0, diagonal2.moveScore(0x2L));
        assertEquals(1, diagonal2.moveScore(0x10000L));

    }


    @Test
    void testCopy() {
        Position copy = new Position(complexPosition);
        assertEquals(complexPosition.position, copy.position);
        assertEquals(complexPosition.mask, copy.mask);
        assertEquals(complexPosition.mask, copy.mask);
    }

    @Test
    void testKeyToPosition() {
        Position copy = new Position(complexPosition.getKey(), complexPosition.movesPlayed);
        assertEquals(complexPosition.position, copy.position);
        assertEquals(complexPosition.mask, copy.mask);
        assertEquals(complexPosition.movesPlayed, copy.movesPlayed);
    }
}
