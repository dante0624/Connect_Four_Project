public class Solver {
    // Evaluates the score a position using alpha beta, algorithm.
    // But, it assumes a null window, so beta == alpha + 1

    // Assumptions:
        // Never called on a full board
            // If solver.solve() is called on full board, instantly returns 0
            // If solver.solve() is called with 41 moves played,
            // then nullWindow already returns 0 because min = max = 0.
            // We will prune and return 0.

        // No one has already won the game (needs to be checked beforehand)
        // The current player cannot simply win in one move (is checked
        //  beforehand by public method "solve")

    // Return Values:
        // if actual score of position <= alpha, then actual score <= return value <= alpha
        // if actual score of position >= beta, then beta <= return value <= actual score
        // if alpha <= actual score <= beta, then return value = actual score
    private static int nullWindow(
            Position p,
            int alpha,
            int [] columnOrder,
            TranspositionTable table
    ) {
        // Consider all legal moves which don't let the opponent immediately win
        long nonLosing = p.possibleNonLosingMoves();

        if (nonLosing == 0L) { // The opponent can immediately win next move, no matter what we do
            return -(Position.WIDTH * Position.HEIGHT - p.getMovesPlayed()) / 2;
        }

        // Negamax assumption that we can't win on the next move, consider the next best case
        // The best (max) case is that we win in 3 turns (player, opponent, back to player)
        int max = (Position.WIDTH * Position.HEIGHT - 1 - p.getMovesPlayed()) / 2;

        // Opponent cannot win on their next move, consider the next worst case
        // This is where, no matter what we do, they will win in 4 moves (us, them, us, them)
        int min = -(Position.WIDTH * Position.HEIGHT - 2 - p.getMovesPlayed()) / 2;

        // Go to the transposition table for potentially tighter upper or lower bounds
        // We store upper bound evals as (eval - Position.MIN_SCORE + 1)
        // We store lower bound evals as (eval + Position.MAX_SCORE - 2*Position.MIN_SCORE + 2)
        // We to this so that there is no overlap between these (we can distinguish what was stored)
        // Also, the lowest possible upper bound is 1, so a stored value of 0 is null data
        int tableVal = table.get(p.getKey());
        int bound;

        if (tableVal != 0) {
            // stored upper bound
            if (tableVal < Position.MAX_SCORE - Position.MIN_SCORE + 2) {
                bound = tableVal + Position.MIN_SCORE - 1;

                // We found a tighter, upper bound
                if (bound < max) {max = bound;}
            }

            // stored lower bound
            else {
                bound = tableVal + 2*Position.MIN_SCORE - Position.MAX_SCORE - 2;

                // We found a tighter, lower bound
                if (bound > min) {min = bound;}
            }
        }

        // Compare window [alpha, alpha + 1] to this max
        if (alpha >= max) {
            return max;
        }

        // Compare window [alpha, alpha + 1] to this min
        if (alpha < min) {
            return min;
        }

        // Prepare for the main recursion with a move sorter
        MoveSorter moveSorter = new MoveSorter();

        // Iterate through each column in columnOrder in reverse order, adding to the sorter
        // We go through in reverse order because MoveSorter is a stack in the case of ties
        // We also only add moves that are legally playable, and non-losing
        for (int i = Position.WIDTH - 1; i >= 0; i--) {
            long move = nonLosing & Position.colMask(columnOrder[i]);
            if (move != 0L) {
                moveSorter.add(move, p.moveScore(move));
            }
        }

        // Iterate through each move in the MoveSorter
        // This is the main recursion
        for (long move = moveSorter.getNext(); move != 0L; move = moveSorter.getNext()) {
            // Make a copy of the position, then play a move, and look at from other player's POV
            Position p2 = new Position(p);
            p2.playMove(move);

            int score = -nullWindow(p2, -(alpha + 1), columnOrder, table); // The awesome recursion
            if (score > alpha) { // This is a pruning case

                // This score is a lower bound of the true score (other children could beat this score)
                table.put(p.getKey(), score + Position.MAX_SCORE - 2 * Position.MIN_SCORE + 2);
                return score; // We are returning a score >= beta
            }
        }

        // This alpha is now either the best of all children nodes (none were >= beta)
        // But it can also just be the initial value of alpha (implying that all children nodes were < alpha)
        // So, this alpha is really an upper bound of the true evaluation of the position
        table.put(p.getKey(), alpha - Position.MIN_SCORE + 1);
        return alpha;
    }

    public static int solve(Position p) {
        // Check if we can win in one move on this turn, as Negamax will now assume that we cannot
        if (p.canWinNext()) {
            return (Position.WIDTH * Position.HEIGHT + 1 - p.getMovesPlayed()) / 2;
        }

        // This tells us which order to explore the columns in
        int[] columnOrder = new int[Position.WIDTH];
        for (int i = 0; i < Position.WIDTH; i++) {
            // Start in the middle and move out
            columnOrder[i] = Position.WIDTH/2 + (1-2*(i%2)) * (i+1)/2;
        }

        // Create a new Transposition table
        TranspositionTable table = new TranspositionTable();

        // Use Negamax, iterative deepening, and null window search
        // Comparable to using binary search, where we are searching for the true position score
        int min = -(Position.WIDTH*Position.HEIGHT - p.getMovesPlayed()) / 2;
        int max = (Position.WIDTH*Position.HEIGHT - p.getMovesPlayed() + 1) / 2;

        while (min < max) {
            // This is the true middle value between max and min
            // We do this instead of (min + max) / 2, because we want the floor division to always
            // round down to -inf, instead of towards 0.
            // This is relevant if say min = -2, and max = -1.
            int middle = min + (max - min) / 2;

            // Problem is, if max = 21 and min = -21, then middle = 0
            // We don't want to use this value in Negamax, because it will involve looking deeply
            // We want to find quick winning paths or quick loosing paths
            // So, if this happens, we set middle = max /2, or middle = min / 2
            if (middle <= 0 && min / 2 < middle) {
                middle = min / 2;
            }
            else if (middle >= 0 && max / 2 > middle) {
                middle = max / 2;
            }

            int result = nullWindow(
                    p,
                    middle,
                    columnOrder,
                    table
            );

            // This result tells us if the true position score is <= or >= middle
            if (result <= middle) {
                max = result;
            }
            else {
                min = result;
            }
        }
        // Loop ends when min = max = true score.
        return min;
    }
}
