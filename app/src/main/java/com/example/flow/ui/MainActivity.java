package com.example.flow.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionLongClickListener {

    private static final String PREFS_NAME = "FlowPrefs";
    private static final String BALANCE_CARD_EXPANDED = "balance_card_expanded";

    private TransactionAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvCategorias;
    private LinearLayout emptyStateLayout;
    private ImageView profileImageView, ivSearchIcon;
    private EditText etSearch;
    private TextView profileNameView;
    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldoTotal;
    private List<Categoria> allCategorias = new ArrayList<>();

    // --- Variáveis para o Card de Saldo ---
    private RelativeLayout balanceHeader;
    private ConstraintLayout balanceDetails;
    private ImageView ivBalanceToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Encontrar Componentes ---
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        rvCategorias = findViewById(R.id.rvCategorias);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        LinearLayout profileSection = findViewById(R.id.profile_section);
        profileImageView = findViewById(R.id.profile_image);
        profileNameView = findViewById(R.id.profile_name);
        tvTotalReceitas = findViewById(R.id.tv_total_receitas);
        tvTotalDespesas = findViewById(R.id.tv_total_despesas);
        tvSaldoTotal = findViewById(R.id.tv_saldo_total);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);
        etSearch = findViewById(R.id.etSearch);

        // --- Encontrar Componentes do Card de Saldo ---
        balanceHeader = findViewById(R.id.balance_header);
        balanceDetails = findViewById(R.id.balance_details);
        ivBalanceToggle = findViewById(R.id.iv_balance_toggle);

        // --- Inicializar Base de Dados ---
        db = AppDatabase.getInstance(getApplicationContext());

        // --- Configurar a Lista ---
        setupRecyclerView();

        // --- Carregar Dados Iniciais ---
        carregarCategorias();
        loadProfileData();
        setupBalanceCardToggle();

        // --- Configurar Cliques e Listeners ---
        btnAdd.setOnClickListener(v -> {
            AlertDialog.Builder newGroupBuilder = new AlertDialog.Builder(this);
            newGroupBuilder.setTitle("Novo Grupo");

            final EditText input = new EditText(this);
            input.setHint("Nome do Grupo");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(48, 16, 48, 0);
            input.setLayoutParams(lp);
            newGroupBuilder.setView(input);

            newGroupBuilder.setPositiveButton("Criar", (dialog, which) -> {
                // Ação de criar grupo desativada temporariamente.
                dialog.dismiss();
            });
            newGroupBuilder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

            newGroupBuilder.show();
        });

        profileSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        setupSearch();
    }

    private void setupBalanceCardToggle() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isExpanded = prefs.getBoolean(BALANCE_CARD_EXPANDED, true);
        updateBalanceCardView(isExpanded, false);

        balanceHeader.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean newExpandedState = !balanceDetails.isShown();
            editor.putBoolean(BALANCE_CARD_EXPANDED, newExpandedState);
            editor.apply();
            updateBalanceCardView(newExpandedState, true);
        });
    }

    private void updateBalanceCardView(boolean isExpanded, boolean animate) {
        if (animate) {
            ivBalanceToggle.animate().rotation(isExpanded ? 180 : 0).setDuration(300).start();
        } else {
            ivBalanceToggle.setRotation(isExpanded ? 180 : 0);
        }
        balanceDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, this);
        rvCategorias.setAdapter(adapter);
    }

    private void setupSearch() {
        ivSearchIcon.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.GONE) {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
                etSearch.setHint("Buscar Transação...");
            } else {
                etSearch.setVisibility(View.GONE);
                etSearch.setText("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTransactions(String query) {
        List<Categoria> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(allCategorias);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Categoria c : allCategorias) {
                if (c.getNome().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) {
                    filteredList.add(c);
                }
            }
        }
        adapter.setData(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        carregarCategorias();
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("profile", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "Perfil");
        String imageUriString = prefs.getString("imageUri", null);

        profileNameView.setText(name);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            profileImageView.setImageURI(imageUri);
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    private void carregarCategorias() {
        executor.execute(() -> {
            allCategorias = db.categoriaDao().getAllCategorias();

            Collections.sort(allCategorias, (c1, c2) -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(c1.getData());
                    Date date2 = sdf.parse(c2.getData());

                    int dateCompare = date2.compareTo(date1);
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return c1.getNome().compareToIgnoreCase(c2.getNome());
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            });

            runOnUiThread(() -> {
                adapter.setData(allCategorias);
                updateEmptyState(allCategorias.isEmpty());
                atualizarSaldo(allCategorias);
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        rvCategorias.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private String formatarValor(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private void atualizarSaldo(List<Categoria> categorias) {
        double totalReceitas = 0;
        double totalDespesas = 0;

        for (Categoria c : categorias) {
            if ("receita".equals(c.getTipo())) {
                totalReceitas += c.getValor();
            } else {
                totalDespesas += c.getValor();
            }
        }

        double saldoTotal = totalReceitas - totalDespesas;

        tvTotalReceitas.setText(formatarValor(totalReceitas));
        tvTotalDespesas.setText(formatarValor(totalDespesas));
        tvSaldoTotal.setText(formatarValor(saldoTotal));
    }

    @Override
    public void onTransactionLongClick(Categoria categoria) {
        new AlertDialog.Builder(this)
                .setTitle("Opções da Transação")
                .setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            Intent intent;
                            if ("receita".equals(categoria.getTipo())) {
                                intent = new Intent(MainActivity.this, ReceitaActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, DespesaActivity.class);
                            }
                            intent.putExtra("categoria_id", categoria.getId());
                            startActivity(intent);
                            break;
                        case 1: // Excluir
                            confirmarExclusaoTransacao(categoria);
                            break;
                    }
                })
                .show();
    }

    private void confirmarExclusaoTransacao(Categoria categoria) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Transação")
                .setMessage("Tem certeza que deseja excluir esta transação?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    executor.execute(() -> {
                        db.categoriaDao().delete(categoria);
                        runOnUiThread(this::carregarCategorias);
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        AppDatabase.destroyInstance();
    }
}
