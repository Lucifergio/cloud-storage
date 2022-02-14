package com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB;

    public interface AuthService {

        void start();

        void stop();

        boolean getLoginAndPass(String login, String pass);

        boolean checkLogin(String login);

        void insertUserBatch(String login, String pass);
    }
