package com.parad.vchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parad.fragments.Chats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static com.parad.vchat.LaptopServer.LOG_TAG;

public class MainActivity extends AppCompatActivity {
    private ServerSocket serverSocket;
    Chats myFragment;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    public static LaptopServer mServer = null;
    public static String user, user2;
    final Context context = this;
    static String s;
    static String ip = "192.168.0.100";
    static private Handler mHandler;
    static long startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myFragment = new Chats();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings1) {
            //Получаем вид с файла prompt.xml, который применим для диалогового окна:
            LayoutInflater li = LayoutInflater.from(context);
            @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.promt, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            //Настраиваем prompt.xml для нашего AlertDialog:
            builder.setView(promptsView);
            //Настраиваем отображение поля для ввода текста в открытом диалоге:
            final EditText userInput = promptsView.findViewById(R.id.input_text);
            builder
                    .setTitle("IP Server")
                    .setMessage("Enter server ip")
                    .setIcon(R.drawable.lock)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ip = userInput.getText().toString();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (mServer != null)
                mServer.closeConnection();
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final TextView textView = rootView.findViewById(R.id.textView3);
            textView.setTextColor(Color.WHITE);
            textView.setText("Hello!");
            textView.setVisibility(View.INVISIBLE);
            final EditText et2 = rootView.findViewById(R.id.editText2);
            final EditText et = rootView.findViewById(R.id.editText);
            et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et.setText("");
                }
            });
            Button bt = rootView.findViewById(R.id.button_open_connection);
            bt.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("HandlerLeak")
                @Override
                public void onClick(View v) {
                    if (textView.getVisibility() == View.INVISIBLE) {
                        if (mServer == null) {
                            mServer = new LaptopServer();
        /* Открываем соединение. Открытие должно происходить в отдельном потоке от ui */
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mServer.openConnection();
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, e.getMessage());
                                        mServer = null;
                                    }
                                }
                            }).start();
                        }
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mServer == null) {
                            Log.e(LOG_TAG, "Сервер не создан");
                        } else
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {   /* отправляем на сервер данные */
                                        user = et.getText().toString().replace(" ", "");
                                        s = String.format("Login %s %s", user,
                                                et2.getText().toString().replace(" ", ""));
                                        mServer.sendData(s.getBytes());
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, e.getMessage());
                                    }
                                }
                            }).start();
                    } else {
                        try {
                            user2 = et.getText().toString().replace(" ", "");
                            s = String.format("Check %s", user2);
                            mServer.sendData(s.getBytes());
                        } catch (Exception e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                        Chats.getdate(String.format("%s %s Online", user, user2));
                        if (mServer != null) {
                            startTime = System.currentTimeMillis();
                            mHandler = new Handler() {
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {  /* отправляем на сервер данные */
                                                String z = String.format("Send %s %s", user2, user);
                                                MainActivity.mServer.sendData(z.getBytes());
                                            } catch (Exception e) {
                                                Log.e(LOG_TAG, e.getMessage());
                                            }
                                        }
                                    }).start();
                                    mHandler.sendEmptyMessageDelayed(0, 10000);
                                }
                            };
                            mHandler.sendEmptyMessage(0);
                        }
                    }
                }
            });
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1)
                return new Chats();
            else
                return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateConversationHandler.post(new updateUIThread(read));
                    if (read == null) {
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private String msg;

        updateUIThread(String str) {
            this.msg = str;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (msg == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Server is offline, restart when it online", Toast.LENGTH_SHORT);
                toast.show();
            } else
                switch (msg) {
                    case "No in base":
                    case "Wrong password":
                    case "Online":
                    case "Message is taken":
                        Toast toast = Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case "Hello!":
                        TextView textView = findViewById(R.id.textView3);
                        textView.setVisibility(View.VISIBLE);
                        EditText et2 = findViewById(R.id.editText2);
                        et2.setVisibility(View.INVISIBLE);
                        TextView textView2 = findViewById(R.id.textView2);
                        TextView textView3 = findViewById(R.id.textView);
                        textView3.setVisibility(View.INVISIBLE);
                        textView2.setText("Enter login your partner");
                        Button bt = findViewById(R.id.button_open_connection);
                        bt.setText("Enter");
                        break;
                    case "Chat":
                        TextView textView1 = findViewById(R.id.textView3);
                        textView1.setText("Come to Chat");
                        break;
                    case "Nothing":
                        Chats.deleteTextMessage(myFragment.getView());
                        break;
                    default:
                        Chats.deleteTextMessage(myFragment.getView());
                        Chats.getdate(msg);
                        ImageButton ib = findViewById(R.id.updateButton);
                        if (user2 != null)
                            ib.setVisibility(View.VISIBLE);
                        break;
                }

        }
    }

}