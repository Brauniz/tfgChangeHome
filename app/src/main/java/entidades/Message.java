package entidades;

import com.google.firebase.firestore.DocumentId;

public class Message {
    @DocumentId
    private String documentId;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String message;
    private long timestamp;
    private boolean isRead;

    // Constructor vacío requerido por Firebase
    public Message() {}

    // Constructor con parámetros
    public Message(String senderId, String receiverId, String senderName, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters y Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}