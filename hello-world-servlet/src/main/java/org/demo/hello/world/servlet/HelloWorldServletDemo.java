package org.demo.hello.world.servlet;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldServletDemo extends GenericServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1522387682150936960L;

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		log.info("remote: {} params: {} ", req.getRemoteAddr(), JSON.toJSONString(req.getParameterMap()));
		res.getWriter().write("<h1>Hello World Servlet</h1>");
	}
}
