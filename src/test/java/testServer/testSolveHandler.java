package testServer;

import miscHelpers.Utils;
import server.SolveHandler;

import java.nio.file.Paths;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Path;
import java.io.*;

import org.junit.jupiter.api.*;

import liveSolverClasses.Position;

import static org.junit.jupiter.api.Assertions.*;

public class testSolveHandler {
    final static String[] testFiles = {
		"beginEasyTests.txt", 
		"beginMediumTests.txt", 
		"beginHardTests.txt", 
		"solverTests.txt",
		"myMixedSolveTests.txt",
	};
	// Cap at this number of tests to keep the build time reasonable
	final static int maxTestsPerFile = 30;

    static Scanner[] fileStreams = new Scanner[testFiles.length];
	static SolveHandler solveHandler;
	static Position position;

	static void playMoves(Position position, String moves) {
		for (char move : moves.toCharArray()) {
			position.playCol(Character.getNumericValue(move));
		}
	}
	
    @BeforeAll
    static void openFiles() throws FileNotFoundException, IOException, ClassNotFoundException {
		for (int i = 0; i < testFiles.length; i++) {
			String testFile = testFiles[i];
			try {
				Path testCasesPath = Paths.get(Utils.getProjectRoot(), Utils.testResources, testFile);
				File fileIn = new File(testCasesPath.toString());
				fileStreams[i] = new Scanner(fileIn);
			} catch (FileNotFoundException e) {
				throw new FileNotFoundException(e.toString());
			}
		}

        solveHandler = new SolveHandler();
    }

	@Test
    void testMixedSolve() throws IOException {
		for (Scanner fileStream : fileStreams) {
			int fileTestCount = 0;
			while (fileStream.hasNextLine() && fileTestCount < maxTestsPerFile) {
				String[] line = fileStream.nextLine().split(" ");
				String moves = line[0];
				int evalExpected = Integer.parseInt(line[1]);

				position = new Position();
				playMoves(position, moves);
				assertEquals(evalExpected, solveHandler.mixedSolve(position));

				fileTestCount++;
			}
		}
    }

    @AfterAll
    static void closeFiles() {
		for (Scanner fileStream : fileStreams) {
			fileStream.close();
		}
    }
	
}
