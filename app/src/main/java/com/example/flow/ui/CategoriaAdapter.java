package com.example.flow.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.Categoria;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    private List<Categoria> categorias;

    public CategoriaAdapter(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.tvNomeCategoria.setText(categoria.getNome());

        // Define a cor do indicador com base no tipo
        if ("receita".equals(categoria.getTipo())) {
            holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        } else {
            holder.indicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return categorias != null ? categorias.size() : 0;
    }

    public void setLista(List<Categoria> categorias) {
        this.categorias = categorias;
        notifyDataSetChanged();
    }

    static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeCategoria;
        View indicator;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCategoria = itemView.findViewById(R.id.tvNomeCategoria);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
}
