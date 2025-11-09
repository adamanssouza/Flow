package com.example.flow.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flow.R;
import com.example.flow.data.Grupo;
import java.util.List;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Grupo> grupos;
    private final OnItemLongClickListener listener;

    public GrupoAdapter(List<Grupo> grupos, OnItemLongClickListener listener) {
        this.grupos = grupos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        Grupo grupo = grupos.get(position);
        holder.tvNomeGrupo.setText(grupo.getNome());
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(grupo);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
        notifyDataSetChanged();
    }

    static class GrupoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeGrupo = itemView.findViewById(R.id.tvNomeGrupo);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Grupo grupo);
    }
}
