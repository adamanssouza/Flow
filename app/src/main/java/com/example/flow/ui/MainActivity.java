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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Categoria;
import com.example.flow.data.Grupo;
import com.example.flow.ui.DespesaActivity;
import com.example.flow.ui.GrupoActivity;
import com.example.flow.ui.GroupedTransactionAdapter.GroupHeader;
import com.example.flow.ui.ProfileActivity;
import com.example.flow.ui.ReceitaActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GroupedTransactionAdapter.OnTransactionLongClickListener, GroupedTransactionAdapter.OnGroupLongClickListener {

    private static final String PREFS_NAME = "FlowPrefs";
    private static final String BALANCE_CARD_EXPANDED = "balance_card_expanded";

    private GroupedTransactionAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvCategorias;
    private LinearLayout emptyStateLayout, balanceDetails;
    private RelativeLayout balanceHeader;
    private ImageView profileImageView, ivSearchIcon, ivBalanceToggle;
    private EditText etSearch;
    private TextView profileNameView;
    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldoTotal;
    private Map<String, Boolean> groupExpandedState = new HashMap<>();
    private List<Categoria> allCategorias = new ArrayList<>();

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
            showAddOptionsDialog();
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
        updateBalanceCardView(isExpanded, false); // false para não animar na inicialização

        balanceHeader.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean newExpandedState = !balanceDetails.isShown();
            editor.putBoolean(BALANCE_CARD_EXPANDED, newExpandedState);
            editor.apply();
            updateBalanceCardView(newExpandedState, true); // true para animar no clique
        });
    }

    private void updateBalanceCardView(boolean isExpanded, boolean animate) {
        if (animate) {
            ivBalanceToggle.animate().rotation(isExpanded ? 0 : 180).setDuration(300).start();
            balanceDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        } else {
            ivBalanceToggle.setRotation(isExpanded ? 0 : 180);
            balanceDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showAddOptionsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_options, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog addOptionsDialog = builder.create();

        Button btnAddGrupo = dialogView.findViewById(R.id.btnAddGrupo);

        btnAddGrupo.setOnClickListener(v -> {
            addOptionsDialog.dismiss();

            AlertDialog.Builder newGroupBuilder = new AlertDialog.Builder(this);
            newGroupBuilder.setTitle("Novo Grupo");

            final EditText input = new EditText(this);
            input.setHint("Nome do Grupo");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            newGroupBuilder.setView(input);

            newGroupBuilder.setPositiveButton("Criar", (dialog, which) -> {
                String newGroupName = input.getText().toString().trim();
                if (!newGroupName.isEmpty()) {
                    executor.execute(() -> {
                        Grupo existingGroup = db.grupoDao().getGrupoByName(newGroupName);
                        if (existingGroup == null) {
                            db.grupoDao().insert(new Grupo(newGroupName));
                        }
                    });
                    mostrarDialogoAdicionarTransacao(newGroupName);
                }
            });
            newGroupBuilder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

            newGroupBuilder.show();
        });

        addOptionsDialog.show();
    }

    private void setupRecyclerView() {
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupedTransactionAdapter(this, this);
        rvCategorias.setAdapter(adapter);
    }

    private void setupSearch() {
        ivSearchIcon.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.GONE) {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
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
                filterGroups(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterGroups(String query) {
        Map<String, List<Categoria>> filteredData = new LinkedHashMap<>();
        if (query.isEmpty()) {
            filteredData = groupData(allCategorias);
        } else {
            for (Categoria c : allCategorias) {
                String groupName = c.getGrupo() != null ? c.getGrupo() : "Sem Grupo";
                if (groupName.toLowerCase().contains(query.toLowerCase())) {
                    if (!filteredData.containsKey(groupName)) {
                        filteredData.put(groupName, new ArrayList<>());
                    }
                    filteredData.get(groupName).add(c);
                }
            }
        }
        adapter.setData(filteredData, groupExpandedState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        carregarCategorias(); // Recarrega os dados ao voltar para a tela
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("profile", Context.MODE_PRIVATE);
        String name = prefs.getString("name", null);
        String imageUriString = prefs.getString("imageUri", null);

        if (name != null && !name.isEmpty()) {
            profileNameView.setText(name);
        } else {
            profileNameView.setText(R.string.profile);
        }

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
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

            Map<String, List<Categoria>> groupedData = groupData(allCategorias);

            runOnUiThread(() -> {
                adapter.setData(groupedData, groupExpandedState);
                updateEmptyState(allCategorias.isEmpty());
                atualizarSaldo(allCategorias);
            });
        });
    }

    private Map<String, List<Categoria>> groupData(List<Categoria> categorias) {
        Map<String, List<Categoria>> groupedMap = new LinkedHashMap<>();
        for (Categoria c : categorias) {
            String groupName = c.getGrupo() != null ? c.getGrupo() : "Sem Grupo";
            if (!groupedMap.containsKey(groupName)) {
                groupedMap.put(groupName, new ArrayList<>());
            }
            groupedMap.get(groupName).add(c);
        }
        return groupedMap;
    }

    private void updateEmptyState(boolean isEmpty) {
        rvCategorias.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private String formatarValor(double valor) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        if (valor >= 1000 || valor <= -1000) {
            String formattedValue = format.format(valor).replace("R$", "").trim();
            return "R$\n" + formattedValue;
        } else {
            return format.format(valor);
        }
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
    
    private void mostrarDialogoAdicionarTransacao(String groupName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.float_categoria, null);
        Button btnReceitas = dialogView.findViewById(R.id.btnReceitas);
        Button btnDespesas = dialogView.findViewById(R.id.btnDespesas);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        btnReceitas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReceitaActivity.class);
            intent.putExtra("group_name", groupName);
            startActivity(intent);
            dialog.dismiss();
        });

        btnDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DespesaActivity.class);
            intent.putExtra("group_name", groupName);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
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
    public void onGroupLongClick(GroupHeader groupHeader) {
        new AlertDialog.Builder(this)
                .setTitle("Opções do Grupo")
                .setItems(new CharSequence[]{"Adicionar Transação", "Excluir Grupo"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Adicionar Transação
                            mostrarDialogoAdicionarTransacao(groupHeader.getGroupName());
                            break;
                        case 1: // Excluir Grupo
                            confirmarExclusaoGrupo(groupHeader.getGroupName());
                            break;
                    }
                })
                .show();
    }

    private void confirmarExclusaoGrupo(String groupName) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Grupo")
                .setMessage("Tem certeza que deseja excluir o grupo '" + groupName + "' e todas as suas transações?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    executor.execute(() -> {
                        Grupo grupoParaDeletar = db.grupoDao().getGrupoByName(groupName);
                        if (grupoParaDeletar != null) {
                            db.grupoDao().delete(grupoParaDeletar);
                        }
                        db.categoriaDao().deleteByGroupName(groupName);
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
