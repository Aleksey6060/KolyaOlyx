package com.example.kakapupu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private TextView welcomeText;
    private Button manageServicesButton, manageUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        welcomeText = findViewById(R.id.welcomeText);
        manageServicesButton = findViewById(R.id.manageServicesButton);
        manageUsersButton = findViewById(R.id.manageUsersButton);

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null && user.getRole() != null) {
                            String role = user.getRole();
                            if (role.equals("admin")) {
                                welcomeText.setText("Администратор");
                            } else {
                                welcomeText.setText("Пользователь");
                            }
                            setupUI(role);
                        } else {
                            handleError("Роль пользователя не определена");
                        }
                    } else {
                        handleError("Данные пользователя не найдены");
                    }
                })
                .addOnFailureListener(e -> handleError("Ошибка загрузки данных: " + e.getMessage()));

        manageServicesButton.setOnClickListener(v -> startActivity(new Intent(this, ManageServicesActivity.class)));
        manageUsersButton.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
    }

    private void setupUI(String role) {
        if (role.equals("admin")) {
            manageUsersButton.setVisibility(View.VISIBLE);
        }
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        firebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void logoutUser(View view) {
        firebaseAuth.signOut();
        Toast.makeText(this, "Вы вышли", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void viewLogs(View view) {
        startActivity(new Intent(this, ViewLogsActivity.class));
    }
}