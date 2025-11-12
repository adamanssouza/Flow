package com.example.flow.ui;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Grupo;
import com.example.flow.databinding.ActivityGrupoBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrupoActivity extends AppCompatActivity implements GrupoAdapter.OnItemLongClickListener {

    private ActivityGrupoBinding binding;
    private GrupoAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Grupo grupoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(getApplicationContext());

        setupRecyclerView();
        carregarGrupos();

        binding.btnSalvarGrupo.setOnClickListener(v -> salvarGrupo());

        if (getIntent().hasExtra("group_name_to_edit")) {
            String groupNameToEdit = getIntent().getStringExtra("group_name_to_edit");
            carregarGrupoParaEdicao(groupNameToEdit);
        }
    }

    private void carregarGrupoParaEdicao(String groupName) {
        executor.execute(() -> {
            Grupo grupo = db.grupoDao().getGrupoByName(groupName);
            if (grupo != null) {
                runOnUiThread(() -> {
                    binding.etNomeGrupo.setText(grupo.getNome());
                    binding.btnSalvarGrupo.setText("Atualizar");
                    grupoAtual = grupo;
                });
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvGrupos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GrupoAdapter(new ArrayList<>(), this);
        binding.rvGrupos.setAdapter(adapter);
    }

    private void carregarGrupos() {
        executor.execute(() -> {
            List<Grupo> grupos = db.grupoDao().getAllGrupos();
            runOnUiThread(() -> adapter.setGrupos(grupos));
        });
    }

    private void salvarGrupo() {
        String nomeGrupo = binding.etNomeGrupo.getText().toString().trim();
        if (nomeGrupo.isEmpty()) {
            binding.etNomeGrupo.setError("O nome do grupo não pode estar vazio");
            return;
        }

        executor.execute(() -> {
            if (grupoAtual == null) {
                Grupo novoGrupo = new Grupo(nomeGrupo);
                db.grupoDao().insert(novoGrupo);
            } else {
                String nomeAntigo = grupoAtual.getNome();
                grupoAtual.setNome(nomeGrupo);
                db.grupoDao().update(grupoAtual);
                db.categoriaDao().updateGroupName(nomeAntigo, nomeGrupo);
                grupoAtual = null;
            }
            runOnUiThread(() -> {
                binding.etNomeGrupo.setText("");
                binding.btnSalvarGrupo.setText("Salvar");
                carregarGrupos();
            });
        });
    }

    @Override
    public void onItemLongClick(Grupo grupo) {
        new AlertDialog.Builder(this)
                .setTitle("Opções do Grupo")
                .setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            binding.etNomeGrupo.setText(grupo.getNome());
                            binding.btnSalvarGrupo.setText("Atualizar");
                            grupoAtual = grupo;
                            break;
                        case 1: // Excluir
                            confirmarExclusao(grupo);
                            break;
                    }
                })
                .show();
    }

    private void confirmarExclusao(Grupo grupo) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Grupo")
                .setMessage("Tem certeza que deseja excluir este grupo?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    executor.execute(() -> {
                        db.categoriaDao().deleteByGroupName(grupo.getNome());
                        db.grupoDao().delete(grupo);
                        runOnUiThread(this::carregarGrupos);
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
