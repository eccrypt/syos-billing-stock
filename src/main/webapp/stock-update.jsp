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
    <title>Update Stock Entry</title>
</head>
<body>
    <div>
        <h1>Update Stock Entry</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>

        <form action="stock" method="post">
            <input type="hidden" name="action" value="update">
            <label>Item Code: <input type="text" name="itemCode" required></label><br>
            <label>New Quantity: <input type="number" name="quantity" min="1" required></label><br>
            <label>New Expiry Date: <input type="date" name="expiry" required></label><br>
            <button type="submit">Update</button>
        </form>
    </div>
</body>
</html>