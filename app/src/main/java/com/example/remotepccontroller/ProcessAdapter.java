package com.example.remotepccontroller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {

    public interface OnKillClickListener {
        void onKillClick(ProcessItem process);
    }

    private List<ProcessItem> processes;
    private final OnKillClickListener listener;

    public ProcessAdapter(List<ProcessItem> processes, OnKillClickListener listener) {
        this.processes = processes != null ? processes : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.process_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProcessItem proc = processes.get(position);
        holder.textView.setText(proc.name + " (PID: " + proc.pid + ")");
        holder.btnKill.setOnClickListener(v -> {
            if (listener != null) listener.onKillClick(proc);
        });
    }

    @Override
    public int getItemCount() {
        return processes.size();
    }

    // 🔥 Добавил метод для обновления списка
    public void updateList(List<ProcessItem> newProcesses) {
        this.processes = newProcesses != null ? newProcesses : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final Button btnKill;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvName);
            btnKill = itemView.findViewById(R.id.btnKill);
        }
    }
}
