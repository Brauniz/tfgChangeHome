package com.example.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import entidades.Vivienda;

public class CreateViviendaActivity extends AppCompatActivity {
    private TextInputEditText etTitulo, etDescripcion, etPrecio, etUbicacion;
    private ImageButton btnSubirImagen;
    private Button btnCancelar, btnGuardar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private List<Uri> imagenesSeleccionadas;
    private static final int PICK_IMAGES_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_vivienda_layout);
        
        initializeViews();
        setupFirebase();
        setupListeners();
    }

    private void initializeViews() {
        etTitulo = findViewById(R.id.et_titulo);
        etDescripcion = findViewById(R.id.et_descripcion);
        etPrecio = findViewById(R.id.et_precio);
        etUbicacion = findViewById(R.id.et_ubicacion);
        btnSubirImagen = findViewById(R.id.btn_subir_imagen);
        btnCancelar = findViewById(R.id.btn_cancelar);
        btnGuardar = findViewById(R.id.btn_guardar);
        
        imagenesSeleccionadas = new ArrayList<>();
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void setupListeners() {
        btnSubirImagen.setOnClickListener(v -> seleccionarImagenes());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarVivienda());
    }

    private void seleccionarImagenes() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            imagenesSeleccionadas.clear();
            
            if (data.getClipData() != null) {
                // Múltiples imágenes
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    imagenesSeleccionadas.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                // Una sola imagen
                imagenesSeleccionadas.add(data.getData());
            }
            
            Toast.makeText(this, imagenesSeleccionadas.size() + " imágenes seleccionadas", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarVivienda() {
        // Validar campos obligatorios
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || precio.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        if (!imagenesSeleccionadas.isEmpty()) {
            subirImagenesYGuardar(titulo, descripcion, precio, ubicacion);
        } else {
            guardarViviendaSinImagen(titulo, descripcion, precio, ubicacion);
        }
    }

    private void subirImagenesYGuardar(String titulo, String descripcion, String precio, String ubicacion) {
        String userId = auth.getCurrentUser().getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Solo subir la primera imagen por simplicidad (puedes modificar para múltiples)
        Uri primeraImagen = imagenesSeleccionadas.get(0);
        StorageReference imageRef = storage.getReference()
                .child("viviendas")
                .child(userId)
                .child(timestamp + ".jpg");

        imageRef.putFile(primeraImagen)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        guardarViviendaConImagen(titulo, descripcion, precio, ubicacion, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }

    private void guardarViviendaConImagen(String titulo, String descripcion, String precio, String ubicacion, String imageUrl) {
        String userId = auth.getCurrentUser().getUid();
        
        Vivienda vivienda = new Vivienda(imageUrl, titulo, precio, descripcion, ubicacion, userId);
        
        db.collection("viviendas")
                .add(vivienda)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Vivienda guardada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar vivienda", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }

    private void guardarViviendaSinImagen(String titulo, String descripcion, String precio, String ubicacion) {
        String userId = auth.getCurrentUser().getUid();
        
        Vivienda vivienda = new Vivienda("", titulo, precio, descripcion, ubicacion, userId);
        
        db.collection("viviendas")
                .add(vivienda)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Vivienda guardada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar vivienda", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar");
                });
    }
}