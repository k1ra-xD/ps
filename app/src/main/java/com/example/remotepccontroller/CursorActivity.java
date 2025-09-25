package com.example.remotepccontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class CursorActivity extends AppCompatActivity {

    private View touchPad;
    private Button btnLeftClick, btnRightClick, btnBack;
    private SessionManager sessionManager;

    private float lastX = 0, lastY = 0;
    private final float SPEED_FACTOR = 0.3f; // коэффициент скорости курсора (можно менять)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursor);

        sessionManager = new SessionManager(this);

        touchPad = findViewById(R.id.touchPad);
        btnLeftClick = findViewById(R.id.btnLeftClick);
        btnRightClick = findViewById(R.id.btnRightClick);
        btnBack = findViewById(R.id.btnBackCursor);

        btnBack.setOnClickListener(v -> finish());

        btnLeftClick.setOnClickListener(v -> sendMouseCommand("click:left"));
        btnRightClick.setOnClickListener(v -> sendMouseCommand("click:right"));

        touchPad.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dx = (x - lastX) * SPEED_FACTOR;
                    float dy = (y - lastY) * SPEED_FACTOR;

                    lastX = x;
                    lastY = y;

                    sendMouseCommand("move_cursor:" + dx + ":" + dy);
                    break;
            }
            return true;
        });
    }

    private void sendMouseCommand(String command) {
        String ip = sessionManager.getIp();
        int port = sessionManager.getPort();
        String password = sessionManager.getPassword();

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try (Socket socket = new Socket(ip, port);
                     DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                     DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

                    socket.setSoTimeout(10000);

                    // авторизация
                    dos.writeUTF(password);
                    dos.flush();
                    String authResp = dis.readUTF();
                    if (!"AUTH_OK".equals(authResp)) return "Auth failed";

                    // отправка команды
                    dos.writeUTF(params[0]);
                    dos.flush();

                    return dis.readUTF();

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }
        }.execute(command);
    }
}
