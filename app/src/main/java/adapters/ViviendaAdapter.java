package adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.activities.R;

import java.util.List;

import entidades.Vivienda;

public class ViviendaAdapter extends RecyclerView.Adapter<ViviendaAdapter.ViviendaViewHolder> {
    private List<Vivienda> listaViviendas;
    private Context context;

    public ViviendaAdapter(List<Vivienda> listaViviendas, Context context) {
        this.listaViviendas = listaViviendas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViviendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
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
        
        // Cargar imagen con Glide (necesitarás agregar Glide a tu build.gradle)
        if (vivienda.getImagen() != null && !vivienda.getImagen().isEmpty()) {
            Glide.with(context)
                    .load(vivienda.getImagen())
                    .placeholder(R.drawable.hunter) // imagen por defecto
                    .error(R.drawable.error_icon) // imagen de error
                    .into(holder.imgVivienda);
        } else {
            // Si no hay imagen, mostrar placeholder
            holder.imgVivienda.setImageResource(R.drawable.hunter);
        }
        
        // Configurar botón contactar
        holder.btnContactar.setOnClickListener(v -> {
            // Aquí puedes implementar la lógica para contactar
            // Por ejemplo, abrir WhatsApp, enviar email, etc.
            Toast.makeText(context, "Contactar para: " + vivienda.getTitulo(), Toast.LENGTH_SHORT).show();
            
            // Ejemplo: abrir WhatsApp (opcional)
            // abrirWhatsApp(vivienda);
        });
        
        // Click en toda la tarjeta para ver detalles (opcional)
        holder.itemView.setOnClickListener(v -> {
            // Aquí puedes abrir una actividad de detalle de la vivienda
            // Intent intent = new Intent(context, DetalleViviendaActivity.class);
            // intent.putExtra("vivienda", vivienda);
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaViviendas.size();
    }

    // Método para actualizar la lista
    public void updateList(List<Vivienda> nuevaLista) {
        this.listaViviendas = nuevaLista;
        notifyDataSetChanged();
    }

    // Método opcional para abrir WhatsApp
    private void abrirWhatsApp(Vivienda vivienda) {
        try {
            String mensaje = "Hola, estoy interesado en: " + vivienda.getTitulo();
            String url = "https://wa.me/?text=" + Uri.encode(mensaje);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
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