<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="core.models.User, java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.jsp?error=Please login first");
        return;
    }
%>
<html>
<head>
    <title>Reorder Alerts</title>
</head>
<body>
    <div>
        <h1>Reorder Alerts</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>

        <h2>Items Needing Reorder</h2>
        <% Map<String, Integer> reorderItems = (Map<String, Integer>) request.getAttribute("reorderItems");
        if (reorderItems == null || reorderItems.isEmpty()) { %>
        <p>No items need reordering.</p>
        <% } else { %>
        <ul>
        <% for (Map.Entry<String, Integer> entry : reorderItems.entrySet()) { %>
        <li><%= entry.getKey() %>: <%= entry.getValue() %> units left</li>
        <% } %>
        </ul>
        <% } %>
    </div>
</body>
</html>