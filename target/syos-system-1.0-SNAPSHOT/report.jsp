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
    <title>Reports</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .container { max-width: 800px; margin: auto; }
        .menu { list-style: none; padding: 0; }
        .menu li { margin-bottom: 10px; }
        .menu button { width: 100%; padding: 15px; background-color: #007bff; color: white; border: none; cursor: pointer; }
        .menu button:hover { background-color: #0056b3; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; }
        input[type="date"] { padding: 8px; width: 200px; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Generate Reports</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="success"><%= request.getAttribute("message") %></p>
        <% } %>

        <ul class="menu">
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="reorder">
                    <button type="submit">Reorder Level Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="daily">
                    <div class="form-group">
                        <label for="daily-date">Date:</label>
                        <input type="date" id="daily-date" name="date" required>
                    </div>
                    <button type="submit">Daily Sales Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="stock">
                    <button type="submit">Stock Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="bill">
                    <div class="form-group">
                        <label for="bill-date">Date:</label>
                        <input type="date" id="bill-date" name="date" required>
                    </div>
                    <button type="submit">Bill Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="all">
                    <div class="form-group">
                        <label for="all-date">Date:</label>
                        <input type="date" id="all-date" name="date" required>
                    </div>
                    <button type="submit">Generate All Reports</button>
                </form>
            </li>
        </ul>
    </div>
</body>
</html>