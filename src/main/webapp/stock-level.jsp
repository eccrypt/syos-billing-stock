<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="core.models.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.jsp?error=Please login first");
        return;
    }
%>
<html>
<head>
    <title>View Stock Level</title>
</head>
<body>
    <div>
        <h1>View Stock Level</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>

        <% if (request.getAttribute("itemCode") != null) { %>
        <h2>Stock Level for <%= request.getAttribute("itemCode") %></h2>
        <p>Available stock: <%= request.getAttribute("stockLevel") %> units</p>
        <% } else { %>
        <h2>View Stock Level</h2>
        <form action="stock/level" method="get">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <button type="submit">View</button>
        </form>
        <% } %>
    </div>
</body>
</html>