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
    private final OnItemLongClickListener listener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Categoria categoria);
    }

    public CategoriaAdapter(List<Categoria> categorias, OnItemLongClickListener listener) {
        this.categorias = categorias;
        this.listener = listener;
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
        holder.bind(categoria, listener);
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
        TextView tvNomeCategoria, tvValor, tvData;
        View indicator;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCategoria = itemView.findViewById(R.id.tvNomeCategoria);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvData = itemView.findViewById(R.id.tvData);
            indicator = itemView.findViewById(R.id.indicator);
        }

        public void bind(final Categoria categoria, final OnItemLongClickListener listener) {
            tvNomeCategoria.setText(categoria.getNome());
            tvData.setText(categoria.getData());
            
            String valorFormatado = String.format("R$ %.2f", categoria.getValor());
            tvValor.setText(valorFormatado);

            if ("receita".equals(categoria.getTipo())) {
                indicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                tvValor.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else {
                indicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                tvValor.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            }

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(categoria);
                }
                return true;
            });
        }
    }
}
