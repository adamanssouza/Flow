package com.example.flow.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.Categoria;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Categoria> items = new ArrayList<>();
    private final OnTransactionLongClickListener longClickListener;
    private final Context context;

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Categoria categoria);
    }

    public TransactionAdapter(Context context, OnTransactionLongClickListener listener) {
        this.context = context;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Categoria currentItem = items.get(position);

        holder.tvName.setText(currentItem.getNome());
        holder.tvDate.setText(currentItem.getData());

        // Formatar valor e definir cor
        String formattedValue = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(currentItem.getValor());
        holder.tvAmount.setText(formattedValue);

        if ("receita".equals(currentItem.getTipo())) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        // Listener para clique longo
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTransactionLongClick(currentItem);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<Categoria> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_transaction_name);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}
