package testTiming;

import java.nio.file.Paths;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Path;
import java.io.FileNotFoundException;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;
import serializationHelpers.Utils;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class BeginMediumTest {
    final static String testFile = "beginMediumTests.txt";
    final static int numTests = 1000;


    Position position;
    static Scanner fileStream;
    static Solver solver;

    @BeforeAll
    static void openFile() throws FileNotFoundException {
        try {
            Path testCasesPath = Paths.get(Utils.getProjectRoot(), Utils.testResources, testFile);
            File fileIn = new File(testCasesPath.toString());
            fileStream = new Scanner(fileIn);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }

        solver = new Solver();
    }

    @BeforeEach
    void resetPosition()  {
        position = new Position();

    }

	@Tag("timing")
    @RepeatedTest(numTests)
    void testSolve() {
        String[] line = fileStream.nextLine().split(" ");
        String columns = line[0];
        int evalExpected = Integer.parseInt(line[1]);

        for (int i = 0; i < columns.length(); i++) {
            int col = Integer.parseInt(columns.substring(i, i+1));
            position.playCol(col);
        }

        assertEquals(evalExpected, solver.solve(position));
    }

    @AfterAll
    static void closeFile() {
        fileStream.close();
    }
}
