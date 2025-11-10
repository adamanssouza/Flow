package com.example.flow.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Categoria;
import com.example.flow.data.Grupo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements CategoriaAdapter.OnItemLongClickListener, GrupoAdapter.OnGrupoClickListener {

    private CategoriaAdapter adapter;
    private GrupoAdapter grupoAdapter; // Bruno: Adapter para grupos
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvCategorias;
    private RecyclerView rvGrupos; // Bruno: RecyclerView para grupos
    private LinearLayout emptyStateLayout;
    private ImageView profileImageView;
    private TextView profileNameView;
    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldoTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Encontrar Componentes ---
        FloatingActionButton btnAddCategoria = findViewById(R.id.btnAddCategoria);
        Button btnAddGrupo = findViewById(R.id.btnAddGrupo); // Bruno: Botão de adicionar grupo
        rvCategorias = findViewById(R.id.rvCategorias);
        rvGrupos = findViewById(R.id.rvGrupos); // Bruno: RecyclerView para grupos
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        LinearLayout profileSection = findViewById(R.id.profile_section);
        profileImageView = findViewById(R.id.profile_image);
        profileNameView = findViewById(R.id.profile_name);
        tvTotalReceitas = findViewById(R.id.tv_total_receitas);
        tvTotalDespesas = findViewById(R.id.tv_total_despesas);
        tvSaldoTotal = findViewById(R.id.tv_saldo_total);

        // --- Inicializar Base de Dados ---
        db = AppDatabase.getInstance(getApplicationContext());

        // --- Configurar a Lista ---
        setupRecyclerView();
        setupGruposRecyclerView(); // Bruno: Configurar lista de grupos

        // --- Carregar Dados Iniciais ---
        carregarCategorias();
        loadProfileData();

        // --- Configurar Cliques ---
        btnAddCategoria.setOnClickListener(v -> mostrarDialogoSelecao());
        btnAddGrupo.setOnClickListener(v -> mostrarDialogoCriarGrupo()); // NOVO: Clique do botão grupo
        profileSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupGruposRecyclerView() {
        rvGrupos.setLayoutManager(new LinearLayoutManager(this));
        grupoAdapter = new GrupoAdapter(new ArrayList<>(), this);
        rvGrupos.setAdapter(grupoAdapter);
    }
    private void setupRecyclerView() {
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoriaAdapter(new ArrayList<>(), this);
        rvCategorias.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        carregarCategorias(); // Recarrega os dados ao voltar para a tela
        carregarGrupos(); // NOVO: Carregar grupos também
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

    private void carregarGrupos() {
        executor.execute(() -> {
            List<Grupo> grupos = db.grupoDao().getAllGrupos();
            runOnUiThread(() -> {
                grupoAdapter.setLista(grupos);
                // Mostrar/ocultar lista baseado se há grupos
                rvGrupos.setVisibility(grupos.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }
    private void carregarCategorias() {
        executor.execute(() -> {
            List<Categoria> categorias = db.categoriaDao().getAllCategorias();
            runOnUiThread(() -> {
                adapter.setLista(categorias);
                updateEmptyState(categorias.isEmpty());
                atualizarSaldo(categorias);
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        rvCategorias.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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

        tvTotalReceitas.setText(String.format("R$ %.2f", totalReceitas));
        tvTotalDespesas.setText(String.format("R$ %.2f", totalDespesas));
        tvSaldoTotal.setText(String.format("R$ %.2f", saldoTotal));
    }

    private void mostrarDialogoCriarGrupo() {
        // Dialog para criar novo grupo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_criar_grupo, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText edtNomeGrupo = dialogView.findViewById(R.id.edtNomeGrupo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarGrupo);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvarGrupo);

        // Por padrão, selecionar a cor azul
        final String[] corSelecionada = {"#2196F3"};

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnSalvar.setOnClickListener(v -> {
            String nomeGrupo = edtNomeGrupo.getText().toString().trim();
            if (nomeGrupo.isEmpty()) {
                Toast.makeText(this, "Digite um nome para o grupo!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Criar e salvar o grupo
            Grupo novoGrupo = new Grupo(nomeGrupo, corSelecionada[0], "ic_folder");
            executor.execute(() -> {
                db.grupoDao().insert(novoGrupo);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Grupo criado!", Toast.LENGTH_SHORT).show();
                    carregarGrupos();
                });
            });

            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoOpcoesGrupo(Grupo grupo) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        TextView tvOptionNome = dialogView.findViewById(R.id.tvOptionNome);
        Button btnEditar = dialogView.findViewById(R.id.btnEditar);
        Button btnExcluir = dialogView.findViewById(R.id.btnExcluir);

        // Esconder campos não usados para grupos
        dialogView.findViewById(R.id.tvOptionValor).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tvOptionData).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tvOptionMetodo).setVisibility(View.GONE);
        dialogView.findViewById(R.id.tvOptionNota).setVisibility(View.GONE);

        tvOptionNome.setText("Grupo: " + grupo.getNome());

        btnEditar.setOnClickListener(v -> {
            // Abrir dialog de edição (reutilizar o mesmo dialog de criação)
            mostrarDialogoEditarGrupo(grupo);
            dialog.dismiss();
        });

        btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir Grupo")
                    .setMessage("Tem certeza que deseja excluir o grupo \"" + grupo.getNome() + "\"?")
                    .setPositiveButton("Excluir", (d, which) -> {
                        executor.execute(() -> {
                            db.grupoDao().delete(grupo);
                            carregarGrupos(); // Recarregar lista
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoEditarGrupo(Grupo grupo) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_criar_grupo, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        EditText edtNomeGrupo = dialogView.findViewById(R.id.edtNomeGrupo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarGrupo);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvarGrupo);

        // Preencher com dados atuais
        edtNomeGrupo.setText(grupo.getNome());
        final String[] corSelecionada = {grupo.getCor()};

        // TODO: No próximo card - implementar seleção de cor no edit também

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnSalvar.setOnClickListener(v -> {
            String nomeGrupo = edtNomeGrupo.getText().toString().trim();
            if (nomeGrupo.isEmpty()) {
                Toast.makeText(this, "Digite um nome para o grupo!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Atualizar grupo
            grupo.setNome(nomeGrupo);
            grupo.setCor(corSelecionada[0]);
            executor.execute(() -> {
                db.grupoDao().update(grupo);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Grupo atualizado!", Toast.LENGTH_SHORT).show();
                    carregarGrupos(); // Recarregar lista
                });
            });

            dialog.dismiss();
        });

        dialog.show();
    }
    private void mostrarDialogoSelecao() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.float_categoria, null);
        Button btnReceitas = dialogView.findViewById(R.id.btnReceitas);
        Button btnDespesas = dialogView.findViewById(R.id.btnDespesas);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        btnReceitas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReceitaActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        btnDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DespesaActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onItemLongClick(Categoria categoria) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        Button btnEditar = dialogView.findViewById(R.id.btnEditar);
        Button btnExcluir = dialogView.findViewById(R.id.btnExcluir);

        btnEditar.setOnClickListener(v -> {
            Intent intent;
            if ("receita".equals(categoria.getTipo())) {
                intent = new Intent(MainActivity.this, ReceitaActivity.class);
            } else {
                intent = new Intent(MainActivity.this, DespesaActivity.class);
            }
            intent.putExtra("categoria_id", categoria.getId());
            startActivity(intent);
            dialog.dismiss();
        });

        btnExcluir.setOnClickListener(v -> {
            executor.execute(() -> {
                db.categoriaDao().delete(categoria);
                carregarCategorias();
            });
            dialog.dismiss();
        });

        dialog.show();
    }

    // Bruno: Implementação dos cliques nos grupos
    @Override
    public void onGrupoClick(Grupo grupo) {
        // TODO: No próximo card - expandir/recolher grupo
        Toast.makeText(this, "Clicou no grupo: " + grupo.getNome(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGrupoLongClick(Grupo grupo) {
        // Dialog para editar/excluir grupo
        mostrarDialogoOpcoesGrupo(grupo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        AppDatabase.destroyInstance();
    }
}
