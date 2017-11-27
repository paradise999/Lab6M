package com.parad.vchat;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by parad on 19.11.2017.
 */

public class LaptopServer {
    public static final String LOG_TAG = "myServerApp";
    // ip адрес сервера, который принимает соединения
    private String mServerName = "192.168.0.100";
    // сокет, через которий приложения общается с сервером
    private Socket mSocket = null;

    LaptopServer() {
        mServerName = MainActivity.ip;
    }

    void openConnection() throws Exception {
    /* Освобождаем ресурсы */
        closeConnection();
        try {
        /*
            Создаем новый сокет. Указываем на каком компютере и порту запущен наш процесс,
            который будет принамать наше соединение.
        */
            int mServerPort = 6789;
            mSocket = new Socket(mServerName, mServerPort);
        } catch (IOException e) {
            throw new Exception(String.format("Невозможно создать сокет: %s", e.getMessage()));
        }
    }

    void closeConnection() {
    /* Проверяем сокет. Если он не зарыт, то закрываем его и освобдождаем соединение.*/
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, String.format("Невозможно закрыть сокет: %s", e.getMessage()));
            } finally {
                mSocket = null;
            }
        }
        mSocket = null;
    }

    public void sendData(byte[] data) throws Exception {
    /* Проверяем сокет. Если он не создан или закрыт, то выдаем исключение */
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Невозможно отправить данные. Сокет не создан или закрыт");
        }
    /* Отправка данных */
        try {
            mSocket.getOutputStream().write(data);
            mSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new Exception(String.format("Невозможно отправить данные: %s", e.getMessage()));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }
}