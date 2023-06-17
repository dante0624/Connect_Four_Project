package serializationHelpers;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;
import liveSolverClasses.TranspositionTable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

// Serializes a transposition table after solving several positions up to a max depth
public class TransTableSerializer {
    final static int maxDepth = 1;

    public static void main(String[] args) throws IOException {
        //  By using these constructors, I still have a reference to the table and arrays
        int[] myKeys = new int[TranspositionTable.numEntries];
        byte[] myEvals = new byte[TranspositionTable.numEntries];
        TranspositionTable myTable = new TranspositionTable(myKeys, myEvals);
        Solver solver = new Solver(myTable);

        Position blankPosition = new Position();

        ArrayList<Position> currPositions = new ArrayList<>();
        currPositions.add(blankPosition);

        for (int depth = 0; depth <= maxDepth; depth++) {
            String keysFileName = "depth"+depth+"Keys.ser";
            String evalsFileName = "depth"+depth+"Evals.ser";
            Path keysPath = Paths.get(Utils.getProjectRoot(), Utils.tableResources, keysFileName);
            Path evalPath = Paths.get(Utils.getProjectRoot(), Utils.tableResources, evalsFileName);

            ArrayList<Position> nextPositions = new ArrayList<>();
            HashSet<Long> nextPositionKeys = new HashSet<>();

            for (Position position : currPositions) {
                // First solve it, filling up the table
                solver.solve(position);

                for (int col = 0; col < Position.WIDTH; col++) {
                    if (!position.canPlay(col) || position.isWinningMove(col)) {
						continue;
					}
					Position nextPosition = new Position(position);
					nextPosition.playCol(col);

					if (nextPositionKeys.contains(nextPosition.getKey())) {
						continue;
					}

					nextPositionKeys.add(nextPosition.getKey());
					nextPositions.add(nextPosition);
                }
            }

            ObjectOutputStream outKeys = new ObjectOutputStream(new FileOutputStream(keysPath.toString()));
            outKeys.writeObject(myKeys);
            outKeys.flush();
            outKeys.close();

            ObjectOutputStream outEvals = new ObjectOutputStream(new FileOutputStream(evalPath.toString()));
            outEvals.writeObject(myEvals);
            outEvals.flush();
            outEvals.close();

            System.out.println("Done serializing at depth " + depth);

            // Prepare for the next depth
            currPositions = nextPositions;
        }
    }
}
