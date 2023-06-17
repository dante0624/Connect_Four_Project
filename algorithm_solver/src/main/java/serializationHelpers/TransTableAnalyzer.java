package serializationHelpers;

import liveSolverClasses.TranspositionTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.nio.file.Path;

// Analyzes serialized transposition tables up to a max depth for percent utilization
// Only really need to look at the evals, because this will be zero if unused and nonzero if used.
public class TransTableAnalyzer {
    final static int maxDepth = 1;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        byte[] serializedEvals;

        for (int depth = 0; depth <= maxDepth; depth++) {
            String evalsFileName = "depth"+depth+"Evals.ser";
            Path evalPath = Paths.get(Utils.getProjectRoot(), Utils.tableResources, evalsFileName);

            ObjectInputStream inEvals = new ObjectInputStream(new FileInputStream(evalPath.toString()));
            serializedEvals = (byte[]) inEvals.readObject();
            inEvals.close();

            int nonZeroCount = 0;
            for (byte eval: serializedEvals) {
                if (eval != 0) {
                    nonZeroCount++;
                }
            }

            System.out.println("At a depth of " + depth + ":");
            System.out.println("" + nonZeroCount + " of " + TranspositionTable.numEntries + " possible entries are used");
            System.out.print("This is a percent utilization of ");
            System.out.println("" + 100*(float)nonZeroCount/(float) TranspositionTable.numEntries + "%");

            if (depth != maxDepth) {
                System.out.println();
            }
        }
    }
}
