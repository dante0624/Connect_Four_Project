public class Solver {
    private static int negamax(Position p, int alpha, int beta) {
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
        if (beta > max) {
            beta = max; // No need to have an upper bound that we can't ever hit anyway
            if (alpha >= beta) { // prune if the [alpha, beta] window is null
                return beta;
            }
        }

        // The main recursion
        for (int col = 0; col < Position.WIDTH; col++) {
            if (p.canPlay(col)) {
                // Make a copy of the position, then play a move, and look at from other player's POV
                Position p2 = new Position(p);
                p2.play(col);

                int score = -negamax(p2, -beta, -alpha); // The awesome recursion
                if (score >= beta) { // This is a pruning case
                    return score;
                }
                if (score > alpha) { // alpha is now going to function as a running best
                    alpha = score;
                }
            }
        }
        // Return alpha, the current best of all move options
        return alpha;
    }

    public static int solve(Position p) {
        // Use negamax when applicable, with a starting window of [-inf, inf]
        return negamax(
                p,
                -Position.WIDTH*Position.HEIGHT/2,
                Position.WIDTH*Position.HEIGHT/2
        );
    }
}
