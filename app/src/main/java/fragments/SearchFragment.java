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
    private boolean dataLoaded = false;

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

        // NO cargar datos al inicio, mostrar mensaje inicial
        showInitialState();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.propertyRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Si no tienes el TextView en tu layout, puedes crearlo dinámicamente
        if (emptyStateText == null) {
            // Crear TextView programáticamente si no existe
            emptyStateText = new TextView(getContext());
            emptyStateText.setTextColor(getResources().getColor(android.R.color.white));
            emptyStateText.setTextSize(16);
            emptyStateText.setGravity(android.view.Gravity.CENTER);
            emptyStateText.setPadding(16, 32, 16, 32);

            // Agregarlo al layout padre
            ViewGroup parent = (ViewGroup) view;
            parent.addView(emptyStateText);
        }
    }

    private void setupRecyclerView() {
        adapter = new ViviendaAdapter(filteredViviendas, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Ocultar RecyclerView inicialmente
        recyclerView.setVisibility(View.GONE);
    }

    private void setupSearchView() {
        // Cambiar el hint para indicar que se busca por ciudad
        searchEditText.setHint("Buscar viviendas por ciudad...");

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();

                if (searchText.isEmpty()) {
                    // Si el campo está vacío, mostrar estado inicial
                    showInitialState();
                } else {
                    // Si hay texto, realizar búsqueda
                    if (!dataLoaded) {
                        // Primera búsqueda, cargar datos
                        loadAllViviendas(searchText);
                    } else {
                        // Datos ya cargados, solo filtrar
                        filterViviendas(searchText);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showInitialState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Ingresa una ciudad para buscar viviendas");
        filteredViviendas.clear();
        adapter.notifyDataSetChanged();
    }

    private void loadAllViviendas(String initialSearch) {
        // Mostrar mensaje de carga
        emptyStateText.setText("Buscando viviendas...");
        emptyStateText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // Cargar TODAS las viviendas de todos los usuarios
        db.collection("viviendas")
                .orderBy("ciudad", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allViviendas.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Vivienda vivienda = doc.toObject(Vivienda.class);
                        if (vivienda != null) {
                            vivienda.setDocumentId(doc.getId());
                            allViviendas.add(vivienda);
                        }
                    }

                    dataLoaded = true;

                    // Filtrar con el texto de búsqueda actual
                    filterViviendas(initialSearch);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar viviendas: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al buscar viviendas", Toast.LENGTH_SHORT).show();
                    emptyStateText.setText("Error al cargar viviendas");
                });
    }

    private void filterViviendas(String searchText) {
        filteredViviendas.clear();

        if (searchText.isEmpty()) {
            showInitialState();
            return;
        }

        // Filtrar por ciudad (case insensitive)
        String searchLower = searchText.toLowerCase().trim();

        for (Vivienda vivienda : allViviendas) {
            if (vivienda.getCiudad() != null &&
                    vivienda.getCiudad().toLowerCase().contains(searchLower)) {
                filteredViviendas.add(vivienda);
            }
        }

        adapter.notifyDataSetChanged();

        // Actualizar vista según resultados
        if (filteredViviendas.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No se encontraron viviendas en \"" + searchText + "\"");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Limpiar el campo de búsqueda y resetear el estado
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        showInitialState();

        // Resetear el flag de datos cargados para forzar recarga en próxima búsqueda
        dataLoaded = false;
        allViviendas.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Limpiar datos al salir del fragment para ahorrar memoria
        allViviendas.clear();
        filteredViviendas.clear();
        dataLoaded = false;
    }
}