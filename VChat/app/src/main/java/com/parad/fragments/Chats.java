package com.parad.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parad.vchat.ChatAdapter;
import com.parad.vchat.ChatMessage;
import com.parad.vchat.CommonMethods;
import com.parad.vchat.MainActivity;
import com.parad.vchat.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.parad.security.AesCrypt;

import static com.parad.vchat.LaptopServer.LOG_TAG;

public class Chats extends Fragment implements OnClickListener {
    static String message = "Offline";
    public static EditText msg_edittext;
    public static String user1, user2;
    private static Random random;
    ArrayList<ChatMessage> chatlist;
    public static ChatAdapter chatAdapter;
    ListView msgListView;
    private static boolean key = true;
    static ArrayList<String> l = new ArrayList<>();

    public static void getdate(String str) {
        String[] s = new String[3];
        int i = 0;
        for (String retval : str.split(" ")) {
            if (i == 2 && s[i] != null)
                s[i] += " " + retval;
            if (s[i] == null)
                s[i] = retval;
            if (i < 2)
                i++;
        }
        key = s[0].equals(MainActivity.user);
        user1 = s[0];
        user2 = s[1];
        message = s[2];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        random = new Random();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");
        msg_edittext = view.findViewById(R.id.messageEditText);
        msgListView = view.findViewById(R.id.msgListView);
        ImageButton updateButton = view.findViewById(R.id.updateButton);
        updateButton.setVisibility(View.INVISIBLE);
        ImageButton sendButton = view.findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);
        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);
        chatlist = new ArrayList<>();
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public static void sendTextMessage(View v) {
        if (!message.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(user1, user2, message,
                    "" + random.nextInt(1000), key);
            chatMessage.setMsgID();
            chatMessage.body = message;
            chatMessage.Date = CommonMethods.getCurrentDate();
            chatMessage.Time = CommonMethods.getCurrentTime();
            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();
        }
    }

    public static void deleteTextMessage(View v) {
        chatAdapter.remove();
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                user1 = MainActivity.user;
                user2 = MainActivity.user2;
                String h = msg_edittext.getEditableText().toString();
                final String s = user1 + " " + user2 + " " + AesCrypt.encode(h);
                message = msg_edittext.getEditableText().toString();
                msg_edittext.setText("");
                /* Открываем соединение. Открытие должно происходить в отдельном потоке от ui */
                if (MainActivity.mServer == null) {
                    Log.e(LOG_TAG, "Сервер не создан");
                } else
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                    /* отправляем на сервер данные */
                                MainActivity.mServer.sendData(s.getBytes());
                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        }
                    }).start();
                key = true;
                sendTextMessage(v);
                break;
            case R.id.updateButton:
                if (message != null && MainActivity.user2 != null) {
                    v.setVisibility(View.INVISIBLE);
                    if (user2.equals(MainActivity.user)) {
                        Collections.addAll(l, message.split("::"));
                        for (int i = 0; i < l.size(); i+=2) {
                            message = AesCrypt.decode(l.get(i), l.get(i+1)).toString();
                            sendTextMessage(v);
                        }
                        if (MainActivity.mServer == null) {
                            Log.e(LOG_TAG, "Сервер не создан");
                        } else
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                    /* отправляем на сервер данные */
                                        String s = String.format("Read %s %s", MainActivity.user,
                                                MainActivity.user2);
                                        MainActivity.mServer.sendData(s.getBytes());
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, e.getMessage());
                                    }
                                }
                            }).start();
                    }
                }else{
                    Toast toast = Toast.makeText(getActivity(),
                            "Enter second user", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }
    }
}