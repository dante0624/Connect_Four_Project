package server;

import java.io.IOException;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import liveSolverClasses.Position;

public class AlignmentHandler implements HttpHandler {
	public void handle(HttpExchange httpExchange) throws IOException {

		Server.readRequest(httpExchange);

		// Ignores queries (?), fragments (#), and the path beginning with /alignment/
		String moves = httpExchange.getRequestURI().getPath().substring(11);
		Position position = new Position();
		for (char move : moves.toCharArray()) {
			position.playCol(Character.getNumericValue(move));
		}
		int[] alignments = position.priorPlayerAlignments();

		StringBuilder csvBuilder = new StringBuilder();
		String csvResponse;
		if (alignments.length > 0) {
			csvBuilder.append(alignments[0]);
			for (int i = 1; i < alignments.length; i++) {
				csvBuilder.append(",");
				csvBuilder.append(alignments[i]);
			}
			csvResponse = csvBuilder.toString();
		}
		else {
			csvResponse = "";
		}

		Server.writeResponse(httpExchange, csvResponse);

		System.out.print("AlignmentHandler found " + Arrays.toString(alignments));
		System.out.println(" for " + moves);
	}
}
