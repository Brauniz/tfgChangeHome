package com.example.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activities.R;
import com.example.activities.LogInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import entidades.Contact;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private ImageButton btnSignUp;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etName = findViewById(R.id.User);
        btnSignUp = findViewById(R.id.buttonSignUp);
        // Crear ProgressBar programáticamente ya que no está en el layout
        progressBar = new ProgressBar(this);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es requerido");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El email es requerido");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un email válido");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es requerida");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        // Mostrar progress bar
        if (progressBar.getParent() == null) {
            View rootView = findViewById(android.R.id.content);
            if (rootView instanceof ViewGroup) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                ((ViewGroup) rootView).addView(progressBar, params);
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Guardar información adicional del usuario en Firestore
                                saveUserToFirestore(user, name, email);
                            }
                        } else {
                            // Error en el registro
                            progressBar.setVisibility(View.GONE);
                            btnSignUp.setEnabled(true);

                            String errorMessage = "Error al crear la cuenta";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(SignUpActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email) {
        // Crear objeto Contact usando la clase definida
        Contact newContact = new Contact(name, email, user.getUid());

        // Opcional: Si el usuario tiene una foto de perfil de Google/Facebook
        if (user.getPhotoUrl() != null) {
            newContact.setProfileImageUrl(user.getPhotoUrl().toString());
        }

        // Guardar en Firestore
        db.collection("users")
                .document(user.getUid())
                .set(newContact)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        btnSignUp.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Usuario guardado exitosamente
                            // Cerrar sesión para que el usuario tenga que iniciar sesión manualmente
                            mAuth.signOut();

                            Toast.makeText(SignUpActivity.this,
                                    "Cuenta creada exitosamente. Por favor inicia sesión.",
                                    Toast.LENGTH_SHORT).show();

                            // Ir a LogInActivity
                            goToLoginActivity();

                        } else {
                            // Error al guardar en Firestore
                            Toast.makeText(SignUpActivity.this,
                                    "Error al guardar datos del usuario: " +
                                            (task.getException() != null ? task.getException().getMessage() : ""),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Opcional: Navegar a LoginActivity al presionar atrás
        super.onBackPressed();
        goToLoginActivity();
    }
}