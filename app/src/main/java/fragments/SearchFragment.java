package fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activities.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import adapters.ViviendaAdapter;
import entidades.Vivienda;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private TextView emptyStateText;
    private ViviendaAdapter adapter;
    private List<Vivienda> allViviendas;
    private List<Vivienda> filteredViviendas;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Inicializar listas
        allViviendas = new ArrayList<>();
        filteredViviendas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSearchView();
        loadAllViviendas();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.propertyRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        
        // Agregar un TextView para mostrar cuando no hay resultados (opcional)
        // Si no existe en tu layout, puedes agregarlo o ignorar esta línea
        // emptyStateText = view.findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        adapter = new ViviendaAdapter(filteredViviendas, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        // Cambiar el hint para indicar que se busca por ciudad
        searchEditText.setHint("Buscar por ciudad...");
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterViviendas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllViviendas() {
        // Cargar TODAS las viviendas de todos los usuarios
        db.collection("viviendas")
            .orderBy("ciudad", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error al cargar viviendas: " + error.getMessage());
                    Toast.makeText(getContext(), "Error al cargar viviendas", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    allViviendas.clear();
                    
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Vivienda vivienda = doc.toObject(Vivienda.class);
                        if (vivienda != null) {
                            vivienda.setDocumentId(doc.getId());
                            allViviendas.add(vivienda);
                        }
                    }
                    
                    // Inicialmente mostrar todas las viviendas
                    filteredViviendas.clear();
                    filteredViviendas.addAll(allViviendas);
                    adapter.notifyDataSetChanged();
                    
                    // Actualizar estado vacío si es necesario
                    updateEmptyState();
                }
            });
    }

    private void filterViviendas(String searchText) {
        filteredViviendas.clear();
        
        if (searchText.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las viviendas
            filteredViviendas.addAll(allViviendas);
        } else {
            // Filtrar por ciudad (case insensitive)
            String searchLower = searchText.toLowerCase().trim();
            
            for (Vivienda vivienda : allViviendas) {
                if (vivienda.getCiudad() != null && 
                    vivienda.getCiudad().toLowerCase().contains(searchLower)) {
                    filteredViviendas.add(vivienda);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        // Mostrar u ocultar mensaje de "no hay resultados"
        if (filteredViviendas.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            // Si tienes un TextView para estado vacío, mostrarlo aquí
            // emptyStateText.setVisibility(View.VISIBLE);
            // emptyStateText.setText("No se encontraron viviendas en esta ciudad");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            // emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Limpiar el campo de búsqueda al volver al fragment
        if (searchEditText != null) {
            searchEditText.setText("");
        }
    }
}