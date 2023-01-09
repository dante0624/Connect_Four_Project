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
    // Global information about this class
    final static String rootDir = "/Users/dante/OneDrive/Desktop/Connect_Four_Project/algorithm_solver";
    final static String resourcesFolder = "resources/transTableSerialized";
    final static int maxDepth = 1;

    public static void main(String[] args) throws IOException {
        //  By using these constructors, I still have a reference to the table and arrays
        int[] myKeys = new int[TranspositionTable.numEntries];
        byte[] myEvals = new byte[TranspositionTable.numEntries];
        TranspositionTable myTable = new TranspositionTable(myKeys, myEvals);
        Solver solver = new Solver(myTable);

        // Keep track of all unique positions at this depth
        Position blankPosition = new Position();

        ArrayList<Position> currPositions = new ArrayList<>();
        currPositions.add(blankPosition);

        for (int depth = 0; depth <= maxDepth; depth++) {
            // Prepare the objects that do the serializing
            String keysFileName = "depth"+depth+"Keys.ser";
            String evalsFileName = "depth"+depth+"Evals.ser";
            Path keysPath = Paths.get(rootDir, resourcesFolder, keysFileName);
            Path evalPath = Paths.get(rootDir, resourcesFolder, evalsFileName);

            ObjectOutputStream outKeys = new ObjectOutputStream(new FileOutputStream(keysPath.toString()));
            ObjectOutputStream outEvals = new ObjectOutputStream(new FileOutputStream(evalPath.toString()));

            // Prepare for the next depth
            ArrayList<Position> nextPositions = new ArrayList<>();
            HashSet<Long> nextPositionKeys = new HashSet<>();

            // Iterate through each position at this depth
            for (Position p : currPositions) {
                // First solve it, filling up the table
                solver.solve(p);

                // Then add all unique children for the next depth
                for (int col = 0; col < Position.WIDTH; col++) {
                    if (p.canPlay(col)) {
                        // Make a copy of the position, then play the column
                        Position pChild = new Position(p);
                        pChild.playCol(col);

                        // Only add it if it is unique (the key has not been seen yet)
                        if (!nextPositionKeys.contains(pChild.getKey())) {
                            nextPositionKeys.add(pChild.getKey());
                            nextPositions.add(pChild);
                        }
                    }
                }
            }

            // Write and close everything
            outKeys.writeObject(myKeys);
            outKeys.flush();
            outKeys.close();

            outEvals.writeObject(myEvals);
            outEvals.flush();
            outEvals.close();

            // Say that we are done
            System.out.println("Done serializing at depth " + depth);

            // Prepare for the next depth
            currPositions = nextPositions;
        }
    }
}
