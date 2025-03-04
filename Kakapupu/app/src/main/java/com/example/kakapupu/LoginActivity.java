package com.example.kakapupu;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEd, passwordEd;
    private Button signUp;
    private FirebaseAuth firebaseAuth;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        loginEd = findViewById(R.id.et_email);
        passwordEd = findViewById(R.id.et_password);
        signUp = findViewById(R.id.loginButton);

        signUp.setOnClickListener(view -> validateData());
    }

    public void goToRegister(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void validateData() {
        email = loginEd.getText().toString().trim();
        password = passwordEd.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEd.setError("Неподходящий почтовый адрес");
            loginEd.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            passwordEd.setError("Пароль не может быть пустым");
            passwordEd.requestFocus();
        } else {
            firebaseLogin();
        }
    }

    private void firebaseLogin() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}