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
    <title>Delete Stock Entry</title>
</head>
<body>
    <div>
        <h1>Delete Stock Entry</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>

        <form action="stock" method="post">
            <input type="hidden" name="action" value="delete">
            <label>Stock Entry ID: <input type="number" name="entryId" min="1" required></label><br>
            <button type="submit">Delete</button>
        </form>
    </div>
</body>
</html>