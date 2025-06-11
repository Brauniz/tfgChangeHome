package com.example.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InitializeTestDataActivity extends AppCompatActivity {

    private static final String TAG = "InitializeTestData";

    private FirebaseFirestore db;
    private Button btnInitialize;
    private ProgressBar progressBar;
    private TextView tvStatus;

    // Arrays para generar datos aleatorios
    private String[] nombres = {"Carlos", "María", "Juan", "Ana", "Pedro", "Laura", "Diego", "Sofia", "Miguel", "Elena"};
    private String[] apellidos = {"García", "Rodríguez", "Martínez", "López", "González", "Pérez", "Sánchez", "Ramírez", "Torres", "Flores"};
    private String[] ciudades = {"Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza", "Málaga", "Bilbao", "Murcia", "Palma", "Granada"};
    private String[] titulos = {"Piso céntrico", "Casa con jardín", "Ático con terraza", "Estudio moderno", "Chalet familiar", "Apartamento playa", "Loft industrial", "Dúplex nuevo", "Casa rural", "Piso reformado"};
    private String[] descripciones = {
            "Amplio y luminoso, perfectamente ubicado",
            "Ideal para familias, zona tranquila",
            "Vistas espectaculares, totalmente equipado",
            "Perfecto para estudiantes o profesionales",
            "Espacioso y con todas las comodidades",
            "A pocos metros de la playa, zona privilegiada",
            "Diseño moderno y funcional",
            "Construcción reciente, materiales de calidad",
            "Entorno natural, perfecta desconexión",
            "Completamente renovado, listo para entrar"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout simple
        setContentView(R.layout.activity_initialize_test_data);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnInitialize = findViewById(R.id.btnInitialize);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        progressBar.setVisibility(ProgressBar.GONE);
    }

    private void setupListeners() {
        btnInitialize.setOnClickListener(v -> {
            inicializarDatos();
        });
    }

    private void inicializarDatos() {
        btnInitialize.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvStatus.setText("Creando datos de prueba...");

        // Primero crear los 10 contactos
        crearContactos();
    }

    private void crearContactos() {
        List<Map<String, Object>> contactos = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String uid = "test_user_" + i;
            String nombre = nombres[i] + " " + apellidos[i];
            String email = nombres[i].toLowerCase() + "." + apellidos[i].toLowerCase() + "@example.com";

            Map<String, Object> contacto = new HashMap<>();
            contacto.put("uid", uid);
            contacto.put("name", nombre);
            contacto.put("email", email);
            contacto.put("profileImageUrl", ""); // Puedes agregar URLs de imágenes si quieres
            contacto.put("createdAt", System.currentTimeMillis());

            contactos.add(contacto);

            // Guardar en Firestore
            db.collection("users")
                    .document(uid)
                    .set(contacto)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Contacto creado: " + nombre);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al crear contacto: " + e.getMessage());
                    });
        }

        // Después de crear contactos, crear viviendas
        tvStatus.setText("Contactos creados. Creando viviendas...");
        crearViviendas();
    }

    private void crearViviendas() {
        Random random = new Random();
        int totalViviendas = 20;
        final int[] viviendasCreadas = {0};
        final int[] viviendasError = {0};

        // Crear 2 viviendas por cada contacto (10 contactos x 2 = 20 viviendas)
        for (int i = 0; i < 10; i++) {
            String creadorId = "test_user_" + i;

            for (int j = 0; j < 2; j++) {
                Map<String, Object> vivienda = new HashMap<>();

                // Datos aleatorios
                String ciudad = ciudades[random.nextInt(ciudades.length)];
                String titulo = titulos[random.nextInt(titulos.length)];
                String descripcion = descripciones[random.nextInt(descripciones.length)];
                String precio = (random.nextInt(1500) + 500) + "€/mes"; // Entre 500 y 2000€

                vivienda.put("ciudad", ciudad);
                vivienda.put("titulo", titulo);
                vivienda.put("subtitulo", precio);
                vivienda.put("descripcion", descripcion);
                vivienda.put("creadorId", creadorId);
                vivienda.put("imagen", ""); // Sin imagen por defecto
                vivienda.put("createdAt", System.currentTimeMillis());

                // Guardar en Firestore
                db.collection("viviendas")
                        .add(vivienda)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Vivienda creada: " + titulo + " en " + ciudad);
                            viviendasCreadas[0]++;

                            // Verificar si se completaron todas las viviendas
                            if (viviendasCreadas[0] + viviendasError[0] == totalViviendas) {
                                finalizarProceso();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al crear vivienda: " + e.getMessage());
                            viviendasError[0]++;

                            // Verificar si se completaron todas las viviendas
                            if (viviendasCreadas[0] + viviendasError[0] == totalViviendas) {
                                finalizarProceso();
                            }
                        });
            }
        }
    }

    private void finalizarProceso() {
        // Finalizar
        tvStatus.setText("Proceso completado: 10 contactos y 20 viviendas creadas");
        progressBar.setVisibility(ProgressBar.GONE);
        btnInitialize.setEnabled(true);

        Toast.makeText(this, "Datos de prueba creados exitosamente", Toast.LENGTH_LONG).show();

        // Cerrar la actividad después de 2 segundos
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        }, 2000);
    }
}