package com.example.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import entidades.Setting;

public class InitializeSettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "InitializeSettings";
    private FirebaseFirestore db;
    private Button btnInitialize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Crear un layout simple con un botón
        btnInitialize = new Button(this);
        btnInitialize.setText("Inicializar Configuraciones");
        setContentView(btnInitialize);
        
        db = FirebaseFirestore.getInstance();
        
        btnInitialize.setOnClickListener(v -> {
            checkAndCreateSettings();
        });
    }

    private void checkAndCreateSettings() {
        // Primero verificar si ya existen configuraciones
        db.collection("settings")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    // No hay configuraciones, crearlas
                    createSettingsInFirestore();
                } else {
                    Toast.makeText(this, "Las configuraciones ya existen", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al verificar configuraciones: " + e.getMessage());
            });
    }

    private void createSettingsInFirestore() {
        List<Setting> defaultSettings = new ArrayList<>();
        
        // Crear las opciones con iconos
        // Puedes usar URLs reales de iconos o dejar vacío el imageUrl
        defaultSettings.add(new Setting("Editar Perfil", ""));
        defaultSettings.add(new Setting("Notificaciones", ""));
        defaultSettings.add(new Setting("Privacidad", ""));
        defaultSettings.add(new Setting("Ayuda", ""));
        defaultSettings.add(new Setting("Acerca de", ""));
        defaultSettings.add(new Setting("Cerrar Sesión", ""));
        
        // Subir cada configuración a Firestore
        for (int i = 0; i < defaultSettings.size(); i++) {
            Setting setting = defaultSettings.get(i);
            
            db.collection("settings")
                .document("setting_" + i) // Usar IDs específicos
                .set(setting)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Configuración creada: " + setting.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear configuración: " + e.getMessage());
                });
        }
        
        Toast.makeText(this, "Configuraciones creadas exitosamente", Toast.LENGTH_LONG).show();
    }
}