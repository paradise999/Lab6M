/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author parad
 */
public class Server implements Runnable {

    private static volatile Server instane = null;
    /* Сокет, который обрабатывает соединения на сервере */
    private ServerSocket serverSoket = null;

    private Server() {
    }

    public static Server getServer() {
        if (instane == null) {
            synchronized (Server.class) {
                if (instane == null) {
                    instane = new Server();
                }
            }
        }
        return instane;
    }

    @Override
    public void run() {
        int SERVER_PORT = 6789;
        try {
            /* Создаем серверный сокет, которые принимает соединения */
            serverSoket = new ServerSocket(SERVER_PORT);
            System.out.printf("Start server on port: %d%n", SERVER_PORT);
            /*              * старт приема соединений на сервер              */
            while (true) {
                ConnectionWorker worker;
                try {
                    /* ждем нового соединения  */
                    worker = new ConnectionWorker(serverSoket.accept());
                    System.out.println("Get client connection");
                    /* создается новый поток, в котором обрабатывается соединение */
                    Thread t = new Thread(worker);
                    t.start();
                } catch (Exception e) {
                    System.out.printf("Connection error: %s%n", e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.printf("Cant start server on port %d:%s%n", SERVER_PORT, e.getMessage());
        } finally {
            /* Закрываем соединение */
            if (serverSoket != null) {
                try {
                    serverSoket.close();
                } catch (IOException e) {
                    System.out.printf("Cant start server on port %d:%s%n", SERVER_PORT, e.getMessage());
                }
            }
        }
    }
}
