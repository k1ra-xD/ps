package com.example.remotepccontroller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProcessesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProcessAdapter processAdapter;
    private List<ProcessItem> processList = new ArrayList<>();
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processes); // разметка для списка процессов

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewProcesses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        processAdapter = new ProcessAdapter(processList, process -> {
            // Здесь вызываем метод для убийства процесса
            Toast.makeText(this, "Убить процесс: " + process.name, Toast.LENGTH_SHORT).show();
            // Например, можно вызывать SendCommandTask как в MainActivity
        });

        recyclerView.setAdapter(processAdapter);

        fetchProcesses();
    }

    private void fetchProcesses() {
        // Тут получаем список процессов с сервера
        // И добавляем их в processList, затем:
        // processAdapter.notifyDataSetChanged();
    }
}
