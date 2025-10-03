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
</head>
<body>
    <div>
        <h1>Generate Reports</h1>
        <a href="dashboard.jsp">Back to Dashboard</a>

        <% if (request.getAttribute("error") != null) { %>
            <p><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>

        <ul>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="reorder">
                    <button type="submit">Reorder Level Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="daily">
                    <div>
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
                    <div>
                        <label for="bill-date">Date:</label>
                        <input type="date" id="bill-date" name="date" required>
                    </div>
                    <button type="submit">Bill Report</button>
                </form>
            </li>
            <li>
                <form action="report" method="post">
                    <input type="hidden" name="action" value="all">
                    <div>
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