<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="core.models.User, core.models.StockEntry, java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.jsp?error=Please login first");
        return;
    }
%>
<html>
<head>
    <title>View All Stock Entries</title>
</head>
<body>
    <div>
        <h1>View All Stock Entries</h1>
        <a href="stock">Back to Stock Menu</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>

        <h2>All Stock Entries</h2>
        <table border="1">
            <tr><th>Item Code</th><th>Quantity</th><th>Entry Date</th><th>Expiry Date</th></tr>
            <% List<StockEntry> entries = (List<StockEntry>) request.getAttribute("stockEntries");
            if (entries != null) {
                for (StockEntry entry : entries) { %>
            <tr><td><%= entry.getItemCode() %></td><td><%= entry.getQuantity() %></td><td><%= entry.getEntryDate() %></td><td><%= entry.getExpiryDate() %></td></tr>
            <% } } %>
        </table>
    </div>
</body>
</html>