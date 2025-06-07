package com.example.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import entidades.Vivienda;

public class CreateViviendaActivity extends AppCompatActivity {

    private static final String TAG = "CreateVivienda";
    private static final int PICK_IMAGE_REQUEST = 1;

    // Vistas
    private TextInputEditText etCiudad, etTitulo, etSubtitulo, etDescripcion;
    private ImageButton btnSubirImagen;
    private Button btnCancelar, btnGuardar;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    // Variables para la imagen
    private Uri imagenSeleccionada = null;
    private boolean imagenCambiada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_vivienda_layout);

        initializeViews();
        setupFirebase();
        setupListeners();
    }

    private void initializeViews() {
        // TextInputEditText (el orden está basado en tu layout)
        etCiudad = findViewById(R.id.et_titulo); // Ciudad
        etTitulo = findViewById(R.id.et_descripcion); // Título
        etSubtitulo = findViewById(R.id.et_precio); // Subtítulo
        etDescripcion = findViewById(R.id.et_ubicacion); // Descripción

        // Botones
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        btnCancelar = findViewById(R.id.btn_cancelar);
        btnGuardar = findViewById(R.id.btn_guardar);

        // Progress bar (crear uno si no existe en el layout)
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnSubirImagen.setOnClickListener(v -> seleccionarImagen());

        btnCancelar.setOnClickListener(v -> {
            // Volver al HomeFragment
            finish();
        });

        btnGuardar.setOnClickListener(v -> guardarVivienda());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccionar imagen"),
                PICK_IMAGE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            imagenSeleccionada = data.getData();
            imagenCambiada = true;

            // Cambiar el icono o mostrar indicador de que hay imagen seleccionada
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarVivienda() {
        // Obtener valores de los campos
        String ciudad = etCiudad.getText().toString().trim();
        String titulo = etTitulo.getText().toString().trim();
        String subtitulo = etSubtitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // Validar campos obligatorios
        if (ciudad.isEmpty()) {
            etCiudad.setError("La ciudad es obligatoria");
            etCiudad.requestFocus();
            return;
        }

        if (titulo.isEmpty()) {
            etTitulo.setError("El título es obligatorio");
            etTitulo.requestFocus();
            return;
        }

        if (subtitulo.isEmpty()) {
            etSubtitulo.setError("El subtítulo es obligatorio");
            etSubtitulo.requestFocus();
            return;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("La descripción es obligatoria");
            etDescripcion.requestFocus();
            return;
        }

        // Deshabilitar botón y mostrar progreso
        mostrarProgreso(true);

        // Si hay imagen seleccionada, subirla primero
        if (imagenSeleccionada != null) {
            subirImagenYGuardar(ciudad, titulo, subtitulo, descripcion);
        } else {
            // Si no hay imagen, guardar directamente
            guardarViviendaEnFirestore(ciudad, titulo, subtitulo, descripcion, "");
        }
    }

    private void subirImagenYGuardar(String ciudad, String titulo, String subtitulo, String descripcion) {
        String userId = currentUser.getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nombreArchivo = "vivienda_" + userId + "_" + timestamp + ".jpg";

        // Referencia al Storage
        StorageReference imageRef = storage.getReference()
                .child("viviendas")
                .child(userId)
                .child(nombreArchivo);

        // Subir imagen
        imageRef.putFile(imagenSeleccionada)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Guardar vivienda con URL de imagen
                        guardarViviendaEnFirestore(ciudad, titulo, subtitulo, descripcion, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir imagen: " + e.getMessage());
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    mostrarProgreso(false);
                })
                .addOnProgressListener(snapshot -> {
                    // Opcional: mostrar progreso de subida
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d(TAG, "Progreso de subida: " + progress + "%");
                });
    }

    private void guardarViviendaEnFirestore(String ciudad, String titulo, String subtitulo,
                                            String descripcion, String imageUrl) {
        String userId = currentUser.getUid();

        // Crear objeto Vivienda
        Vivienda vivienda = new Vivienda(imageUrl, titulo, subtitulo, descripcion, ciudad, userId);

        // Agregar timestamp
        Map<String, Object> viviendaMap = new HashMap<>();
        viviendaMap.put("imagen", imageUrl);
        viviendaMap.put("titulo", titulo);
        viviendaMap.put("subtitulo", subtitulo);
        viviendaMap.put("descripcion", descripcion);
        viviendaMap.put("ciudad", ciudad);
        viviendaMap.put("creadorId", userId);
        viviendaMap.put("createdAt", System.currentTimeMillis());

        // Guardar en Firestore
        db.collection("viviendas")
                .add(viviendaMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Vivienda guardada con ID: " + documentReference.getId());
                    Toast.makeText(this, "Vivienda guardada exitosamente", Toast.LENGTH_SHORT).show();

                    // Volver al HomeFragment
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar vivienda: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar la vivienda", Toast.LENGTH_SHORT).show();
                    mostrarProgreso(false);
                });
    }

    private void mostrarProgreso(boolean mostrar) {
        btnGuardar.setEnabled(!mostrar);
        btnCancelar.setEnabled(!mostrar);
        btnSubirImagen.setEnabled(!mostrar);

        if (mostrar) {
            btnGuardar.setText("Guardando...");
        } else {
            btnGuardar.setText("Guardar");
        }
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás, es como cancelar
        super.onBackPressed();
        finish();
    }
}