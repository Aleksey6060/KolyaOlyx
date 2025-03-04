package com.example.kakapupu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private EditText emailEt, passwordEt, roleEt;
    private RecyclerView usersRv;
    private Button addUserBtn;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private boolean isEditing = false;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        emailEt = findViewById(R.id.user_email);
        passwordEt = findViewById(R.id.user_password);
        roleEt = findViewById(R.id.user_role);
        usersRv = findViewById(R.id.users_rv);
        addUserBtn = findViewById(R.id.add_user_btn);

        usersRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this::deleteUser, this::editUser);
        usersRv.setAdapter(adapter);

        checkAdminRole();
        loadUsers();

        addUserBtn.setOnClickListener(v -> {
            if (isEditing) {
                updateUser();
            } else {
                addUser();
            }
        });
    }

    private void checkAdminRole() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists() || !"admin".equals(document.toObject(User.class).getRole())) {
                        Toast.makeText(this, "Доступ только для админов", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки роли: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    redirectToLogin();
                });
    }

    private void loadUsers() {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        userList.add(doc.toObject(User.class));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки пользователей: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String role = roleEt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || role.isEmpty() || (!role.equals("admin") && !role.equals("employee"))) {
            Toast.makeText(this, "Заполните email, пароль и роль (admin/employee)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            passwordEt.setError("Пароль должен быть не менее 6 символов");
            passwordEt.requestFocus();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        User user = new User(uid, email, role);
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    logAction("Добавлен пользователь: " + email);
                                    Toast.makeText(this, "Пользователь добавлен: " + email, Toast.LENGTH_SHORT).show();
                                    clearFields();
                                    loadUsers();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка сохранения данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    task.getResult().getUser().delete();
                                });
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Неизвестная ошибка";
                        Toast.makeText(this, "Ошибка создания пользователя: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUser() {
        String email = emailEt.getText().toString().trim();
        String role = roleEt.getText().toString().trim();

        if (email.isEmpty() || role.isEmpty() || (!role.equals("admin") && !role.equals("employee"))) {
            Toast.makeText(this, "Заполните email и роль (admin/employee)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            User updatedUser = new User(currentUser.getUid(), email, role);
            db.collection("users").document(currentUser.getUid()).set(updatedUser)
                    .addOnSuccessListener(aVoid -> {
                        logAction("Обновлён пользователь: " + email);
                        Toast.makeText(this, "Пользователь обновлён", Toast.LENGTH_SHORT).show();
                        resetToAddMode();
                        loadUsers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления пользователя: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void deleteUser(User user) {
        db.collection("users").document(user.getUid()).delete()
                .addOnSuccessListener(aVoid -> {
                    logAction("Удалён пользователь: " + user.getEmail());
                    Toast.makeText(this, "Пользователь удалён из базы данных", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void editUser(User user) {
        emailEt.setText(user.getEmail());
        roleEt.setText(user.getRole());
        passwordEt.setText("********"); // Показываем заглушку вместо пароля
        passwordEt.setEnabled(false);   // Делаем поле только для чтения
        passwordEt.setKeyListener(null); // Отключаем возможность ввода

        isEditing = true;
        currentUser = user;
        addUserBtn.setText("Изменить");
    }

    private void logAction(String action) {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        db.collection("logs").add(new Log(action, System.currentTimeMillis(), firebaseAuth.getCurrentUser().getUid()))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка логирования: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearFields() {
        emailEt.setText("");
        passwordEt.setText("");
        roleEt.setText("");
        passwordEt.setEnabled(true); // Включаем поле обратно для добавления
        passwordEt.setKeyListener(new android.text.method.TextKeyListener(android.text.method.TextKeyListener.Capitalize.NONE, false)); // Восстанавливаем ввод
    }

    private void resetToAddMode() {
        isEditing = false;
        currentUser = null;
        addUserBtn.setText("Добавить");
        clearFields();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Сессия истекла, войдите снова", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}   