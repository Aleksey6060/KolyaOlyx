package com.example.kakapupu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewLogsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private RecyclerView logsRv;
    private LogAdapter adapter;
    private List<Log> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_logs);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Проверка авторизации
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        logsRv = findViewById(R.id.logs_rv);
        logsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter(logList);
        logsRv.setAdapter(adapter);

        // Проверка роли администратора
        checkAdminRole();
    }

    private void checkAdminRole() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists() || !"admin".equals(document.toObject(User.class).getRole())) {
                        Toast.makeText(this, "Доступ только для админов", Toast.LENGTH_SHORT).show();
                        finish(); // Закрываем активность, если не админ
                    } else {
                        loadLogs(); // Загружаем логи только для админа
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки роли: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    redirectToLogin();
                });
    }

    private void loadLogs() {
        db.collection("logs").orderBy("timestamp").get()
                .addOnSuccessListener(querySnapshot -> {
                    logList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        logList.add(doc.toObject(Log.class));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки логов: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Сессия истекла, войдите снова", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}