<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register</title>
</head>
<body>
    <h1>Register</h1>
    <form action="api/auth/register" method="post">
        <div>
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required>
        </div>
        <div>
            <label for="role">Role:</label>
            <select id="role" name="role" required>
                <option value="employee">Employee</option>
                <option value="admin">Admin</option>
                <option value="customer">Customer</option>
            </select>
        </div>
        <div>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <button type="submit">Register</button>
    </form>
    <% if (request.getParameter("error") != null) { %>
        <p><%= request.getParameter("error") %></p>
    <% } %>
    <% if (request.getParameter("message") != null) { %>
        <p><%= request.getParameter("message") %></p>
    <% } %>
    <p><a href="login.jsp">Login</a></p>
</body>
</html>