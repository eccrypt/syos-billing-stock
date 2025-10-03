<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="core.models.User, core.models.Item, java.util.List" %>
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

        <% if ("items".equals(request.getAttribute("view"))) { %>
        <h2>All Items</h2>
        <table>
            <tr><th>Item Code</th><th>Item Name</th><th>Price (Rs)</th></tr>
            <% List<Item> items = (List<Item>) request.getAttribute("items");
            for (Item item : items) { %>
            <tr><td><%= item.getCode() %></td><td><%= item.getName() %></td><td><%= String.format("%.2f", item.getPrice()) %></td></tr>
            <% } %>
        </table>
        <% } %>

        <% if ("item".equals(request.getAttribute("view"))) { %>
        <h2>Item Details</h2>
        <% Item item = (Item) request.getAttribute("item");
        if (item != null) { %>
        <table>
            <tr><th>Item Code</th><th>Item Name</th><th>Price (Rs)</th></tr>
            <tr><td><%= item.getCode() %></td><td><%= item.getName() %></td><td><%= String.format("%.2f", item.getPrice()) %></td></tr>
        </table>
        <% } else { %>
        <p>Item not found.</p>
        <% } %>
        <% } %>

        <% if (request.getAttribute("showSearchForm") != null) { %>
        <h2>Search Item</h2>
        <form action="item/search" method="get">
            <label>Item Code: <input type="text" name="code" required></label><br>
            <button type="submit">Search</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showAddForm") != null) { %>
        <h2>Add Item</h2>
        <form action="item" method="post">
            <input type="hidden" name="action" value="add">
            <label>Name: <input type="text" name="name" required></label><br>
            <label>Price: <input type="number" step="0.01" name="price" required></label><br>
            <label>Shelf Default: <input type="number" name="shelfDefault" required></label><br>
            <label>Quantity: <input type="number" name="quantity" required></label><br>
            <label>Expiry Date: <input type="date" name="expiry" required></label><br>
            <button type="submit">Add</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showUpdateForm") != null) { %>
        <h2>Update Item</h2>
        <form action="item" method="post">
            <input type="hidden" name="action" value="update">
            <label>Code: <input type="text" name="code" required></label><br>
            <label>Name: <input type="text" name="name" required></label><br>
            <label>Price: <input type="number" step="0.01" name="price" required></label><br>
            <button type="submit">Update</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showUpdateNameForm") != null) { %>
        <h2>Update Item Name</h2>
        <form action="item" method="post">
            <input type="hidden" name="action" value="updateName">
            <label>Code: <input type="text" name="code" required></label><br>
            <label>Name: <input type="text" name="name" required></label><br>
            <button type="submit">Update</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showUpdatePriceForm") != null) { %>
        <h2>Update Item Price</h2>
        <form action="item" method="post">
            <input type="hidden" name="action" value="updatePrice">
            <label>Code: <input type="text" name="code" required></label><br>
            <label>Price: <input type="number" step="0.01" name="price" required></label><br>
            <button type="submit">Update</button>
        </form>
        <% } %>

        <% if (request.getAttribute("showDeleteForm") != null) { %>
        <h2>Delete Item</h2>
        <form action="item" method="post">
            <input type="hidden" name="action" value="delete">
            <label>Code: <input type="text" name="code" required></label><br>
            <button type="submit">Delete</button>
        </form>
        <% } %>
    </div>
</body>
</html>