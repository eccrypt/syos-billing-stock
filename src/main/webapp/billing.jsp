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
</head>
<body>
    <div>
        <h1>Billing</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("success") != null) { %>
            <p><%= request.getAttribute("success") %></p>
        <% } %>

        <form id="billingForm" action="billing" method="post">
            <div id="items">
                <div>
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
            newRow.innerHTML = '<input type="text" name="itemCode" placeholder="Item Code" required> <input type="number" name="quantity" placeholder="Quantity" min="1" required>';
            itemsDiv.appendChild(newRow);
        }
    </script>
</body>
</html>