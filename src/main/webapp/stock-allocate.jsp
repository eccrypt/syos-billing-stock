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
    <title>Allocate Stock</title>
</head>
<body>
    <div>
        <h1>Allocate Stock</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>

        <form action="stock" method="post">
            <input type="hidden" name="action" value="allocate">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <label>Quantity: <input type="number" name="quantity" min="1" required></label><br>
            <button type="submit">Allocate</button>
        </form>
    </div>
</body>
</html>