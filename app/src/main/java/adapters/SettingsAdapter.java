package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.activities.R;

import java.util.List;

import entidades.Setting;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private Context context;
    private List<Setting> settingsList;
    private OnSettingClickListener listener;

    // Interface para manejar clicks
    public interface OnSettingClickListener {
        void onSettingClick(Setting setting);
    }

    public SettingsAdapter(Context context, List<Setting> settingsList, OnSettingClickListener listener) {
        this.context = context;
        this.settingsList = settingsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.setting_card, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        Setting setting = settingsList.get(position);
        
        // Establecer nombre
        holder.txtName.setText(setting.getName());
        
        // Cargar imagen/icono
        if (setting.getImageUrl() != null && !setting.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(setting.getImageUrl())
                .placeholder(R.drawable.unselected_settings) // Imagen por defecto
                .error(R.drawable.unselected_settings)
                .into(holder.icon);
        } else {
            // Si no hay imagen URL, usar icono por defecto
            holder.icon.setImageResource(R.drawable.unselected_settings);
        }
        
        // Manejar click en toda la tarjeta
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingClick(setting);
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public void updateList(List<Setting> newList) {
        settingsList.clear();
        settingsList.addAll(newList);
        notifyDataSetChanged();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView icon;
        TextView txtName;

        SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.settings_card);
            icon = itemView.findViewById(R.id.icon);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}