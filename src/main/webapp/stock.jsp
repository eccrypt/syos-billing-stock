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
    <title>Stock Management</title>
</head>
<body>
    <div>
        <h1>Stock Management</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>

        <ul>
            <li><button onclick="location.href='stock/view'">View All Stock Entries</button></li>
            <li><button onclick="location.href='stock/reorder'">Check Reorder Alerts</button></li>
            <li><button onclick="location.href='stock/level'">View Stock Level</button></li>
            <li><form action="stock" method="post"><input type="hidden" name="action" value="add"><button type="submit">Add Stock Entry</button></form></li>
            <li><form action="stock" method="post"><input type="hidden" name="action" value="allocate"><button type="submit">Allocate Stock</button></form></li>
            <li><form action="stock" method="post"><input type="hidden" name="action" value="update"><button type="submit">Update Stock Entry</button></form></li>
            <li><form action="stock" method="post"><input type="hidden" name="action" value="delete"><button type="submit">Delete Stock Entry</button></form></li>
        </ul>
    </div>
</body>
</html>