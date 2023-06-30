package server;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SolveHandler implements HttpHandler {
	public void handle(HttpExchange httpExchange) throws IOException {
		Server.readRequest(httpExchange);

		// TODO: Make this take in one parameter, which contains the current position
		// Then make it solve the position, and send back a complex JSON object
		// This respose is temporary, delete later
		// Also delete Server.queryToMap. We only expect one paramter (position) so this is extra
		StringBuilder response = new StringBuilder();
		Map <String,String>parms = Server.queryToMap(httpExchange.getRequestURI().getQuery());
		response.append("<html><body>");
		response.append("hello : " + parms.get("hello") + "<br/>");
		response.append("foo : " + parms.get("foo") + "<br/>");
		response.append("</body></html>");
		Server.writeResponse(httpExchange, response.toString());

		System.out.println("SolveHandler send a response");
	}
}
