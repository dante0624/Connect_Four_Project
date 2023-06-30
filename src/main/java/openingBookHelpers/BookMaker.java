package openingBookHelpers;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;
import miscHelpers.Utils;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.HashSet;

public class BookMaker {
    private static File findFile(int depth) {
        String fileName = "depth" + depth + "Book.bin";
        return Paths.get(Utils.getProjectRoot(), Utils.bookResources, fileName).toFile();
    }

    // Takes the desired depth as a command line argument
    public static void main(String[] args) throws IOException {
        assert args.length == 1;
        int depth = Integer.parseInt(args[0]);

        long startTime = System.currentTimeMillis();

        Solver solver = new Solver();
        AVLTreeWriter treeWriter = new AVLTreeWriter();

        // Depth = 0 is a special case because there is no prior file
        if (depth == 0) {
            Position blankPosition = new Position();
            int eval = solver.solve(blankPosition);
            treeWriter.insertNode(blankPosition.getKey(), (byte) eval);
        }

		// We have depth > 0, so we need to read from the previous file
        else {
            TreeReader reader = new TreeReader(findFile(depth - 1));
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

		FileOutputStream out = new FileOutputStream(findFile(depth));
        treeWriter.writeContent(out);
        out.close();

        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time in Milliseconds: " + (endTime-startTime));
    }
}
