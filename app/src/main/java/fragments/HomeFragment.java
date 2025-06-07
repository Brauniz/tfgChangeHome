package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activities.CreateViviendaActivity;
import com.example.activities.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import adapters.ViviendaAdapter;
import entidades.Vivienda;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Vistas
    private RecyclerView recyclerViviendas;
    private FloatingActionButton fabCrearVivienda;
    private TextView tvEmptyState;

    // Adapter y datos
    private ViviendaAdapter adapter;
    private List<Vivienda> listaViviendas;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private ListenerRegistration viviendasListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Obtener usuario actual
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Inicializar lista
        listaViviendas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupFab();

        // Cargar viviendas solo si hay usuario autenticado
        if (currentUserId != null) {
            loadUserViviendas();
        } else {
            showEmptyState("Debes iniciar sesión para ver tus viviendas");
        }
    }

    private void initializeViews(View view) {
        recyclerViviendas = view.findViewById(R.id.propertyRecyclerView);
        fabCrearVivienda = view.findViewById(R.id.fab);

        // Si quieres agregar un TextView para estado vacío, puedes hacerlo aquí
        // tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new ViviendaAdapter(listaViviendas, getContext());
        recyclerViviendas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViviendas.setAdapter(adapter);
        recyclerViviendas.setHasFixedSize(true);
    }

    private void setupFab() {
        fabCrearVivienda.setOnClickListener(v -> {
            if (currentUserId != null) {
                Intent intent = new Intent(getActivity(), CreateViviendaActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(),
                        "Debes iniciar sesión para crear una vivienda",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserViviendas() {
        // Mostrar solo las viviendas creadas por el usuario actual
        viviendasListener = db.collection("viviendas")
                .whereEqualTo("creadorId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Más recientes primero
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al cargar viviendas: " + error.getMessage());
                        Toast.makeText(getContext(),
                                "Error al cargar tus viviendas",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        listaViviendas.clear();

                        for (DocumentSnapshot document : value.getDocuments()) {
                            try {
                                Vivienda vivienda = document.toObject(Vivienda.class);
                                if (vivienda != null) {
                                    vivienda.setDocumentId(document.getId());
                                    listaViviendas.add(vivienda);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear vivienda: " + e.getMessage());
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // Mostrar/ocultar mensaje de estado vacío
                        if (listaViviendas.isEmpty()) {
                            showEmptyState("No has creado ninguna vivienda aún");
                        } else {
                            hideEmptyState();
                        }
                    }
                });
    }

    private void showEmptyState(String message) {
        // Si tienes un TextView para estado vacío, muéstralo aquí
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        recyclerViviendas.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        recyclerViviendas.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener el listener para evitar memory leaks
        if (viviendasListener != null) {
            viviendasListener.remove();
        }
    }
}