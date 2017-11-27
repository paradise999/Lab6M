/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 * @author parad
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {
    private Socket socket;
    private static final int SERVERPORT = 6000;
    //private static final String SERVER_IP = "192.168.0.101";
    private String s = "";
    private static String ip;
    private static int port;


    Client(String l1, String l2, String l3) {
        s = l1;
        ip = l2; //ip smartphone
        port = Integer.parseInt(l3); //port smartphone
    }


    private void get(String k) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(k);
        } catch (Exception e) {
            System.out.printf("Connection error: %s%n", e.getMessage());
        }
    }


    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(ip);

            socket = new Socket(serverAddr, SERVERPORT);
            get(s);
            System.out.println("It send");

        } catch (IOException e1) {
            System.out.printf("Connection error: %s%n", e1.getMessage());
        }

    }
}

	

