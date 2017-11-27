package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author parad
 */
public class ConnectionWorker implements Runnable {
    /* сокет, через который происходит обмен данными с клиентом*/
    private Socket clientSocket = null;
    /* входной поток, через который получаем данные с сокета */
    private InputStream inputStream = null;
    private String[] ip = new String[3];
    private static String login1 = null, login2 = null;
    private static String password1;
    private static String password2 = null;
    private static String text = null;
    private boolean k = false;
    private static int id = 0, author = 0, client = 0;

    public ConnectionWorker(Socket socket) {
        clientSocket = socket;
    }

    @Override
    public void run() {
        /* получаем входной поток */
        try {
            ip[0] = clientSocket.getRemoteSocketAddress().toString();
            int i = 1;
            for (String retval : ip[0].split(":")) {
                ip[i] = retval;
                i++;
            }
            ip[1] = ip[1].substring(1);
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Cant get input stream");
        }
        /* создаем буфер для данных */
        byte[] buffer = new byte[1024 * 4];
        while (true) {
            try {
                /*                   * получаем очередную порцию данных                  * в переменной count хранится реальное количество байт, которое получили                  */
                int count = inputStream.read(buffer, 0, buffer.length);
                /* проверяем, какое количество байт к нам прийшло */
                if (count > 0) {
                    String l = new String(buffer, 0, count);
                    System.out.println(l);
                    String[] a = new String[3];
                    int i = 0;
                    /*Розделаем пришедшую строку на параметрі - user1/command - user2/login - message/password*/
                    for (String retval : l.split(" ")) {
                        if (i == 2 && a[i] != null)
                            a[i] += " " + retval;
                        if (a[i] == null)
                            a[i] = retval;
                        if (i < 2)
                            i++;
                    }
                    Connection c = null;
                    Statement stmt = null;
                    if (a[0].equals("Login")) {
                        login1 = null;
                        password1 = null;
                        try {
                            Class.forName("org.postgresql.Driver");
                            c = DriverManager
                                    .getConnection("jdbc:postgresql://localhost:5432/mess",
                                            "postgres", "123");
                            c.setAutoCommit(false);
                            stmt = c.createStatement();
                            ResultSet rs = stmt.executeQuery(
                                    "SELECT login, password " +
                                            "FROM users;");
                            while (rs.next()) {
                                String login = rs.getString("login");
                                String password = rs.getString("password");
                                if (login.equals(a[1])) {
                                    login1 = login;
                                    password1 = password;
                                }
                            }
                            rs.close();
                            stmt.close();
                            c.close();
                        } catch (Exception e) {
                            System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        }
                    } else if (a[0].equals("Check")) {
                        login2 = null;
                        try {
                            Class.forName("org.postgresql.Driver");
                            c = DriverManager
                                    .getConnection("jdbc:postgresql://localhost:5432/mess",
                                            "postgres", "123");
                            c.setAutoCommit(false);
                            stmt = c.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT login FROM users;");
                            while (rs.next()) {
                                String login = rs.getString("login");
                                if (login.equals(a[1])) {
                                    login2 = login;
                                }
                            }
                            rs.close();
                            stmt.close();
                            c.close();
                        } catch (Exception e) {
                            System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        }
                        if (login2 != null) {
                            Client client = new Client("Chat", ip[1], ip[2]);
                            Thread s = new Thread(client);
                            s.start();
                            System.out.println("Chat");
                        } else {
                            Client client = new Client("No in base", ip[1], ip[2]);
                            Thread s = new Thread(client);
                            s.start();
                            System.out.println("No in base");
                        }
                    }
                    if (login1 == null) {
                        Client client = new Client("No in base", ip[1], ip[2]);
                        Thread s = new Thread(client);
                        s.start();
                        System.out.println("No in base");
                    } else if (password1.equals(a[2]) || password2 != null) {
                        password2 = password1;
                        if (a[0].equals("Send")) {
                            try {
                                Class.forName("org.postgresql.Driver");
                                c = DriverManager
                                        .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                "postgres", "123");
                                c.setAutoCommit(false);
                                stmt = c.createStatement();
                                ResultSet rs = stmt.executeQuery(
                                        "SELECT c.ctext " +
                                                "FROM chat c, users u, users u1 " +
                                                "WHERE u.id = c.author " +
                                                "AND u.login = '" + a[1] + "' " +
                                                "AND u1.id = c.client " +
                                                "AND u1.login = '" + a[2] + "';");
                                while (rs.next()) {
                                    text = rs.getString("ctext");
                                }
                                if (!text.equals("") && (text != null)) {
                                    Client client = new Client(String.format("%s %s %s", a[1], a[2], text), ip[1], ip[2]);
                                    Thread s = new Thread(client);
                                    s.start();
                                    System.out.printf("%s %s %s%n", a[1], a[2], text);
                                }
                                text = null;
                                rs.close();
                                stmt.close();
                                c.close();
                            } catch (Exception e) {
                                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                                Client client = new Client("Nothing", ip[1], ip[2]);
                                Thread s = new Thread(client);
                                s.start();
                                System.out.println("Nothing");
                            }
                        } else if (a[0].equals("Read")) {
                            try {
                                Class.forName("org.postgresql.Driver");
                                c = DriverManager
                                        .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                "postgres", "123");
                                c.setAutoCommit(false);
                                stmt = c.createStatement();
                                ResultSet rs = stmt.executeQuery(
                                        "SELECT c.id " +
                                                "FROM chat c, users u, users u1 " +
                                                "WHERE u.id = c.author " +
                                                "AND u.login = '" + a[2] + "' " +
                                                "AND u1.id = c.client " +
                                                "AND u1.login = '" + a[1] + "';");
                                while (rs.next()) {
                                    id = rs.getInt("id");
                                }
                                try {
                                    Class.forName("org.postgresql.Driver");
                                    c = DriverManager
                                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                    "postgres", "123");
                                    c.setAutoCommit(false);
                                    stmt = c.createStatement();
                                    String sql =
                                            "DELETE FROM chat WHERE id = " + id + ";";
                                    System.out.println(sql);
                                    stmt.executeUpdate(sql);
                                    c.commit();
                                    stmt.close();
                                    c.close();
                                } catch (Exception e) {
                                    System.err.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
                                }
                                id = 0;
                            } catch (Exception e) {
                                System.err.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
                            }
                        } else if (k) {
                            try {
                                Class.forName("org.postgresql.Driver");
                                c = DriverManager
                                        .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                "postgres", "123");
                                c.setAutoCommit(false);
                                stmt = c.createStatement();
                                ResultSet rs = stmt.executeQuery(
                                        "SELECT c.id " +
                                                "FROM chat c, users u, users u1 " +
                                                "WHERE u.id = c.author " +
                                                "AND u.login = '" + a[0] + "' " +
                                                "AND u1.id = c.client " +
                                                "AND u1.login = '" + a[1] + "';");
                                while (rs.next()) {
                                    id = rs.getInt("id");
                                }
                                c.commit();
                                stmt.close();
                                c.close();
                            } catch (Exception e) {
                                System.err.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
                            }
                            if (id != 0 && a[2] != null) {
                                try {
                                    Class.forName("org.postgresql.Driver");
                                    c = DriverManager
                                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                    "postgres", "123");
                                    c.setAutoCommit(false);
                                    stmt = c.createStatement();
                                    String sql =
                                            "UPDATE chat set ctext = ctext || '" + "::" + a[2] + "' :: text where id = " + id + ";";
                                    stmt.executeUpdate(sql);
                                    c.commit();
                                    stmt.close();
                                    c.close();
                                } catch (Exception e) {
                                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                                }
                                Client client = new Client("Message is taken", ip[1], ip[2]);
                                Thread s = new Thread(client);
                                s.start();
                                System.out.println("Message is taken");
                            } else if (!a[0].equals("Check")) {
                                try {
                                    Class.forName("org.postgresql.Driver");
                                    c = DriverManager
                                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                    "postgres", "123");
                                    c.setAutoCommit(false);
                                    stmt = c.createStatement();
                                    ResultSet rs = stmt.executeQuery(
                                            "SELECT id " +
                                                    "FROM users " +
                                                    "WHERE login = '" + a[0] + "';");
                                    while (rs.next()) {
                                        author = rs.getInt("id");
                                    }
                                    rs.close();
                                    stmt.close();
                                    c.close();
                                } catch (Exception e) {
                                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                                }
                                try {
                                    Class.forName("org.postgresql.Driver");
                                    c = DriverManager
                                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                    "postgres", "123");
                                    c.setAutoCommit(false);
                                    stmt = c.createStatement();
                                    ResultSet rs = stmt.executeQuery(
                                            "SELECT id " +
                                                    "FROM users " +
                                                    "WHERE login = '" + a[1] + "';");
                                    while (rs.next()) {
                                        client = rs.getInt("id");
                                    }
                                    rs.close();
                                    stmt.close();
                                    c.close();
                                } catch (Exception e) {
                                    System.err.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
                                }
                                try {
                                    Class.forName("org.postgresql.Driver");
                                    c = DriverManager
                                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                                    "postgres", "123");
                                    c.setAutoCommit(false);
                                    stmt = c.createStatement();
                                    String sql = "INSERT INTO chat (author,client,ctext)" +
                                            "VALUES ('" + author + "', '" + client + "', '" + a[2] + "');";
                                    stmt.executeUpdate(sql);
                                    stmt.close();
                                    c.commit();
                                    c.close();
                                } catch (Exception e) {
                                    System.err.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
                                }
                                Client client = new Client("Message is taken", ip[1], ip[2]);
                                Thread s = new Thread(client);
                                s.start();
                                System.out.println("Message is taken");
                            }
                        } else {
                            k = true;
                            Client client = new Client("Hello!", ip[1], ip[2]);
                            Thread s = new Thread(client);
                            s.start();
                            System.out.println("Hello!");
                        }
                    } else {
                        Client client = new Client("Wrong password", ip[1], ip[2]);
                        Thread s = new Thread(client);
                        s.start();
                        System.out.println("Wrong password");
                    }

                } else /* если мы получили -1, значит прервался наш поток с данными  */
                    if (count == -1) {
                        System.out.println("Close socket");
                        login1 = null;
                        password2 = null;
                        k = false;
                        clientSocket.close();
                        break;
                    }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}