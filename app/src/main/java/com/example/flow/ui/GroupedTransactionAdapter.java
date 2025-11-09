package com.example.flow.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flow.R;
import com.example.flow.data.Categoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_TRANSACTION = 1;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<Categoria>> originalGroupedData = new LinkedHashMap<>();
    private Map<String, Boolean> groupExpandedState;
    private OnTransactionLongClickListener transactionListener;
    private OnGroupLongClickListener groupListener;

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Categoria categoria);
    }

    public interface OnGroupLongClickListener {
        void onGroupLongClick(GroupHeader groupHeader);
    }

    public GroupedTransactionAdapter(OnTransactionLongClickListener transactionListener, OnGroupLongClickListener groupListener) {
        this.transactionListener = transactionListener;
        this.groupListener = groupListener;
    }

    public void setData(Map<String, List<Categoria>> groupedData, Map<String, Boolean> groupExpandedState) {
        this.originalGroupedData = groupedData;
        this.groupExpandedState = groupExpandedState;
        rebuildItems();
        notifyDataSetChanged(); // Mantido aqui para a carga inicial de dados
    }

    public void toggleGroup(String groupName) {
        int headerPosition = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof GroupHeader && ((GroupHeader) items.get(i)).getGroupName().equals(groupName)) {
                headerPosition = i;
                break;
            }
        }

        if (headerPosition != -1) {
            boolean isExpanded = groupExpandedState.getOrDefault(groupName, false);
            groupExpandedState.put(groupName, !isExpanded);
            notifyItemChanged(headerPosition); // Atualiza o ícone de expandir/recolher

            if (!isExpanded) { // Se estava recolhido, agora expande
                List<Categoria> transactions = originalGroupedData.get(groupName);
                if (transactions != null && !transactions.isEmpty()) {
                    items.addAll(headerPosition + 1, transactions);
                    notifyItemRangeInserted(headerPosition + 1, transactions.size());
                }
            } else { // Se estava expandido, agora recolhe
                List<Categoria> transactions = originalGroupedData.get(groupName);
                if (transactions != null && !transactions.isEmpty()) {
                    items.subList(headerPosition + 1, headerPosition + 1 + transactions.size()).clear();
                    notifyItemRangeRemoved(headerPosition + 1, transactions.size());
                }
            }
        }
    }

    private void rebuildItems() {
        items.clear();
        for (String groupName : originalGroupedData.keySet()) {
            GroupHeader header = new GroupHeader(groupName, calculateGroupTotal(originalGroupedData.get(groupName)));
            items.add(header);
            if (groupExpandedState.getOrDefault(groupName, false)) {
                items.addAll(originalGroupedData.get(groupName));
            }
        }
    }

    private double calculateGroupTotal(List<Categoria> transactions) {
        double total = 0;
        for (Categoria t : transactions) {
            if ("receita".equals(t.getTipo())) {
                total += t.getValor();
            } else {
                total -= t.getValor();
            }
        }
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof GroupHeader) {
            return TYPE_GROUP;
        } else {
            return TYPE_TRANSACTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_group_header, parent, false);
            return new GroupViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_GROUP) {
            GroupHeader header = (GroupHeader) items.get(position);
            ((GroupViewHolder) holder).bind(header);
        } else {
            Categoria transaction = (Categoria) items.get(position);
            ((TransactionViewHolder) holder).bind(transaction);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupName;
        private TextView tvGroupTotal;
        private ImageView ivExpandIcon;

        GroupViewHolder(View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupTotal = itemView.findViewById(R.id.tvGroupTotal);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);

            ivExpandIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    toggleGroup(((GroupHeader) items.get(position)).getGroupName());
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && groupListener != null) {
                    groupListener.onGroupLongClick((GroupHeader) items.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(GroupHeader header) {
            tvGroupName.setText(header.getGroupName());
            tvGroupTotal.setText(String.format("R$ %.2f", header.getGroupTotal()));
            boolean isExpanded = groupExpandedState.getOrDefault(header.getGroupName(), false);
            ivExpandIcon.setRotation(isExpanded ? 90 : 0);
        }
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransactionName;
        private TextView tvTransactionAmount;
        private TextView tvTransactionDate;

        TransactionViewHolder(View itemView) {
            super(itemView);
            tvTransactionName = itemView.findViewById(R.id.tv_transaction_name);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && transactionListener != null) {
                    transactionListener.onTransactionLongClick((Categoria) items.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(Categoria transaction) {
            tvTransactionName.setText(transaction.getNome());
            tvTransactionDate.setText(transaction.getData());

            double value = transaction.getValor();
            if ("despesa".equals(transaction.getTipo())) {
                tvTransactionAmount.setText(String.format("- R$ %.2f", value));
                tvTransactionAmount.setTextColor(Color.RED);
            } else {
                tvTransactionAmount.setText(String.format("R$ %.2f", value));
                tvTransactionAmount.setTextColor(Color.GREEN);
            }
        }
    }

    public static class GroupHeader {
        private String groupName;
        private double groupTotal;

        public GroupHeader(String groupName, double groupTotal) {
            this.groupName = groupName;
            this.groupTotal = groupTotal;
        }

        public String getGroupName() {
            return groupName;
        }

        public double getGroupTotal() {
            return groupTotal;
        }
    }
}
