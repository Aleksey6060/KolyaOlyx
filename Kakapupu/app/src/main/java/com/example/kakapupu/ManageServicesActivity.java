package com.example.kakapupu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageServicesActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private EditText nameEt, descEt, categoryEt, searchEt, filterEt;
    private RecyclerView servicesRv;
    private Button addServiceBtn;
    private ServiceAdapter adapter;
    private List<Service> serviceList = new ArrayList<>();
    private boolean isAdmin = false;
    private boolean isEditing = false;
    private Service currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        checkAdminRole();

        nameEt = findViewById(R.id.service_name);
        descEt = findViewById(R.id.service_desc);
        categoryEt = findViewById(R.id.service_category);
        searchEt = findViewById(R.id.search_et);
        filterEt = findViewById(R.id.filter_et);
        servicesRv = findViewById(R.id.services_rv);
        addServiceBtn = findViewById(R.id.add_service_btn);

        servicesRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(serviceList, this::deleteService, this::editService);
        servicesRv.setAdapter(adapter);

        loadServices();

        addServiceBtn.setOnClickListener(v -> {
            if (isEditing) {
                Toast.makeText(this, "Нажата кнопка Изменить", Toast.LENGTH_SHORT).show(); // Отладка
                updateService();
            } else {
                Toast.makeText(this, "Нажата кнопка Добавить", Toast.LENGTH_SHORT).show(); // Отладка
                addService();
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchServices(s.toString(), filterEt.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchServices(searchEt.getText().toString(), s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadServices() {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        db.collection("services").get()
                .addOnSuccessListener(querySnapshot -> {
                    serviceList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        serviceList.add(doc.toObject(Service.class));
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Услуги загружены, размер списка: " + serviceList.size(), Toast.LENGTH_SHORT).show(); // Отладка
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки услуг: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addService() {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        String name = nameEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String category = categoryEt.getText().toString().trim();
        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Заполните название и категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        String serviceId = UUID.randomUUID().toString();
        Service service = new Service(serviceId, name, desc, category);
        db.collection("services").document(serviceId).set(service)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Услуга добавлена", Toast.LENGTH_SHORT).show();
                    if (isAdmin) logAction("Добавлена услуга: " + name);
                    clearFields();
                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка добавления услуги: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateService() {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        String name = nameEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String category = categoryEt.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Заполните название и категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentService != null) {
            Toast.makeText(this, "Обновление услуги с ID: " + currentService.getServiceId(), Toast.LENGTH_SHORT).show(); // Отладка
            Service updatedService = new Service(currentService.getServiceId(), name, desc, category);
            db.collection("services").document(currentService.getServiceId()).set(updatedService)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Услуга обновлена", Toast.LENGTH_SHORT).show();
                        if (isAdmin) logAction("Обновлена услуга: " + name);
                        resetToAddMode();
                        loadServices();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления услуги: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "Ошибка: текущая услуга null", Toast.LENGTH_LONG).show(); // Отладка
        }
    }

    private void deleteService(Service service) {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        db.collection("services").document(service.getServiceId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                    if (isAdmin) logAction("Удалена услуга: " + service.getName());
                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка удаления услуги: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void editService(Service service) {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        nameEt.setText(service.getName());
        descEt.setText(service.getDescription());
        categoryEt.setText(service.getCategory());

        isEditing = true;
        currentService = service;
        addServiceBtn.setText("Изменить");
        Toast.makeText(this, "Редактирование услуги: " + service.getName(), Toast.LENGTH_SHORT).show(); // Отладка
    }

    private void searchServices(String search, String category) {
        if (firebaseAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        Query query = db.collection("services");
        if (!search.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("name", search)
                    .whereLessThanOrEqualTo("name", search + "\uf8ff");
        }
        if (!category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }
        query.get().addOnSuccessListener(querySnapshot -> {
                    serviceList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        serviceList.add(doc.toObject(Service.class));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка поиска услуг: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkAdminRole() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        isAdmin = "admin".equals(user.getRole());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки роли: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
        nameEt.setText("");
        descEt.setText("");
        categoryEt.setText("");
    }

    private void resetToAddMode() {
        isEditing = false;
        currentService = null;
        addServiceBtn.setText("Добавить");
        clearFields();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Сессия истекла, войдите снова", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}