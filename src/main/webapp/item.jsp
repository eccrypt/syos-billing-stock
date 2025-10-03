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
</head>
<body>
    <div>
        <h1>Item Management</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>

        <ul>
            <li><button onclick="location.href='item/view'">View All Items</button></li>
            <li><button onclick="location.href='item/search'">Search Item by Code</button></li>
            <li><form action="item" method="post"><input type="hidden" name="action" value="add"><button type="submit">Add Item</button></form></li>
            <li><form action="item" method="post"><input type="hidden" name="action" value="update"><button type="submit">Update Item</button></form></li>
            <li><form action="item" method="post"><input type="hidden" name="action" value="updateName"><button type="submit">Update Item Name</button></form></li>
            <li><form action="item" method="post"><input type="hidden" name="action" value="updatePrice"><button type="submit">Update Item Price</button></form></li>
            <li><form action="item" method="post"><input type="hidden" name="action" value="delete"><button type="submit">Delete Item</button></form></li>
        </ul>
    </div>
</body>
</html>