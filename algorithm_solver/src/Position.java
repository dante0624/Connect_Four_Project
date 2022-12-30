public class Position {
    // This is all a bitmap, that requires 49 total bits
    // http://blog.gamesolver.org/solving-connect-four/06-bitboard/

    // Constants of the Board
    public static final int WIDTH = 7;
    public static final int HEIGHT = 6;
    public static final int MIN_SCORE = -(WIDTH*HEIGHT)/2 + 3;
    public static final int MAX_SCORE = (WIDTH*HEIGHT+1)/2 - 3;

    // This is a bitmap with 1's in the bottom of each row
    // Need to update manually if we change WIDTH or HEIGHT
    private static final long BOTTOM_MASK = 0x40810204081L;

    // Bitmap with all 1's in each playable square
    private static final long BOARD_MASK = BOTTOM_MASK * ((1L << HEIGHT) - 1);


    // Static methods for generating masks based on a column
    // Returns a long mask for the bottom cell of a column
    private static long bottomMask(int col) {
        return 1L << col*(HEIGHT+1);
    }

    // Returns a long mask for the top (playable) cell of a column
    private static long topMask(int col) {
        return 1L << (HEIGHT - 1) << col*(HEIGHT+1);
    }

    // Returns a long mask for the entire (playable) column
    public static long colMask(int col) {
        return ((1L << HEIGHT) - 1L) << col*(HEIGHT+1);
    }

    // Returns the number of 1's in a 64 bit, long number in binary.
    private static int popcount(long num) {
        int count;
        for (count = 0; num != 0; count++) {
            num &= num - 1;
        }
        return count;
    }


    // Private attributes
    private long position;
    private long mask;
    private int movesPlayed;

    // Private interface
    // Returns a bitmap that has 1's everywhere the current player can legally play
    // Either a single 1 per column, or no 1's in a full column
    private long possible() {
        return (mask + BOTTOM_MASK) & BOARD_MASK;
    }

    // Returns a bitmap that has 1's everywhere that we can currently win
    // This means all squares where there is currently no chip, but a chip there creates connect 4
    // This square may be "floating" (meaning if we play that column the chip will fall lower)
    private static long computeWinningPosition(long position, long mask) {
        // Vertical
        long winning = (position << 1) & (position << 2) & (position << 3);

        // Horizontal
        // We will redefine pair many times, right now it means there are a pair of 1's to the left
        long pair = (position << (HEIGHT+1)) & (position << 2*(HEIGHT+1));

        // Compute three to the left, then two left and one right
        winning |= pair & (position << 3*(HEIGHT+1));
        winning |= pair & (position >> (HEIGHT+1));

        // Now, pair means there are a pair of 1's to the right
        pair = (position >> (HEIGHT+1)) & (position >> 2*(HEIGHT+1));

        // Compute three to the right, then two right and one left
        winning |= pair & (position >> 3*(HEIGHT+1));
        winning |= pair & (position << (HEIGHT+1));

        // Diagonal 1
        // Now, pair means there are a pair of 1's up-left
        pair = (position << HEIGHT) & (position << 2*HEIGHT);

        // Compute 3 up-left, then two up-left and one down-right
        winning |= pair & (position << 3*HEIGHT);
        winning |= pair & (position >> HEIGHT);

        // Now, pair means there are a pair of 1's down-right
        pair = (position >> HEIGHT) & (position >> 2*HEIGHT);

        // Compute 3 down-right, then two down-right and one up-left
        winning |= pair & (position >> 3*HEIGHT);
        winning |= pair & (position << HEIGHT);


        //diagonal 2
        // Now, pair means there are a pair of 1's down-left
        pair = (position << (HEIGHT+2)) & (position << 2*(HEIGHT+2));

        // Compute three down-left, then two down-left and one up-right
        winning |= pair & (position << 3*(HEIGHT+2));
        winning |= pair & (position >> (HEIGHT+2));

        // Now, pair means there are a pair of 1's up-right
        pair = (position >> (HEIGHT+2)) & (position >> 2*(HEIGHT+2));

        // Compute three up-right, then two up-right and one down-left
        winning |= pair & (position >> 3*(HEIGHT+2));
        winning |= pair & (position << (HEIGHT+2));

        // Need to be a winning position, and there need to not already be a chip there
        return winning & (BOARD_MASK ^ mask);
    }

    // Return a bitmap that has 1's everywhere that the opponent can currently win
    // Simply called "computeWinningPosition" with the 1's and 0's swapped
    private long opponentWinningPosition() {
        return computeWinningPosition(position ^ mask, mask);
    }
    // Same thing but for the current player
    private long winningPosition(){
        return computeWinningPosition(position, mask);
    }

    // Returns true iff the current player has "connect 4" already
    private static boolean alignment(long pos) {
        // Horizontal
        long pairs = pos & (pos >> (HEIGHT + 1)); //pairs has a 1 for all horizontal "connect 2"
        if ((pairs & (pairs >> (2*(HEIGHT + 1)))) != 0) {return true;}

        // Diagonal 1, up-left to down-right
        pairs = pos & (pos >> HEIGHT);
        if ((pairs & (pairs >> (2 * HEIGHT))) != 0) {return true;}

        // Diagonal 2, down-left to up-right
        pairs = pos & (pos >> (HEIGHT + 2));
        if ((pairs & (pairs >> (2 * (HEIGHT + 2)))) != 0) {return true;}

        // Vertical
        pairs = pos & (pos >> 1);
        return (pairs & (pairs >> 2)) != 0;
    }

    // Public interface
    // Simple getters
    public long getPosition(){
        return position;
    }
    public long getMask() {
        return mask;
    }
    public int getMovesPlayed() {
        return movesPlayed;
    }

    public long getKey(){
        // Just position + mask is  a valid, unique key
        return position + mask;
    }

    // Returns true iff it is legal to play in the indicated column number
    public boolean canPlay(int col) {
        return (mask & topMask(col)) == 0;
    }

    // Actually places a token in the respective column
    // Should never be called on a non-playable (full) column
    public void playCol(int col) {
        // First, switch the bits of current player and opposing player
        position ^= mask;

        // Move up the mask by one in the column, but do not change position
        // This has an effect of adding a 0 to the key, which we want because after playing the "colors" swap
        mask |= mask + bottomMask(col);

        // Increment the move counter
        movesPlayed++;
    }

    // move is a bitmap with a single 1 where you want the new token to be placed
    // Must check beforehand that this move is valid (not floating or taken)
    public void playMove(long move) {
        // First, switch the bits of current player and opposing player
        position ^= mask;

        // Add the move to the mask, but not to the position
        // This has an effect of adding a 0 to the key, which we want because after playing the "colors" swap
        mask |= move;

        // Increment the move counter
        movesPlayed++;
    }

    // Returns true if the player will win by playing in a current column
    // Note, if the position is already won (somehow the game didn't end) this still returns true
    public boolean isWinningMove(int col) {
        // Make a copy of the current position
        long pos = position;

        // Add a single 1 to the top of the desired column
        pos |= (mask + bottomMask(col)) & colMask(col);

        return alignment(pos);
    }

    // Return true if the currentPlayer can win on their current move
    public boolean canWinNext() {
        return (winningPosition() & possible()) != 0;
    }

    // Returns a bitmap of all the possible next moves that do not lose in one turn
    // A losing move is a move leaving the possibility for the opponent to win directly.
    // This function will not work correctly if you can win on this turn (because then that is optimal)
    public long possibleNonLosingMoves() {
        // At most a single 1 per column, containing all legally playable moves
        long possibleMoves = possible();

        // Has 1's everywhere that the opponent can create connect 4 (including "floating" squares)
        long opponentWin = opponentWinningPosition();

        // Moves are forced because we can play them right now, and the opponent can also win there
        long forcedMoves = possibleMoves & opponentWin;

        if (forcedMoves != 0) {
            // This indicates that there are multiple forced moves, so we lose
            if ((forcedMoves & (forcedMoves - 1)) != 0) {
                return 0L;
            }
            possibleMoves = forcedMoves;
        }

        // At this point, possibleMoves is one of two cases:
        // Case 1: There are no forced moves, so possibleMoves is just all playable columns
        // Case 2: There is a single forced move, so possibleMoves is that one move
        // We now want to remove any moves which "build up" and reveal a new winning move to the opponent
        return possibleMoves & ~(opponentWin >> 1);
    }

    // Returns a score of a given move
    // The move is assumed to be a bitmap with a single 1 for the new chip
    // The score is the number of total alignment possibilities, after this new chip is played
    int moveScore(long move) {
        return popcount(computeWinningPosition(move | position, mask));
    }

    // Constructors
    public Position() {
        position = 0L;
        mask = 0L;
        movesPlayed = 0;
    }
    public Position(long initialPosition, long initialMask, int initialMovesPlayed) {
        position = initialPosition;
        mask = initialMask;
        movesPlayed = initialMovesPlayed;
    }
    // This one makes a copy of a position
    public Position(Position otherPosition) {
        position = otherPosition.getPosition();
        mask = otherPosition.getMask();
        movesPlayed = otherPosition.getMovesPlayed();
    }
}
