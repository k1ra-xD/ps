package com.example.remotepccontroller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private EditText editIp, editPort, editPassword;
    private Button btnScreenshot, btnShutdown, btnProcesses, btnCursor, btnLock, btnRestart, btnMedia; // Все кнопки Button
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editIp = findViewById(R.id.editIp);
        editPort = findViewById(R.id.editPort);
        editPassword = findViewById(R.id.editPassword);
        btnScreenshot = findViewById(R.id.btnScreenshot);
        btnShutdown = findViewById(R.id.btnShutdown);
        btnProcesses = findViewById(R.id.btnProcesses);
        btnCursor = findViewById(R.id.btnCursor);
        btnLock = findViewById(R.id.btnLock);
        btnRestart = findViewById(R.id.btnRestart);

        sessionManager = new SessionManager(this);

        if (sessionManager.hasSession()) {
            editIp.setText(sessionManager.getIp());
            editPort.setText(String.valueOf(sessionManager.getPort()));
            editPassword.setText(sessionManager.getPassword());
        }

        btnScreenshot.setOnClickListener(v -> {
            saveSession();
            takeScreenshot();
        });

        btnShutdown.setOnClickListener(v -> {
            saveSession();
            showCustomDialog("Вы уверены, что хотите выключить ПК?", this::shutdownPC);
        });

        btnProcesses.setOnClickListener(v -> {
            saveSession();
            startActivity(new Intent(MainActivity.this, ProcessesActivity.class));
        });

        btnCursor.setOnClickListener(v -> {
            saveSession();
            startActivity(new Intent(MainActivity.this, CursorActivity.class));
        });

        btnRestart.setOnClickListener(v -> {
            saveSession();
            showCustomDialog("Вы уверены, что хотите перезагрузить ПК?", () -> {
                String ip = editIp.getText().toString().trim();
                String port = editPort.getText().toString().trim();
                String password = editPassword.getText().toString();
                new SendCommandTask(ip, port, password).execute("restart");
            });
        });

        btnLock.setOnClickListener(v -> {
            saveSession();
            showCustomDialog("Вы уверены, что хотите заблокировать ПК?", this::lockPC);
        });
    }

    private void saveSession() {
        String ip = editIp.getText().toString().trim();
        String portStr = editPort.getText().toString().trim();
        String password = editPassword.getText().toString();

        int port = 0;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}

        sessionManager.saveSession(ip, port, "user", password);
    }

    private void takeScreenshot() {
        String ip = editIp.getText().toString().trim();
        String port = editPort.getText().toString().trim();
        String password = editPassword.getText().toString();
        new SendCommandTask(ip, port, password).execute("screenshot");
    }

    private void shutdownPC() {
        String ip = editIp.getText().toString().trim();
        String port = editPort.getText().toString().trim();
        String password = editPassword.getText().toString();
        new SendCommandTask(ip, port, password).execute("shutdown");
    }

    private void lockPC() {
        String ip = editIp.getText().toString().trim();
        String port = editPort.getText().toString().trim();
        String password = editPassword.getText().toString();
        new SendCommandTask(ip, port, password).execute("lock");
    }

    private void showCustomDialog(String message, Runnable onConfirm) {
        LayoutInflater inflater = getLayoutInflater();
        final var dialogView = inflater.inflate(R.layout.dialog_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private class SendCommandTask extends AsyncTask<String, Void, Object> {
        String ip, port, password;
        String error = null;

        SendCommandTask(String ip, String port, String password) {
            this.ip = ip;
            this.port = port;
            this.password = password;
        }

        @Override
        protected Object doInBackground(String... params) {
            String command = params[0];
            int portNum;
            try {
                portNum = Integer.parseInt(port);
            } catch (Exception e) {
                error = "Неверный порт";
                return null;
            }

            try (Socket socket = new Socket(ip, portNum);
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

                socket.setSoTimeout(20000);

                dos.writeUTF(password);
                dos.flush();

                String authResp = dis.readUTF();
                if (!"AUTH_OK".equals(authResp) && !authResp.startsWith("CONNECTED")) {
                    error = "Auth failed: " + authResp;
                    return null;
                }

                dos.writeUTF(command);
                dos.flush();

                String resp = dis.readUTF();
                if ("IMG".equals(resp)) {
                    int len = dis.readInt();
                    byte[] bytes = new byte[len];
                    dis.readFully(bytes);
                    return bytes;
                } else {
                    return resp;
                }

            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (error != null) {
                Toast.makeText(MainActivity.this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                return;
            }

            if (result instanceof byte[]) {
                byte[] imgBytes = (byte[]) result;
                try {
                    File file = new File(getCacheDir(), "screenshot.png");
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(imgBytes);
                        out.flush();
                    }

                    Intent intent = new Intent(MainActivity.this, FullscreenImageActivity.class);
                    intent.putExtra("image_path", file.getAbsolutePath());
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Ошибка при обработке скриншота", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
