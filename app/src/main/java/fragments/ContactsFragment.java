package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activities.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.activities.R;
import adapters.ContactsAdapter;

import java.util.ArrayList;
import java.util.List;

import entidades.Contact;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ContactsAdapter adapter;
    private List<Contact> contactsList;
    private List<Contact> filteredList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String specificUserId = null; // Para mostrar un usuario específico

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar listas
        contactsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Verificar si viene un usuario específico
        if (getArguments() != null) {
            specificUserId = getArguments().getString("specificUserId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearchView();
        loadContacts();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.propertyRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
    }

    private void setupRecyclerView() {
        adapter = new ContactsAdapter(getContext(), filteredList, new ContactsAdapter.OnContactClickListener() {
            @Override
            public void onContactClick(Contact contact) {
                // Aquí puedes manejar el click en un contacto
                // Por ejemplo, abrir un chat o mostrar detalles
                openChat(contact);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadContacts() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Si hay un usuario específico, cargar solo ese usuario
        if (specificUserId != null && !specificUserId.isEmpty()) {
            loadSpecificUser(specificUserId);
        } else {
            // Cargar todos los contactos normalmente
            loadAllContacts(currentUserId);
        }
    }

    private void loadSpecificUser(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Contact contact = documentSnapshot.toObject(Contact.class);
                        if (contact != null) {
                            contactsList.clear();
                            contactsList.add(contact);

                            filteredList.clear();
                            filteredList.addAll(contactsList);
                            adapter.notifyDataSetChanged();

                            // Ocultar búsqueda cuando es un usuario específico
                            if (searchEditText != null) {
                                searchEditText.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuario específico: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar contacto", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAllContacts(String currentUserId) {
        // Mostrar búsqueda
        if (searchEditText != null) {
            searchEditText.setVisibility(View.VISIBLE);
        }

        db.collection("users")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al cargar contactos: " + error.getMessage());
                        Toast.makeText(getContext(), "Error al cargar contactos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        contactsList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Contact contact = doc.toObject(Contact.class);
                            if (contact != null) {
                                // No mostrar el usuario actual en la lista
                                if (!contact.getUid().equals(currentUserId)) {
                                    contactsList.add(contact);
                                }
                            }
                        }

                        // Actualizar la lista filtrada
                        filteredList.clear();
                        filteredList.addAll(contactsList);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void filterContacts(String searchText) {
        filteredList.clear();

        if (searchText.isEmpty()) {
            filteredList.addAll(contactsList);
        } else {
            String searchLower = searchText.toLowerCase().trim();

            for (Contact contact : contactsList) {
                if (contact.getName() != null &&
                        contact.getName().toLowerCase().contains(searchLower)) {
                    filteredList.add(contact);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void openChat(Contact contact) {
        // Abrir ChatActivity
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("receiverId", contact.getUid());
        intent.putExtra("receiverName", contact.getName());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Limpiar el campo de búsqueda al volver al fragment
        if (searchEditText != null && specificUserId == null) {
            searchEditText.setText("");
        }

        // Si volvemos y no hay usuario específico, recargar todos
        if (specificUserId != null) {
            specificUserId = null;
            loadContacts();
        }
    }
}