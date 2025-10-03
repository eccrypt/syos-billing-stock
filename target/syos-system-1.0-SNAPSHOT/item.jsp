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
    <title>Item Management</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .container { max-width: 800px; margin: auto; }
        .menu { list-style: none; padding: 0; }
        .menu li { margin-bottom: 10px; }
        .menu button { width: 100%; padding: 15px; background-color: #007bff; color: white; border: none; cursor: pointer; }
        .menu button:hover { background-color: #0056b3; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Item Management</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="success"><%= request.getAttribute("message") %></p>
        <% } %>

        <ul class="menu">
            <li><button onclick="location.href='item/view'">View All Items</button></li>
            <li><button onclick="location.href='item/search'">Search Item by Code</button></li>
            <li><form action="item" method="post" style="display:inline;"><input type="hidden" name="action" value="add"><button type="submit">Add Item</button></form></li>
            <li><form action="item" method="post" style="display:inline;"><input type="hidden" name="action" value="update"><button type="submit">Update Item</button></form></li>
            <li><form action="item" method="post" style="display:inline;"><input type="hidden" name="action" value="updateName"><button type="submit">Update Item Name</button></form></li>
            <li><form action="item" method="post" style="display:inline;"><input type="hidden" name="action" value="updatePrice"><button type="submit">Update Item Price</button></form></li>
            <li><form action="item" method="post" style="display:inline;"><input type="hidden" name="action" value="delete"><button type="submit">Delete Item</button></form></li>
        </ul>
    </div>
</body>
</html>