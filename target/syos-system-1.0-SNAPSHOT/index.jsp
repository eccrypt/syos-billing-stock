<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>SYOS - Login/Register</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .container { max-width: 400px; margin: auto; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; }
        input[type="text"], input[type="password"] { width: 100%; padding: 8px; }
        button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; }
        button:hover { background-color: #0056b3; }
        .tab { display: none; }
        .tab.active { display: block; }
        .tabs { margin-bottom: 20px; }
        .tab-button { padding: 10px; cursor: pointer; background-color: #f0f0f0; border: none; }
        .tab-button.active { background-color: #007bff; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <h1>SYOS Billing & Stock System</h1>

        <div class="tabs">
            <button class="tab-button active" onclick="showTab('login')">Login</button>
            <button class="tab-button" onclick="showTab('register')">Register</button>
        </div>

        <div id="login" class="tab active">
            <h2>Login</h2>
            <form action="api/auth/login" method="post">
                <div class="form-group">
                    <label for="login-username">Username:</label>
                    <input type="text" id="login-username" name="username" required>
                </div>
                <div class="form-group">
                    <label for="login-password">Password:</label>
                    <input type="password" id="login-password" name="password" required>
                </div>
                <button type="submit">Login</button>
            </form>
        </div>

        <div id="register" class="tab">
            <h2>Register</h2>
            <form action="api/auth/register" method="post">
                <div class="form-group">
                    <label for="register-username">Username:</label>
                    <input type="text" id="register-username" name="username" required>
                </div>
                <div class="form-group">
                    <label for="register-role">Role:</label>
                    <select id="register-role" name="role" required>
                        <option value="employee">Employee</option>
                        <option value="admin">Admin</option>
                        <option value="customer">Customer</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="register-password">Password:</label>
                    <input type="password" id="register-password" name="password" required>
                </div>
                <button type="submit">Register</button>
            </form>
        </div>

        <% if (request.getParameter("error") != null) { %>
            <p style="color: red;"><%= request.getParameter("error") %></p>
        <% } %>
        <% if (request.getParameter("message") != null) { %>
            <p style="color: green;"><%= request.getParameter("message") %></p>
        <% } %>
    </div>

    <script>
        function showTab(tabName) {
            const tabs = document.querySelectorAll('.tab');
            const buttons = document.querySelectorAll('.tab-button');

            tabs.forEach(tab => tab.classList.remove('active'));
            buttons.forEach(btn => btn.classList.remove('active'));

            document.getElementById(tabName).classList.add('active');
            event.target.classList.add('active');
        }
    </script>
</body>
</html>