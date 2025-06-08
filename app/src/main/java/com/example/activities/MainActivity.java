package com.example.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import entidades.Setting;
import fragments.ContactsFragment;
import fragments.HomeFragment;
import fragments.SearchFragment;
import fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "AppPrefs";
    private static final String SETTINGS_INITIALIZED = "settings_initialized";

    private Fragment currentFragment;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Verificar si es la primera vez que se ejecuta la app
        checkAndInitializeSettings();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_menu);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            currentFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
        }
    }

    private void checkAndInitializeSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean settingsInitialized = prefs.getBoolean(SETTINGS_INITIALIZED, false);

        if (!settingsInitialized) {
            // Primera vez ejecutando la app, crear configuraciones
            createDefaultSettings();
        }
    }

    private void createDefaultSettings() {
        // Verificar primero si ya existen configuraciones
        db.collection("settings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No hay configuraciones, crearlas
                        List<Setting> defaultSettings = new ArrayList<>();

                        // Crear las opciones básicas
                        defaultSettings.add(new Setting("Editar Perfil", ""));
                        defaultSettings.add(new Setting("Notificaciones", ""));
                        defaultSettings.add(new Setting("Privacidad", ""));
                        defaultSettings.add(new Setting("Ayuda", ""));
                        defaultSettings.add(new Setting("Acerca de", ""));
                        defaultSettings.add(new Setting("Cerrar Sesión", ""));

                        // Subir cada configuración
                        for (int i = 0; i < defaultSettings.size(); i++) {
                            Setting setting = defaultSettings.get(i);

                            db.collection("settings")
                                    .document("setting_" + i)
                                    .set(setting)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Configuración creada: " + setting.getName());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al crear configuración: " + e.getMessage());
                                    });
                        }

                        // Marcar como inicializado
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putBoolean(SETTINGS_INITIALIZED, true);
                        editor.apply();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar configuraciones: " + e.getMessage());
                });
    }
    // Agregar este método en tu MainActivity:

    public void navigateToContactsWithUser(String userId) {
        // Crear el ContactsFragment con el usuario específico
        Bundle bundle = new Bundle();
        bundle.putString("specificUserId", userId);

        ContactsFragment contactsFragment = new ContactsFragment();
        contactsFragment.setArguments(bundle);

        // Cambiar al fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, contactsFragment)
                .addToBackStack(null)
                .commit();

        // Actualizar el BottomNavigation para mostrar el item correcto
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_menu);
        bottomNav.setSelectedItemId(R.id.nav_customers);

        currentFragment = contactsFragment;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int id = item.getItemId();

                    if (id == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (id == R.id.nav_customers) {
                        selectedFragment = new ContactsFragment();
                    } else if (id == R.id.nav_search) {
                        selectedFragment = new SearchFragment();
                    } else if (id == R.id.nav_settings) {
                        selectedFragment = new SettingsFragment();
                    } else {
                        return false;
                    }

                    // Evitar recargar el mismo fragment
                    if (currentFragment != null && currentFragment.getClass() == selectedFragment.getClass()) {
                        return true;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();

                    currentFragment = selectedFragment;
                    return true;
                }
            };
}