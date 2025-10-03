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
    <title>Billing</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .container { max-width: 800px; margin: auto; }
        .item-row { display: flex; margin-bottom: 10px; }
        .item-row input { margin-right: 10px; flex: 1; padding: 5px; }
        .add-item { margin-bottom: 20px; }
        .total { font-size: 18px; font-weight: bold; margin: 20px 0; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Billing</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("success") != null) { %>
            <p class="success"><%= request.getAttribute("success") %></p>
        <% } %>

        <form id="billingForm" action="billing" method="post">
            <div id="items">
                <div class="item-row">
                    <input type="text" name="itemCode" placeholder="Item Code" required>
                    <input type="number" name="quantity" placeholder="Quantity" min="1" required>
                </div>
            </div>
            <button type="button" onclick="addItem()">Add Another Item</button>
            <br><br>
            <label for="cash">Cash Tendered:</label>
            <input type="number" id="cash" name="cash" step="0.01" min="0" required>
            <br><br>
            <button type="submit">Generate Bill</button>
        </form>
    </div>

    <script>
        function addItem() {
            const itemsDiv = document.getElementById('items');
            const newRow = document.createElement('div');
            newRow.className = 'item-row';
            newRow.innerHTML = '<input type="text" name="itemCode" placeholder="Item Code" required> <input type="number" name="quantity" placeholder="Quantity" min="1" required>';
            itemsDiv.appendChild(newRow);
        }
    </script>
</body>
</html>