package server;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


public class Server {
	public static final int PORT = 80;
	public static final int OK_STATUS = 200;

	public static String readRequest(HttpExchange httpExchange) throws IOException {
		InputStream requestStream = httpExchange.getRequestBody();
		byte[] request = requestStream.readAllBytes();
		requestStream.close();
		return new String(request);
	}

	public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
		httpExchange.sendResponseHeaders(OK_STATUS, response.length());
		OutputStream responseStream = httpExchange.getResponseBody();
		responseStream.write(response.getBytes());
		responseStream.close();
		httpExchange.close();
	}

	public static void writeResponse(HttpExchange httpExchange, File file) throws IOException {
		String contextType = URLConnection.guessContentTypeFromName(file.getName());
		if (contextType != null) {
			httpExchange.getResponseHeaders().set("Content-Type", contextType);
		}
		httpExchange.sendResponseHeaders(OK_STATUS, file.length());
		FileInputStream fileStream = new FileInputStream(file);
		OutputStream responseStream = httpExchange.getResponseBody();
		responseStream.write(fileStream.readAllBytes());
		fileStream.close();
		responseStream.close();
		httpExchange.close();
	}

	public static Map<String, String> queryToMap(String query){
		Map<String, String> map = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) {
				map.put(pair[0], pair[1]);
			}
			else {
				map.put(pair[0], "");
			}
		}
		return map;
	}

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext("/", new RootHandler());
		server.createContext("/solve/", new SolveHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("The server is running");
	}
}
