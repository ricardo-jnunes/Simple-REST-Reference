package com.mock.main;

import java.io.IOException;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

public class Main {

	private static final String SERVER = "http://localhost:9999/";

	public static void main(String[] args) {
		try {
			HttpServer server = HttpServerFactory.create(SERVER);
			server.start();
			System.out.println("Press Enter to stop the server.");
			System.in.read();
			server.stop(0);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
