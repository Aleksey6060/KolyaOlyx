package com.example.kakapupu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<Service> services;
    private Consumer<Service> onDelete;
    private Consumer<Service> onEdit;

    public ServiceAdapter(List<Service> services, Consumer<Service> onDelete, Consumer<Service> onEdit) {
        this.services = services;
        this.onDelete = onDelete;
        this.onEdit = onEdit;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.nameTv.setText(service.getName());
        holder.descTv.setText(service.getDescription());
        holder.categoryTv.setText(service.getCategory());
        holder.deleteBtn.setOnClickListener(v -> onDelete.accept(service));
        holder.editBtn.setOnClickListener(v -> onEdit.accept(service));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, descTv, categoryTv;
        Button deleteBtn, editBtn;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.service_name_tv);
            descTv = itemView.findViewById(R.id.service_desc_tv);
            categoryTv = itemView.findViewById(R.id.service_category_tv);
            deleteBtn = itemView.findViewById(R.id.delete_service_btn);
            editBtn = itemView.findViewById(R.id.edit_service_btn);
        }
    }
}