import java.nio.file.Paths;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Path;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
// We have 5 asserts we can use now
// assertEquals
// assertTrue
// assertFalse
// assertNotNull
// assertNull

public class BeginMediumTest {
    // Global information about this test
    final static String rootDir = "/Users/dante/OneDrive/Desktop/Connect_Four_Project/algorithm_solver";
    final static String resourcesFolder = "testResources";
    final static String testFile = "beginMediumTests.txt";
    final static int numTests = 5;


    Position position;
    static Scanner fileStream;
    static Solver solver;

    // Open the file
    @BeforeAll
    static void openFile() throws FileNotFoundException {
        try {
            Path testCasesPath = Paths.get(rootDir, resourcesFolder, testFile);
            File fileIn = new File(testCasesPath.toString());
            fileStream = new Scanner(fileIn);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }

        solver = new Solver();
    }

    // Reset the position before each test
    @BeforeEach
    void resetPosition()  {
        position = new Position();

    }

    // Test each example position
    @RepeatedTest(numTests)
    void testSolve() {
        String[] line = fileStream.nextLine().split(" ");
        String columns = line[0];
        int evalExpected = Integer.parseInt(line[1]);

        // Play all the column moves
        for (int i = 0; i < columns.length(); i++) {
            int col = Integer.parseInt(columns.substring(i, i+1));
            position.playCol(col);
        }

        assertEquals(evalExpected, solver.solve(position));
    }

    // Close the file
    @AfterAll
    static void closeFile() {
        fileStream.close();
    }
}
