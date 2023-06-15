package openingBookHelpers;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class BookMaker {
    final static String resourcesFolder = "src/main/resources/openingBook";
    static String rootDir;

    // This only opens the output stream
    private static FileOutputStream openStream(int depth) throws FileNotFoundException {
        String fileName = "depth" + depth + "Book.bin";
        Path filePath = Paths.get(rootDir, resourcesFolder, fileName);
        return new FileOutputStream(filePath.toString());
    }

    // Takes the desired depth as a command line argument
    public static void main(String[] args) throws IOException {
        rootDir = System.getProperty("user.dir");
        Path pathToRoot = Paths.get(rootDir);
        assert "algorithm_solver".equals(pathToRoot.getFileName().toString());

        assert args.length == 1;
        int depth = Integer.parseInt(args[0]);

        long startTime = System.currentTimeMillis();

        Solver solver = new Solver();
        AVLTreeWriter treeWriter = new AVLTreeWriter();
        FileOutputStream out = openStream(depth);

        // Depth = 0 is a special case because there is no prior file
        if (depth == 0) {
            Position blankPosition = new Position();
            int eval = solver.solve(blankPosition);
            treeWriter.insertNode(blankPosition.getKey(), (byte) eval);
        }

		// We have depth > 0, so we need to read from the previous file
        else {
            String fileName = "depth" + (depth - 1) + "Book.bin";
            Path filePath = Paths.get(rootDir, resourcesFolder, fileName);
            TreeReader reader = new TreeReader(filePath.toFile());
            HashSet<Long> solvedKeys = new HashSet<>();

            for (long key: reader) {
                Position priorPosition = new Position(key, depth - 1);

                for (int col = 0; col < Position.WIDTH; col++) {
					// We do not store illegal or winning positions in the book
                    if (!priorPosition.canPlay(col) || priorPosition.isWinningMove(col)) {
						continue;
					}

					Position position = new Position(priorPosition);
					position.playCol(col);

					// Can already be in this hashmap due to transposition, or solving the mirror case
					if (solvedKeys.contains(position.getKey())) {
						continue;
					}

					int eval = solver.solve(position);
					treeWriter.insertNode(position.getKey(), (byte) eval);
					solvedKeys.add(position.getKey());

					if (position.getKey() != position.getMirrorKey()) {
						treeWriter.insertNode(position.getMirrorKey(), (byte) eval);
						solvedKeys.add(position.getMirrorKey());
					}
                }
            }
        }

        treeWriter.writeContent(out);
        out.close();

        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in Milliseconds: " + (endTime-startTime));
    }
}
