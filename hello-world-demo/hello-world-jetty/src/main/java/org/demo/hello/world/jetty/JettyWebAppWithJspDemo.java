package org.demo.hello.world.jetty;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyWebAppWithJspDemo {
	public static void main(String[] args) {
		Server server = new Server(8080);

		WebAppContext webApp = new WebAppContext();
		webApp.setResourceBase("./src/main/webapp");

		// 支持JSP必须增加的代码
		File temp = new File("./src/main/webapp/temp");
		webApp.setTempDirectory(temp);
		webApp.setAttribute("javax.servlet.context.tempdir", temp);
		webApp.setAttribute("org.eclipse.jetty.containerInitializers",
				Arrays.asList(new ContainerInitializer(new JettyJasperInitializer(), null)));
		webApp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
		webApp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
				".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
		// webApp.setAttribute("org.apache.tomcat.JarScanner", new
		// StandardJarScanner());
		ClassLoader jspClassLoader = new URLClassLoader(new URL[0], JettyWebAppWithJspDemo.class.getClassLoader());
		webApp.setClassLoader(jspClassLoader);

		// Manually call JettyJasperInitializer on context startup
		webApp.addBean(new JspStarter(webApp));

		ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
		holderJsp.setInitOrder(0);
		holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
		holderJsp.setInitParameter("fork", "false");
		holderJsp.setInitParameter("xpoweredBy", "false");
		holderJsp.setInitParameter("compilerTargetVM", "1.8");
		holderJsp.setInitParameter("compilerSourceVM", "1.8");
		holderJsp.setInitParameter("keepgenerated", "true");
		webApp.addServlet(holderJsp, "*.jsp");
		server.setHandler(webApp);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static class JspStarter extends AbstractLifeCycle
			implements ServletContextHandler.ServletContainerInitializerCaller {
		JettyJasperInitializer sci;
		ServletContextHandler context;

		public JspStarter(ServletContextHandler context) {
			this.sci = new JettyJasperInitializer();
			this.context = context;
			this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
		}

		@Override
		protected void doStart() throws Exception {
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(context.getClassLoader());
			try {
				sci.onStartup(null, context.getServletContext());
				super.doStart();
			} finally {
				Thread.currentThread().setContextClassLoader(old);
			}
		}
	}
}
