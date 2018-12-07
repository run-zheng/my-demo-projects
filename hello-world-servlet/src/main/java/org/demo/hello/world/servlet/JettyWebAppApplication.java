package org.demo.hello.world.servlet;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyWebAppApplication {
	public static void main(String[] args) {
		Server server = new Server(8080); 
		
		//支持JSP必须增加以下配置
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        //用WebAppContext可以支持servlet
		WebAppContext webApp = new WebAppContext(); 
		webApp.setContextPath( "/" );
		webApp.setResourceBase("./src/main/webapp");
		//支持jstl和其他tag必须设置以下配置
		webApp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );
		//Jsp临时文件目录
		File temp = new File("./src/main/webapp/temp");
		webApp.setTempDirectory(temp);
				
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
