package com.example.kakapupu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private List<Log> logs;

    public LogAdapter(List<Log> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Log log = logs.get(position);
        holder.actionTv.setText(log.getAction());
        holder.adminTv.setText("Админ: " + log.getAdminId());
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date(log.getTimestamp()));
        holder.timestampTv.setText(date);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView actionTv, adminTv, timestampTv;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            actionTv = itemView.findViewById(R.id.log_action_tv);
            adminTv = itemView.findViewById(R.id.log_admin_tv);
            timestampTv = itemView.findViewById(R.id.log_timestamp_tv);
        }
    }
}