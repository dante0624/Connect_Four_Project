public class Position {
    // This is all a bitmap, that requires 49 total bits
    // http://blog.gamesolver.org/solving-connect-four/06-bitboard/

    // Constants of the Board
    public static final int WIDTH = 7;
    public static final int HEIGHT = 6;
    public static final int MIN_SCORE = -(WIDTH*HEIGHT)/2 + 3;
    public static final int MAX_SCORE = (WIDTH*HEIGHT+1)/2 - 3;

    // Private attributes
    private long position;
    private long mask;
    private int movesPlayed;

    // Private interface

    // Returns a long mask for the bottom cell of a column
    private long bottomMask(int col) {
        return 1L << col*(HEIGHT+1);
    }

    // Returns a long mask for the top (playable) cell of a column
    private long topMask(int col) {
        return 1L << (HEIGHT - 1) << col*(HEIGHT+1);
    }

    // Returns a long mask for the entire (playable) column
    private long colMask(int col) {
        return ((1L << HEIGHT) - 1L) << col*(HEIGHT+1);
    }

    // Returns true iff the current player has "connect 4" already
    private static boolean alignment(long pos) {
        // Horizontal
        long pairs = pos & (pos >> (HEIGHT + 1)); //pairs has a 1 for all horizontal "connect 2"
        if ((pairs & (pairs >> (2*(HEIGHT + 1)))) != 0) {return true;}

        // Diagonal 1
        pairs = pos & (pos >> HEIGHT);
        if ((pairs & (pairs >> (2 * HEIGHT))) != 0) {return true;}

        // Diagonal 2
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
    public void play(int col) {
        // First, switch the bits of current player and opposing player
        position ^= mask;

        // Move up the mask by one in the column, but do not change position
        // This has an effect of adding a 0 to the key, which we want because after playing the "colors" swap
        mask |= mask + bottomMask(col);

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
