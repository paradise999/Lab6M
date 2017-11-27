/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * @author parad
 */
public class MainApp {

    static ArrayList<String> l = new ArrayList<>();

    public static void main(String[] args) {
        Server server = Server.getServer();
        Thread t = new Thread(server);
        t.start();
        Scanner sc = new Scanner(System.in);
        String enter;
        System.out.println("Wait your command");
        while (sc.hasNext()) {
            enter = sc.nextLine();
            command(enter);
            System.out.println("Wait your command");
        }
    }


    private static void command(String str) {
        Scanner sn = new Scanner(System.in);
        switch (str) {
            case "/stop":
                System.out.println("Exit");
                System.exit(0);
                break;
            case "/login":
                boolean l = false;
                Connection c;
                Statement stmt;
                System.out.println("Enter login");
                String login = sn.nextLine();
                try {
                    Class.forName("org.postgresql.Driver");
                    c = DriverManager
                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                    "postgres", "123");
                    c.setAutoCommit(false);
                    stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT login " +
                                    "FROM users ;");
                    while (rs.next()) {
                        String log = rs.getString("login");
                        if (log.equals(login)) {
                            l = true;
                        }
                    }
                    c.commit();
                    stmt.close();
                    c.close();
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                if (!l) {
                    System.out.println("Enter password");
                    String password = sn.nextLine();
                    try {
                        Class.forName("org.postgresql.Driver");
                        c = DriverManager
                                .getConnection("jdbc:postgresql://localhost:5432/mess",
                                        "postgres", "123");
                        c.setAutoCommit(false);
                        System.out.println("Opened database successfully");
                        stmt = c.createStatement();
                        String sql = "INSERT INTO users (login,password)" + "VALUES ('" + login + "', '" + password + "');";
                        stmt.executeUpdate(sql);
                        stmt.close();
                        c.commit();
                        c.close();
                    } catch (Exception e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                    System.out.println("Records created successfully");
                }
                else{
                    System.out.println("This login already exist");
                }
                break;
            case "/delete":
                l = false;
                boolean f = false;
                System.out.println("Enter login");
                login = sn.nextLine();
                try {
                    Class.forName("org.postgresql.Driver");
                    c = DriverManager
                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                    "postgres", "123");
                    c.setAutoCommit(false);
                    stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT login " +
                                    "FROM users ;");
                    while (rs.next()) {
                        String log = rs.getString("login");
                        if (log.equals(login)) {
                            l = true;
                        }
                    }
                    c.commit();
                    stmt.close();
                    c.close();
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                if (l) {
                    try {
                        Class.forName("org.postgresql.Driver");
                        c = DriverManager
                                .getConnection("jdbc:postgresql://localhost:5432/mess",
                                        "postgres", "123");
                        c.setAutoCommit(false);
                        System.out.println("Enter password");
                        String password = sn.nextLine();
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery(
                                "SELECT password " +
                                        "FROM users " +
                                        "WHERE login = '" + login + "';");
                        while (rs.next()) {
                            String pas = rs.getString("password");
                            if (pas.equals(password)) {
                                f = true;
                            }
                        }
                        c.commit();
                        stmt.close();
                        c.close();
                        if (f) {
                            Class.forName("org.postgresql.Driver");
                            c = DriverManager
                                    .getConnection("jdbc:postgresql://localhost:5432/mess",
                                            "postgres", "123");
                            c.setAutoCommit(false);
                            stmt = c.createStatement();
                            String sql =
                                    "DELETE FROM users " +
                                            "WHERE login = '" + login + "';";
                            stmt.executeUpdate(sql);
                            c.commit();
                            stmt.close();
                            c.close();
                        } else {
                            System.out.println("Wrong password");
                        }
                    } catch (Exception e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                    System.out.println("Records created successfully");
                } else {
                    System.out.println("No in base");
                }
                break;
            case "/update":
                l = false;
                f = false;
                System.out.println("Enter login");
                login = sn.nextLine();
                try {
                    Class.forName("org.postgresql.Driver");
                    c = DriverManager
                            .getConnection("jdbc:postgresql://localhost:5432/mess",
                                    "postgres", "123");
                    c.setAutoCommit(false);
                    stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT login " +
                                    "FROM users ;");
                    while (rs.next()) {
                        String log = rs.getString("login");
                        if (log.equals(login)) {
                            l = true;
                        }
                    }
                    c.commit();
                    stmt.close();
                    c.close();
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                if (l) {
                    try {
                        Class.forName("org.postgresql.Driver");
                        c = DriverManager
                                .getConnection("jdbc:postgresql://localhost:5432/mess",
                                        "postgres", "123");
                        c.setAutoCommit(false);
                        System.out.println("Enter old password");
                        String password = sn.nextLine();
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery(
                                "SELECT password " +
                                        "FROM users " +
                                        "WHERE login = '" + login + "';");
                        while (rs.next()) {
                            String pas = rs.getString("password");
                            if (pas.equals(password)) {
                                f = true;
                            }
                        }
                        c.commit();
                        stmt.close();
                        c.close();
                        if (f) {
                            Class.forName("org.postgresql.Driver");
                            c = DriverManager
                                    .getConnection("jdbc:postgresql://localhost:5432/mess",
                                            "postgres", "123");
                            c.setAutoCommit(false);
                            stmt = c.createStatement();
                            System.out.println("Enter new password");
                            password = sn.nextLine();
                            String sql =
                                    "UPDATE users " +
                                            "SET password = '" + password + "' " +
                                            "WHERE login= '" + login + "';";
                            stmt.executeUpdate(sql);
                            c.commit();
                            stmt.close();
                            c.close();
                        } else {
                            System.out.println("Wrong password");
                        }
                    } catch (Exception e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                    System.out.println("Records created successfully");
                } else {
                    System.out.println("No in base");
                }
                break;
            case "/help":
                System.out.println("/login - Add new user");
                System.out.println("/update - Change password of user (you need to enter old password)");
                System.out.println("/delete - Delete user from base (you need to enter user password)");
                System.out.println("/stop - Stop server work");
                break;

        }

    }
}
