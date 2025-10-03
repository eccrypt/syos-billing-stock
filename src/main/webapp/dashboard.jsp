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
    <title>SYOS - Dashboard</title>
</head>
<body>
    <div>
        <h1>Welcome to SYOS Dashboard</h1>
        <p>Logged in as: <%= user.getUsername() %> (<%= user.getRole() %>)</p>

        <ul>
            <li><a href="billing">Billing</a></li>
            <li><a href="stock">Stock Management</a></li>
            <li><a href="item">Item Management</a></li>
            <li><a href="report">Generate Reports</a></li>
        </ul>

        <div>
            <a href="logout.jsp">Logout</a>
        </div>
    </div>
</body>
</html>