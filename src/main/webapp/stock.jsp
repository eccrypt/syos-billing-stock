<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="core.models.User, core.models.StockEntry, java.util.List, java.util.Map" %>
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
            <li><button onclick="location.href='stock/add'">Add Stock Entry</button></li>
            <li><button onclick="location.href='stock/allocate'">Allocate Stock</button></li>
            <li><button onclick="location.href='stock/reorder'">Check Reorder Alerts</button></li>
            <li><button onclick="location.href='stock/level'">View Stock Level</button></li>
            <li><button onclick="location.href='stock/update'">Update Stock Entry</button></li>
            <% if ("admin".equals(request.getAttribute("userRole"))) { %>
            <li><button onclick="location.href='stock/delete'">Delete Stock Entry</button></li>
            <% } %>
        </ul>

        <% if ("entries".equals(request.getAttribute("view"))) { %>
        <h2>All Stock Entries</h2>
        <table>
            <tr><th>Item Code</th><th>Quantity</th><th>Entry Date</th><th>Expiry Date</th></tr>
            <% List<StockEntry> entries = (List<StockEntry>) request.getAttribute("stockEntries");
            for (StockEntry entry : entries) { %>
            <tr><td><%= entry.getItemCode() %></td><td><%= entry.getQuantity() %></td><td><%= entry.getEntryDate() %></td><td><%= entry.getExpiryDate() %></td></tr>
            <% } %>
        </table>
        <% } %>

        <% if ("reorder".equals(request.getAttribute("view"))) { %>
        <h2>Reorder Alerts</h2>
        <% Map<String, Integer> reorderItems = (Map<String, Integer>) request.getAttribute("reorderItems");
        if (reorderItems.isEmpty()) { %>
        <p>No items need reordering.</p>
        <% } else { %>
        <ul>
        <% for (Map.Entry<String, Integer> entry : reorderItems.entrySet()) { %>
        <li><%= entry.getKey() %>: <%= entry.getValue() %> units left</li>
        <% } %>
        </ul>
        <% } %>
        <% } %>

        <% if ("level".equals(request.getAttribute("view"))) { %>
        <h2>Stock Level for <%= request.getAttribute("itemCode") %></h2>
        <p>Available stock: <%= request.getAttribute("stockLevel") %> units</p>
        <% } %>

        <% if (request.getAttribute("showLevelForm") != null) { %>
        <h2>View Stock Level</h2>
        <form action="stock/level" method="get">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <button type="submit">View</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showAddForm") != null) { %>
        <h2>Add Stock Entry</h2>
        <form action="stock" method="post">
            <input type="hidden" name="action" value="add">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <label>Quantity: <input type="number" name="quantity" min="1" required></label><br>
            <label>Expiry Date: <input type="date" name="expiry" required></label><br>
            <button type="submit">Add</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showAllocateForm") != null) { %>
        <h2>Allocate Stock</h2>
        <form action="stock" method="post">
            <input type="hidden" name="action" value="allocate">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <label>Quantity: <input type="number" name="quantity" min="1" required></label><br>
            <button type="submit">Allocate</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showUpdateForm") != null) { %>
        <h2>Update Stock Entry</h2>
        <form action="stock" method="post">
            <input type="hidden" name="action" value="update">
            <label>Item Code: <input type="text" name="itemCode" required></label><br>
            <label>New Quantity: <input type="number" name="quantity" min="1" required></label><br>
            <label>New Expiry Date: <input type="date" name="expiry" required></label><br>
            <button type="submit">Update</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showDeleteForm") != null) { %>
        <h2>Delete Stock Entry</h2>
        <form action="stock" method="post">
            <input type="hidden" name="action" value="delete">
            <label>Stock Entry ID: <input type="number" name="entryId" min="1" required></label><br>
            <button type="submit">Delete</button>
        </form>
        <% } %>
    </div>
</body>
</html>