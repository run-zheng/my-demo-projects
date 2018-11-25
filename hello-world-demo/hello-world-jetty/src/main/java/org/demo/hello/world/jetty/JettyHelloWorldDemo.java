package org.demo.hello.world.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyHelloWorldDemo {

	@Slf4j
	public static class Handler extends AbstractHandler {
		@Override
		public void handle(String target, Request baseRequest, 
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			log.info("Handler request start: {}", request.getRequestURL());
			//设置类型，指定编码utf8
			response.setContentType("text/html; charset=utf-8");
			//设置响应状态吗
			response.setStatus(HttpServletResponse.SC_OK);
			//写响应数据
			response.getWriter().write("<h1>Hello world!</h1>");
			//标记请求已处理，handle链
			baseRequest.setHandled(true);
			log.info("Handler request end");
		}
	}

	public static void main(String[] args) {
		Server server = new Server(8080);
		try {
			server.setHandler(new Handler());
			server.start();
			server.join();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

}
