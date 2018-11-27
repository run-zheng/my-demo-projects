package org.demo.hello.world.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyResourceServerDemo {
	public static void main(String[] args) {
		Server server = new Server(8080); 
		ResourceHandler handler = new ResourceHandler();
		handler.setResourceBase("./");
		handler.setDirectoriesListed(true);
		server.setHandler(handler);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} 
		
	}
}
