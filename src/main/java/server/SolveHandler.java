package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import liveSolverClasses.Position;
import liveSolverClasses.Solver;
import liveSolverClasses.TranspositionTable;
import miscHelpers.Utils;
import openingBookHelpers.TreeReader;

public class SolveHandler implements HttpHandler {
	private Solver solver;
	private int maxBookDepth;
	private TreeReader[] treeReaders;

	public int mixedSolve(Position position) throws IOException {
		int depth = position.movesPlayed;

		if (position.priorPlayerHasWon()) {
			return (depth - Position.WIDTH * Position.HEIGHT - 2) / 2;
		}
		if (depth <= maxBookDepth) {
			return treeReaders[depth].get(position.getKey());
		}

		return solver.solve(position);
	}

	public void handle(HttpExchange httpExchange) throws IOException {
		Server.readRequest(httpExchange);

		// Ignores queries (?), fragments (#), and the path beginning with /solve/
		String moves = httpExchange.getRequestURI().getPath().substring(7);
		Position position = new Position();
		for (char move : moves.toCharArray()) {
			position.playCol(Character.getNumericValue(move));
		}

		int evaluation = mixedSolve(position);

		if (position.movesPlayed % 2 == 1) {
			evaluation = 0 - evaluation; // Now it is absolute, ie always based on RED's pov
		}
		
		if (evaluation > 0) {
			evaluation = Position.MAX_SCORE + 4 - evaluation;
		}
		if (evaluation < 0) {
			evaluation = Position.MIN_SCORE - 4 - evaluation;
		}

		Server.writeResponse(httpExchange, String.valueOf(evaluation));
		System.out.println("SolveHandler solved " + moves);
		System.out.println("SolveHandler sent an evaluation of " + evaluation);
	}

	public SolveHandler() throws IOException, ClassNotFoundException {
		// Initialize the solver with an already full and valid TranspositionTable
		// Prevents a cold start
		Path evalsSerialized = Paths.get(Utils.getProjectRoot(), Utils.tableResources, "depth0Evals.ser");
		ObjectInputStream inEvals = new ObjectInputStream(new FileInputStream(evalsSerialized.toFile()));
		byte[] evals = (byte[]) inEvals.readObject();
		inEvals.close();

		Path keysSerialized = Paths.get(Utils.getProjectRoot(), Utils.tableResources, "depth0Keys.ser");
		ObjectInputStream inKeys = new ObjectInputStream(new FileInputStream(keysSerialized.toFile()));
		int[] keys = (int[]) inKeys.readObject();
		inKeys.close();

		solver = new Solver(new TranspositionTable(keys, evals));
		maxBookDepth = TreeReader.getMaxBookDepth();
		treeReaders = new TreeReader[maxBookDepth + 1];
		for (int depth = 0; depth <= maxBookDepth; depth++) {
			String bookName = "depth" + depth + "Book.bin";
			File book = Paths.get(Utils.getProjectRoot(), Utils.bookResources, bookName).toFile();
			treeReaders[depth] = new TreeReader(book);
		}
	}
}
