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
    // Global information about this class
    final static String resourcesFolder = "resources/transTableSerialized";
    final static int maxDepth = 1;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        byte[] serializedEvals;
        String rootDir = System.getProperty("user.dir");

        for (int depth = 0; depth <= maxDepth; depth++) {
            // Prepare the object that reads the serialized data
            String evalsFileName = "depth"+depth+"Evals.ser";
            Path evalPath = Paths.get(rootDir, resourcesFolder, evalsFileName);

            ObjectInputStream inEvals = new ObjectInputStream(new FileInputStream(evalPath.toString()));

            // Do the reading
            serializedEvals = (byte[]) inEvals.readObject();

            // Close stream
            inEvals.close();

            // Count how many evals are nonzero
            int count = 0;
            for (byte eval: serializedEvals) {
                if (eval != 0) {
                    count++;
                }
            }

            // Report our findings
            System.out.println("At a depth of " + depth + ":");
            System.out.println("" + count + " of " + TranspositionTable.numEntries + " possible entries are used");
            System.out.print("This is a percent utilization of ");
            System.out.println("" + 100*(float)count/(float) TranspositionTable.numEntries + "%");

            // Prove it by outputting 10 "random evals"
            int randomStart = 38854;
            for (int i = 0; i < 10; i++) {
                System.out.println(serializedEvals[randomStart + i]);
            }

            // Make the output nicely spaced between each depth
            if (depth != maxDepth) {
                System.out.println();
            }
        }
    }
}
