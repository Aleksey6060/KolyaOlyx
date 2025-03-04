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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private Consumer<User> onDelete;
    private Consumer<User> onEdit;

    public UserAdapter(List<User> users, Consumer<User> onDelete, Consumer<User> onEdit) {
        this.users = users;
        this.onDelete = onDelete;
        this.onEdit = onEdit;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.emailTv.setText(user.getEmail());
        holder.roleTv.setText(user.getRole());
        holder.deleteBtn.setOnClickListener(v -> onDelete.accept(user));
        holder.editBtn.setOnClickListener(v -> onEdit.accept(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTv, roleTv;
        Button deleteBtn, editBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTv = itemView.findViewById(R.id.user_email_tv);
            roleTv = itemView.findViewById(R.id.user_role_tv);
            deleteBtn = itemView.findViewById(R.id.delete_user_btn);
            editBtn = itemView.findViewById(R.id.edit_user_btn);
        }
    }
}