package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activities.CreateViviendaActivity;
import com.example.activities.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import adapters.ViviendaAdapter;
import entidades.Vivienda;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViviendas;
    private FloatingActionButton btnCrearVivienda;
    private ViviendaAdapter adapter;
    private List<Vivienda> listaViviendas;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupFirebase();
        setupRecyclerView();
        loadUserViviendas();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerViviendas = view.findViewById(R.id.propertyRecyclerView); // ID correcto
        btnCrearVivienda = view.findViewById(R.id.fab);

        btnCrearVivienda.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateViviendaActivity.class);
            startActivity(intent);
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        listaViviendas = new ArrayList<>();
        adapter = new ViviendaAdapter(listaViviendas, getContext());
        recyclerViviendas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViviendas.setAdapter(adapter);
    }

    private void loadUserViviendas() {
        String currentUserId = auth.getCurrentUser().getUid();
        
        db.collection("viviendas")
                .whereEqualTo("creadorId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaViviendas.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Vivienda vivienda = document.toObject(Vivienda.class);
                        if (vivienda != null) {
                            vivienda.setDocumentId(document.getId());
                            listaViviendas.add(vivienda);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar viviendas", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserViviendas(); // Recargar cuando vuelve del CreateViviendaActivity
    }
}