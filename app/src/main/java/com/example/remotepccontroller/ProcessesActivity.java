package com.example.remotepccontroller;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ProcessesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProcessAdapter processAdapter;
    private final List<ProcessItem> processList = new ArrayList<>();
    private Button btnBack, btnRefresh;

    private final Handler handler = new Handler();
    private SessionManager sessionManager;

    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            fetchProcesses();
            handler.postDelayed(this, 60000); // обновление каждые 10 сек
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processes);

        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> fetchProcesses());

        recyclerView = findViewById(R.id.recyclerViewProcesses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        processAdapter = new ProcessAdapter(processList, process -> {
            killProcess(process.pid);
            Toast.makeText(this, "Процесс завершён: " + process.name, Toast.LENGTH_SHORT).show();
            fetchProcesses();
        });

        recyclerView.setAdapter(processAdapter);

        fetchProcesses(); // первый запрос
    }

    private void fetchProcesses() {
        new Thread(() -> {
            List<ProcessItem> newList = getProcessesFromServer();
            runOnUiThread(() -> {
                processList.clear();
                if (newList != null) {
                    processList.addAll(newList);
                    processAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Список обновлён", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при получении списка", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private List<ProcessItem> getProcessesFromServer() {
        try (Socket socket = new Socket(sessionManager.getIp(), sessionManager.getPort());
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            socket.setSoTimeout(20000);

            // авторизация
            dos.writeUTF(sessionManager.getPassword());
            dos.flush();
            String authResp = dis.readUTF();
            if (!"AUTH_OK".equals(authResp)) {
                runOnUiThread(() -> Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show());
                return null;
            }

            // команда на получение процессов
            dos.writeUTF("processes"); // ✅ совпадает с сервером
            dos.flush();

            int count = dis.readInt();
            List<ProcessItem> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int pid = dis.readInt(); // ✅ читаем int
                String name = dis.readUTF();
                list.add(new ProcessItem(pid, name));
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            return null;
        }
    }

    private void killProcess(long pid) {
        new Thread(() -> {
            try (Socket socket = new Socket(sessionManager.getIp(), sessionManager.getPort());
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

                socket.setSoTimeout(20000);

                dos.writeUTF(sessionManager.getPassword());
                dos.flush();
                String authResp = dis.readUTF();
                if (!"AUTH_OK".equals(authResp)) return;

                dos.writeUTF("kill:" + pid); // ✅ совпадает с сервером
                dos.flush();
                String resp = dis.readUTF();
                runOnUiThread(() -> Toast.makeText(this, "Ответ сервера: " + resp, Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTask);
    }
}
