package org.demo.hello.world.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyWebAppDemo {
	public static void main(String[] args) {
		Server server = new Server(8080); 
		
		WebAppContext webApp = new WebAppContext(); 
		webApp.setResourceBase("./src/main/webapp");
		server.setHandler(webApp);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
