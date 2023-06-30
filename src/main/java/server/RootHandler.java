package server;

import miscHelpers.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RootHandler implements HttpHandler {
	public void handle(HttpExchange httpExchange) throws IOException {
		Server.readRequest(httpExchange);

		// Get Path ignores queries (?) and fragments (#)
		String uriPath = httpExchange.getRequestURI().getPath();

		if (uriPath.equals("/")) {
			uriPath = "/index.html";
		}

		System.out.print("RootHandler handling the uri path ");
		System.out.println(uriPath);

		File file = Paths.get(Utils.getProjectRoot(), Utils.frontEndResources, uriPath).toFile();
		Server.writeResponse(httpExchange, file);
	}
}
