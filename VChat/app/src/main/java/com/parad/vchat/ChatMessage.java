package com.parad.vchat;

/**
 * Created by parad on 17.11.2017.
 */

import android.annotation.SuppressLint;

import java.util.Random;

public class ChatMessage {
    public String body, sender, receiver, senderName;
    public String Date, Time;
    public String msgid;
    public boolean isMine;// Did I send the message.

    public ChatMessage(String Sender, String Receiver, String messageString, String ID,
                       boolean isMINE) {
        body = messageString;
        isMine = isMINE;
        sender = Sender;
        msgid = ID;
        receiver = Receiver;
        senderName = sender;
    }

    @SuppressLint("DefaultLocale")
    public void setMsgID() {
        msgid += "-" + String.format("%02d", new Random().nextInt(100));
    }
}