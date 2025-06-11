package com.example.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.MessageAdapter;
import entidades.Contact;
import entidades.Message;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // Vistas
    private ImageButton fabBack;
    private TextView txtName;
    private RecyclerView recyclerMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    // Adapter
    private MessageAdapter messageAdapter;
    private List<Message> messagesList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ListenerRegistration messagesListener;

    // Datos del chat
    private String receiverId;
    private String receiverName;
    private String chatId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        // Obtener datos del intent
        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");

        if (receiverId == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupFirebase();
        loadCurrentUserName();
        setupRecyclerView();
        setupListeners();
        loadMessages();
    }

    private void initializeViews() {
        fabBack = findViewById(R.id.fab);
        txtName = findViewById(R.id.txtName);
        recyclerMessages = findViewById(R.id.recycler_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        // Establecer nombre del receptor
        txtName.setText(receiverName != null ? receiverName : "Usuario");
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Crear ID único para el chat (ordenado alfabéticamente)
        String[] ids = {currentUser.getUid(), receiverId};
        Arrays.sort(ids);
        chatId = ids[0] + "_" + ids[1];
    }

    private void loadCurrentUserName() {
        // Cargar nombre del usuario actual
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Contact userContact = documentSnapshot.toObject(Contact.class);
                        if (userContact != null) {
                            currentUserName = userContact.getName();
                        } else {
                            currentUserName = "Usuario";
                        }
                    } else {
                        currentUserName = "Usuario";
                    }
                })
                .addOnFailureListener(e -> {
                    currentUserName = "Usuario";
                });
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesList, currentUser.getUid());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Mostrar mensajes desde abajo

        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        // Botón atrás - volver al ContactsFragment
        fabBack.setOnClickListener(v -> finish());

        // Botón enviar
        btnSend.setOnClickListener(v -> sendMessage());

        // Enviar con Enter (opcional)
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessages() {
        // Escuchar mensajes en tiempo real
        messagesListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al cargar mensajes: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            Message message = dc.getDocument().toObject(Message.class);
                            message.setDocumentId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    messagesList.add(message);
                                    messageAdapter.notifyItemInserted(messagesList.size() - 1);
                                    recyclerMessages.scrollToPosition(messagesList.size() - 1);

                                    // Marcar como leído si es mensaje recibido
                                    if (!message.getSenderId().equals(currentUser.getUid()) && !message.isRead()) {
                                        markMessageAsRead(dc.getDocument().getId());
                                    }
                                    break;

                                case MODIFIED:
                                    // Actualizar mensaje modificado
                                    int modIndex = findMessageIndex(message.getDocumentId());
                                    if (modIndex != -1) {
                                        messagesList.set(modIndex, message);
                                        messageAdapter.notifyItemChanged(modIndex);
                                    }
                                    break;

                                case REMOVED:
                                    // Eliminar mensaje
                                    int remIndex = findMessageIndex(message.getDocumentId());
                                    if (remIndex != -1) {
                                        messagesList.remove(remIndex);
                                        messageAdapter.notifyItemRemoved(remIndex);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    private int findMessageIndex(String documentId) {
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).getDocumentId().equals(documentId)) {
                return i;
            }
        }
        return -1;
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Crear mensaje
        Message message = new Message(
                currentUser.getUid(),
                receiverId,
                currentUserName != null ? currentUserName : "Usuario",
                messageText
        );

        // Guardar en Firestore
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mensaje enviado");
                    etMessage.setText("");

                    // Actualizar última actividad del chat
                    updateChatLastMessage(messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje: " + e.getMessage());
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatLastMessage(String lastMessage) {
        // Actualizar información del chat
        Map<String, Object> chatInfo = new HashMap<>();
        chatInfo.put("lastMessage", lastMessage);
        chatInfo.put("lastMessageTime", System.currentTimeMillis());
        chatInfo.put("participants", Arrays.asList(currentUser.getUid(), receiverId));

        db.collection("chats")
                .document(chatId)
                .set(chatInfo)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar chat: " + e.getMessage());
                });
    }

    private void markMessageAsRead(String messageId) {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("read", true)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al marcar como leído: " + e.getMessage());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener listener
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}