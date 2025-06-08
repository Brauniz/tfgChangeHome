package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activities.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import entidades.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    
    private List<Message> messagesList;
    private String currentUserId;
    
    public MessageAdapter(List<Message> messagesList, String currentUserId) {
        this.messagesList = messagesList;
        this.currentUserId = currentUserId;
    }
    
    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messagesList.size();
    }
    
    // ViewHolder para mensajes enviados
    private static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        TextView txtTime;
        
        SentMessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTime = itemView.findViewById(R.id.txt_time);
        }
        
        void bind(Message message) {
            txtMessage.setText(message.getMessage());
            txtTime.setText(formatTime(message.getTimestamp()));
        }
    }
    
    // ViewHolder para mensajes recibidos
    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        TextView txtTime;
        TextView txtSenderName;
        
        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtSenderName = itemView.findViewById(R.id.txt_sender_name);
        }
        
        void bind(Message message) {
            txtMessage.setText(message.getMessage());
            txtTime.setText(formatTime(message.getTimestamp()));
            txtSenderName.setText(message.getSenderName());
        }
    }
    
    // Método para formatear la hora
    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}