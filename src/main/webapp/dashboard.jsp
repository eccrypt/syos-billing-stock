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
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .container { max-width: 600px; margin: auto; }
        .menu { list-style: none; padding: 0; }
        .menu li { margin-bottom: 10px; }
        .menu a { display: block; padding: 15px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; }
        .menu a:hover { background-color: #0056b3; }
        .logout { margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Welcome to SYOS Dashboard</h1>
        <p>Logged in as: <%= user.getUsername() %> (<%= user.getRole() %>)</p>

        <ul class="menu">
            <li><a href="billing.jsp">Billing</a></li>
            <li><a href="stock.jsp">Stock Management</a></li>
            <li><a href="item.jsp">Item Management</a></li>
            <li><a href="report.jsp">Generate Reports</a></li>
        </ul>

        <div class="logout">
            <a href="logout.jsp" style="background-color: #dc3545; padding: 10px 20px; color: white; text-decoration: none; border-radius: 5px;">Logout</a>
        </div>
    </div>
</body>
</html>