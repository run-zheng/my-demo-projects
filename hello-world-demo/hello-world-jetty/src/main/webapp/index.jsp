<!-- 在jar包都有的前提下EL表达式原样输出，不被解析  原因是  page指令中确少 isELIgnored="false" servlet3.0默认关闭了el表达式的解析 -->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=$Encode">
    <title>show page</title>
</head>
<body>
	<h1>Hello world</h1>
    <% out.println("<h2>Hello JSP</h2>"); %>
    
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
	  <c:forEach var="i" begin="1" end="10" step="1">
      <c:out value="${i}" />
      <br />
    </c:forEach>
</body>
</html>


