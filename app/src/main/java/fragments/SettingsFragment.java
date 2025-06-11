package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.activities.InitializeTestDataActivity;
import com.example.activities.LogInActivity;
import com.example.activities.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import adapters.SettingsAdapter;
import entidades.Contact;
import entidades.Setting;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    // Vistas del perfil de usuario
    private ImageView imgProfile;
    private TextView txtName;
    private TextView txtId;

    // RecyclerView para las opciones
    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private List<Setting> settingsList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inicializar lista
        settingsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserProfile();
        setupRecyclerView();

        // TEMPORAL: Descomentar esta línea solo la primera vez para crear las configuraciones
        //createSettingsInFirestore(); // DESCOMENTA ESTA LÍNEA, EJECUTA LA APP UNA VEZ, Y LUEGO VUÉLVELA A COMENTAR

        loadSettings();
    }

    private void initViews(View view) {
        // Vistas del perfil
        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtName);
        txtId = view.findViewById(R.id.txtid);

        // RecyclerView
        recyclerView = view.findViewById(R.id.propertyRecyclerView);
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Cargar datos del usuario desde Firestore
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Contact userContact = documentSnapshot.toObject(Contact.class);
                            if (userContact != null) {
                                // Mostrar nombre
                                txtName.setText(userContact.getName());

                                // Mostrar email o ID
                                txtId.setText(userContact.getEmail());

                                // Cargar imagen de perfil
                                if (userContact.getProfileImageUrl() != null &&
                                        !userContact.getProfileImageUrl().isEmpty()) {
                                    Glide.with(this)
                                            .load(userContact.getProfileImageUrl())
                                            .placeholder(R.drawable.hunter)
                                            .error(R.drawable.hunter)
                                            .circleCrop()
                                            .into(imgProfile);
                                } else {
                                    imgProfile.setImageResource(R.drawable.hunter);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cargar perfil: " + e.getMessage());
                        // Mostrar datos básicos del usuario si falla la carga
                        txtName.setText("Usuario");
                        txtId.setText(currentUser.getEmail());
                    });
        }
    }

    private void setupRecyclerView() {
        adapter = new SettingsAdapter(getContext(), settingsList, new SettingsAdapter.OnSettingClickListener() {
            @Override
            public void onSettingClick(Setting setting) {
                handleSettingClick(setting);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadSettings() {
        // Cargar configuraciones desde Firestore
        db.collection("settings")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al cargar configuraciones: " + error.getMessage());
                        // Si hay error, crear configuraciones en Firestore y mostrar por defecto
                        createSettingsInFirestore();
                        createDefaultSettings();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        settingsList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Setting setting = doc.toObject(Setting.class);
                            if (setting != null) {
                                settingsList.add(setting);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        // Si no hay configuraciones, crear las por defecto en Firestore
                        createSettingsInFirestore();
                        createDefaultSettings(); // También mostrar temporalmente mientras se crean
                    }
                });
    }

    private void createDefaultSettings() {
        // Crear opciones de configuración por defecto si no existen en Firestore
        settingsList.clear();

        // Opción 1: Editar Perfil
        settingsList.add(new Setting("Editar Perfil", ""));

        // Opción 2: Notificaciones
        settingsList.add(new Setting("Notificaciones", ""));

        // Opción 3: Privacidad
        settingsList.add(new Setting("Privacidad", ""));

        // Opción 4: Ayuda
        settingsList.add(new Setting("Ayuda", ""));

        // Opción 5: Acerca de
        settingsList.add(new Setting("Acerca de", ""));

        // Opción 7: Cerrar Sesión
        settingsList.add(new Setting("Cerrar Sesión", ""));

        // Opción 6: Datos de Prueba (Desarrollo)
        settingsList.add(new Setting("Datos de Prueba", ""));


        adapter.notifyDataSetChanged();
    }

    private void handleSettingClick(Setting setting) {
        switch (setting.getName()) {
            case "Editar Perfil":
                // TODO: Abrir actividad/fragment de editar perfil
                Toast.makeText(getContext(), "Editar Perfil", Toast.LENGTH_SHORT).show();
                break;

            case "Notificaciones":
                // TODO: Abrir configuración de notificaciones
                Toast.makeText(getContext(), "Configurar Notificaciones", Toast.LENGTH_SHORT).show();
                break;

            case "Privacidad":
                // TODO: Abrir configuración de privacidad
                Toast.makeText(getContext(), "Configuración de Privacidad", Toast.LENGTH_SHORT).show();
                break;

            case "Ayuda":
                // TODO: Abrir sección de ayuda
                Toast.makeText(getContext(), "Centro de Ayuda", Toast.LENGTH_SHORT).show();
                break;

            case "Acerca de":
                // TODO: Mostrar información de la app
                Toast.makeText(getContext(), "ChangeHome v1.0", Toast.LENGTH_SHORT).show();
                break;

            case "Datos de Prueba":
                // Abrir actividad para crear datos de prueba
                abrirDatosDePrueba();
                break;

            case "Cerrar Sesión":
                showLogoutConfirmation();
                break;

            default:
                Toast.makeText(getContext(), "Opción: " + setting.getName(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void createSettingsInFirestore() {
        // Primero borrar todos los settings existentes
        db.collection("settings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Borrar cada documento existente
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Después de borrar, crear los nuevos
                    createNewSettings();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al borrar configuraciones existentes: " + e.getMessage());
                    // Intentar crear de todas formas
                    createNewSettings();
                });
    }

    private void createNewSettings() {
        List<Setting> defaultSettings = new ArrayList<>();

        // Crear las opciones básicas
        defaultSettings.add(new Setting("Editar Perfil", ""));
        defaultSettings.add(new Setting("Notificaciones", ""));
        defaultSettings.add(new Setting("Privacidad", ""));
        defaultSettings.add(new Setting("Ayuda", ""));
        defaultSettings.add(new Setting("Acerca de", ""));
        defaultSettings.add(new Setting("Datos de Prueba", ""));
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
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void abrirDatosDePrueba() {
        Intent intent = new Intent(getActivity(), InitializeTestDataActivity.class);
        startActivity(intent);
    }
}