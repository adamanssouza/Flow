package com.example.flow.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.Grupo;

import java.util.List;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Grupo> grupos;
    private final OnGrupoClickListener listener;

    public interface OnGrupoClickListener {
        void onGrupoClick(Grupo grupo);
        void onGrupoLongClick(Grupo grupo);
    }

    public GrupoAdapter(List<Grupo> grupos, OnGrupoClickListener listener) {
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
        holder.bind(grupo, listener);
    }

    @Override
    public int getItemCount() {
        return grupos != null ? grupos.size() : 0;
    }

    public void setLista(List<Grupo> grupos) {
        this.grupos = grupos;
        notifyDataSetChanged();
    }

    static class GrupoViewHolder extends RecyclerView.ViewHolder {
        TextView nomeGrupo;
        ImageView iconeGrupo, setaExpandir;
        View resumoGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeGrupo = itemView.findViewById(R.id.nome_grupo);
            iconeGrupo = itemView.findViewById(R.id.icone_grupo);
            setaExpandir = itemView.findViewById(R.id.seta_expandir);
            resumoGrupo = itemView.findViewById(R.id.resumo_grupo);
        }

        public void bind(final Grupo grupo, final OnGrupoClickListener listener) {
            nomeGrupo.setText(grupo.getNome());

            // Definir cor do ícone
            try {
                iconeGrupo.setColorFilter(android.graphics.Color.parseColor(grupo.getCor()));
            } catch (Exception e) {
                iconeGrupo.setColorFilter(android.graphics.Color.parseColor("#2196F3")); // Cor padrão
            }

            // Clique simples - expandir/recolher (implementaremos depois)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGrupoClick(grupo);
                }
            });

            // Long press - editar/excluir
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onGrupoLongClick(grupo);
                }
                return true;
            });
        }
    }
}