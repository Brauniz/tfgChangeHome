package adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.activities.MainActivity;
import com.example.activities.R;

import fragments.ContactsFragment;

import java.util.List;

import entidades.Vivienda;

public class ViviendaAdapter extends RecyclerView.Adapter<ViviendaAdapter.ViviendaViewHolder> {

    private List<Vivienda> listaViviendas;
    private Context context;
    private OnViviendaClickListener listener;

    // Interface para manejar clicks (opcional)
    public interface OnViviendaClickListener {
        void onViviendaClick(Vivienda vivienda);
        void onContactarClick(Vivienda vivienda);
    }

    // Constructor básico
    public ViviendaAdapter(List<Vivienda> listaViviendas, Context context) {
        this.listaViviendas = listaViviendas;
        this.context = context;
    }

    // Constructor con listener
    public ViviendaAdapter(List<Vivienda> listaViviendas, Context context, OnViviendaClickListener listener) {
        this.listaViviendas = listaViviendas;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViviendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.viviendas_layout, parent, false);
        return new ViviendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViviendaViewHolder holder, int position) {
        Vivienda vivienda = listaViviendas.get(position);

        // Configurar textos
        holder.txtTitulo.setText(vivienda.getTitulo());
        holder.txtInfo.setText(vivienda.getSubtitulo());
        holder.txtDescripcion.setText(vivienda.getDescripcion());

        // Cargar imagen con Glide
        if (vivienda.getImagen() != null && !vivienda.getImagen().isEmpty()) {
            Glide.with(context)
                    .load(vivienda.getImagen())
                    .placeholder(R.drawable.hunter) // imagen mientras carga
                    .error(R.drawable.error_icon) // imagen si hay error
                    .centerCrop()
                    .into(holder.imgVivienda);
        } else {
            // Si no hay imagen, mostrar imagen por defecto
            holder.imgVivienda.setImageResource(R.drawable.hunter);
        }

        // Configurar botón contactar
        holder.btnContactar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactarClick(vivienda);
            } else {
                // Comportamiento por defecto: abrir ContactActivity
                abrirContactActivity(vivienda);
            }
        });

        // Click en toda la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViviendaClick(vivienda);
            } else {
                // Comportamiento por defecto
                mostrarDetalles(vivienda);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaViviendas != null ? listaViviendas.size() : 0;
    }

    // Método para actualizar la lista
    public void updateList(List<Vivienda> nuevaLista) {
        this.listaViviendas = nuevaLista;
        notifyDataSetChanged();
    }

    // Métodos privados para comportamiento por defecto
    private void abrirContactActivity(Vivienda vivienda) {
        // Usar el método de MainActivity para navegar
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.navigateToContactsWithUser(vivienda.getCreadorId());
        } else {
            // Si no es MainActivity, usar el método anterior
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;

                Bundle bundle = new Bundle();
                bundle.putString("specificUserId", vivienda.getCreadorId());

                ContactsFragment contactsFragment = new ContactsFragment();
                contactsFragment.setArguments(bundle);

                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, contactsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

    private void mostrarDetalles(Vivienda vivienda) {
        Toast.makeText(context,
                "Ver detalles de: " + vivienda.getTitulo(),
                Toast.LENGTH_SHORT).show();
    }

    // ViewHolder
    public static class ViviendaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVivienda;
        TextView txtTitulo, txtInfo, txtDescripcion;
        Button btnContactar;

        public ViviendaViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVivienda = itemView.findViewById(R.id.img_vivienda);
            txtTitulo = itemView.findViewById(R.id.txt_titulo);
            txtInfo = itemView.findViewById(R.id.txt_info);
            txtDescripcion = itemView.findViewById(R.id.txt_descripcion);
            btnContactar = itemView.findViewById(R.id.btn_contactar);
        }
    }
}