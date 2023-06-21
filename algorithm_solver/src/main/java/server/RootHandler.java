package server;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RootHandler implements HttpHandler {
	public void handle(HttpExchange httpExchange) throws IOException {
		System.out.println("RootHandler reading a response:");
		System.out.println(Server.readRequest(httpExchange));
		// TODO: Make this send over the complete HTML, CSS, JS file for the client
		// This response is temporary, delete later
		String response = "Use /solve/?hello=word&foo=bar to see how to handle url parameters";
		Server.writeResponse(httpExchange, response.toString());
		
		System.out.println("RootHandler sent a response");
	}
}
