package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.activities.ChatActivity;
import com.example.activities.R;

import java.util.List;

import entidades.Contact;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private Context context;
    private List<Contact> contactsList;
    private OnContactClickListener listener;

    // Interface para manejar clicks
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactsAdapter(Context context, List<Contact> contactsList, OnContactClickListener listener) {
        this.context = context;
        this.contactsList = contactsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactsList.get(position);

        // Establecer nombre
        holder.txtName.setText(contact.getName());

        // Establecer email (en lugar de teléfono)
        holder.txtPhone.setText(contact.getEmail());

        // Cargar imagen de perfil
        if (contact.getProfileImageUrl() != null && !contact.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(contact.getProfileImageUrl())
                    .placeholder(R.drawable.hunter) // Asegúrate de tener esta imagen
                    .error(R.drawable.error_icon)
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            // Si no hay imagen, mostrar imagen por defecto o inicial del nombre
            holder.imgProfile.setImageResource(R.drawable.hunter);

            // Opcional: Puedes crear un drawable con la inicial del nombre
            // setInitialImage(holder.imgProfile, contact.getName());
        }

        // Manejar click en el item completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            } else {
                // Abrir chat por defecto
                openChat(contact);
            }
        });

        // Manejar click en el icono de chat específicamente
        holder.imgChat.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            } else {
                // Abrir chat
                openChat(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public void updateList(List<Contact> newList) {
        contactsList.clear();
        contactsList.addAll(newList);
        notifyDataSetChanged();
    }

    private void openChat(Contact contact) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("receiverId", contact.getUid());
        intent.putExtra("receiverName", contact.getName());
        context.startActivity(intent);
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName;
        TextView txtPhone;
        ImageView imgChat;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            imgChat = itemView.findViewById(R.id.imgChat);
        }
    }
}