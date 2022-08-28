package com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DbAuth implements AuthService {

    private static DbAuth instance;

    private static Connection connection;
    private static Statement statement;

    public static synchronized DbAuth getInstance() {
        if (instance == null) {
            instance = new DbAuth();
        }
        return instance;
    }

    private DbAuth() {
    }

    @Override
    public void start() {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:DB-Cloud.sqlite");
            statement = connection.createStatement();
            //dropTable();
            createTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void createTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (\n" +
                "        id    INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "        login  TEXT,\n" +
                "        pass TEXT\n" +
                "    );");
    }

    public void insertUserBatch(String login, String pass) {

        try (PreparedStatement ps = connection.prepareStatement(
                "insert into users (login, pass)" +
                        " values (?, ?)")) {

            ps.setString(1, login);
            ps.setString(2, pass);
            ps.addBatch();
            ps.executeBatch();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void dropTable() throws SQLException {
        statement.executeUpdate("drop table users");
    }

    @Override
    public boolean getLoginAndPass(String login, String pass) {

        try (PreparedStatement ps = connection.prepareStatement("SELECT count (*) FROM users WHERE login = ? and pass = ?")) {
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean checkLogin(String login) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT count (*) FROM users WHERE login = ?")) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                if (rs.getInt(1) > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
