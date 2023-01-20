package openingBookHelpers;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class BookMaker {
    // Path to the folder of all opening books
    final static String resourcesFolder = "resources/openingBook";
    static String rootDir;

    // This only opens the output stream
    private static FileOutputStream openStream(int depth) throws FileNotFoundException {
        String fileName = "depth" + depth + "Book.bin";
        Path filePath = Paths.get(rootDir, resourcesFolder, fileName);
        return new FileOutputStream(filePath.toString());
    }

    // Takes the desired depth as a command line argument
    public static void main(String[] args) throws IOException {
        // Prepare to write the output to the correct place
        rootDir = System.getProperty("user.dir");

        // Make sure the user directory is actually where we think it is (project root)
        Path pathToRoot = Paths.get(rootDir);
        assert "algorithm_solver".equals(pathToRoot.getFileName().toString());

        // Get the desired depth
        assert args.length == 1;
        int depth = Integer.parseInt(args[0]);

        // Start timing the entire process
        long startTime = System.currentTimeMillis();

        // Important classes
        Solver solver = new Solver();
        AVLTreeWriter treeWriter = new AVLTreeWriter();
        FileOutputStream out = openStream(depth);

        // Depth = 0 is a special case because there is no prior file
        if (depth == 0) {
            Position blankPosition = new Position();
            int eval = solver.solve(blankPosition);
            treeWriter.insertNode(blankPosition.getKey(), (byte) eval);
        }

        else {
            // We have depth > 0, so we need to read from the previous file
            String fileName = "depth" + (depth - 1) + "Book.bin";
            Path filePath = Paths.get(rootDir, resourcesFolder, fileName);
            TreeReader reader = new TreeReader(filePath.toString());

            // Keep track of all keys we have already solved at this depth
            HashSet<Long> solvedKeys = new HashSet<>();

            for (long key: reader) {
                // Need to turn keys into positions
                Position oldPosition = new Position(key, depth - 1);

                // Play all possible columns
                for (int col = 0; col < Position.WIDTH; col++) {
                    // Copy the position before playing the new move
                    if (oldPosition.canPlay(col) && !oldPosition.isWinningMove(col)) {
                        Position p = new Position(oldPosition);
                        p.playCol(col);

                        // Can already be in this hashmap due to transposition, or solving the mirror case
                        if (!solvedKeys.contains(p.getKey())) {
                            int eval = solver.solve(p);
                            treeWriter.insertNode(p.getKey(), (byte) eval);
                            solvedKeys.add(p.getKey());

                            // If the position is not symmetrical, add the mirror
                            if (p.getKey() != p.getMirrorKey()) {
                                treeWriter.insertNode(p.getMirrorKey(), (byte) eval);
                                solvedKeys.add(p.getMirrorKey());
                            }
                        }
                    }
                }
            }
        }

        // Write the result
        treeWriter.writeContent(out);

        // Close just the output stream
        out.close();

        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in Milliseconds: " + (endTime-startTime));
    }
}
