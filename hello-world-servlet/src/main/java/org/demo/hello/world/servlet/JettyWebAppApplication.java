package org.demo.hello.world.servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyWebAppApplication {
	public static void main(String[] args) {
		Server server = new Server(8080); 
		//用WebAppContext可以支持servlet
		
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        
		WebAppContext webApp = new WebAppContext(); 
		webApp.setContextPath( "/" );
		webApp.setResourceBase("./src/main/webapp");
		webApp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );
		
		server.setHandler(webApp);
		try {
			server.start();
			server.dumpStdErr();
			server.join();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
