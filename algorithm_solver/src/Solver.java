public class Solver {
    private static int negamax(
            Position p,
            int alpha,
            int beta,
            int [] columnOrder,
            TranspositionTable table
    ) {
        // if actual score of position <= alpha, then actual score <= return value <= alpha
        // if actual score of position >= beta, then beta <= return value <= actual score
        // if alpha <= actual score <= beta, then return value = actual score

        // First check if the game is drawn
        if (p.getMovesPlayed() == Position.WIDTH * Position.HEIGHT) {
            return 0;
        }

        // Next, check if we can win on the very next move
        for (int col = 0; col < Position.WIDTH; col++) {
            if (p.canPlay(col) && p.isWinningMove(col)) {
                return (Position.WIDTH * Position.HEIGHT + 1 - p.getMovesPlayed()) / 2;
            }
        }

        // Now that we can't win on the next move, consider the next best case
        // The best (max) case is that we win in 3 turns (player, opponent, back to player)
        int max = (Position.WIDTH * Position.HEIGHT - 1 - p.getMovesPlayed()) / 2;

        // We also consult the transposition table to see if it can give a tighter, upper bound
        int tableVal = table.get(p.getKey());
        if (tableVal != 0) {
            max = tableVal + Position.MIN_SCORE - 1;
        }

        // Compare beta to this maximum
        if (beta > max) {
            beta = max; // No need to have an upper bound that we can't ever hit anyway
            if (alpha >= beta) { // prune if the [alpha, beta] window is null
                return beta; // We are returning a score <= alpha
            }
        }

        // The main recursion
        for (int col : columnOrder) {
            if (p.canPlay(col)) {
                // Make a copy of the position, then play a move, and look at from other player's POV
                Position p2 = new Position(p);
                p2.play(col);

                int score = -negamax(p2, -beta, -alpha, columnOrder, table); // The awesome recursion
                if (score >= beta) { // This is a pruning case
                    return score; // We are returning a score >= beta
                }
                if (score > alpha) { // alpha is now going to function as a running best
                    alpha = score;
                }
            }
        }
        // This alpha is now either the best of all children nodes (none were >= beta)
        // But it can also just be the initial value of alpha (implying that all children nodes were < alpha)
        // So, this alpha is really an upper bound of the true evaluation of the position
        table.put(p.getKey(), alpha - Position.MIN_SCORE + 1);
        return alpha;
    }

    public static int solve(Position p) {
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

            int result = negamax(
                    p,
                    middle,
                    middle+1,
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
