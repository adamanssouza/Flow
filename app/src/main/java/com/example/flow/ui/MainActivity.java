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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CategoriaAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView rvCategorias;
    private LinearLayout emptyStateLayout;
    private ImageView profileImageView;
    private TextView profileNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Encontrar Componentes ---
        FloatingActionButton btnAddCategoria = findViewById(R.id.btnAddCategoria);
        rvCategorias = findViewById(R.id.rvCategorias);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        LinearLayout profileSection = findViewById(R.id.profile_section);
        profileImageView = findViewById(R.id.profile_image);
        profileNameView = findViewById(R.id.profile_name);

        // --- Inicializar Base de Dados ---
        db = AppDatabase.getInstance(getApplicationContext());

        // --- Configurar a Lista ---
        setupRecyclerView();

        // --- Carregar Dados Iniciais ---
        carregarCategorias();
        loadProfileData();

        // --- Configurar Cliques ---
        btnAddCategoria.setOnClickListener(v -> mostrarDialogoSelecao());
        profileSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoriaAdapter(new ArrayList<>());
        rvCategorias.setAdapter(adapter);
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
            List<Categoria> categorias = db.categoriaDao().getAllCategorias();
            runOnUiThread(() -> {
                adapter.setLista(categorias);
                updateEmptyState(categorias.isEmpty());
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        rvCategorias.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void mostrarDialogoSelecao() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.float_categoria, null);
        Button btnReceitas = dialogView.findViewById(R.id.btnReceitas);
        Button btnDespesas = dialogView.findViewById(R.id.btnDespesas);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        btnReceitas.setOnClickListener(v -> {
            mostrarDialogAdicionar("receita");
            dialog.dismiss();
        });

        btnDespesas.setOnClickListener(v -> {
            mostrarDialogAdicionar("despesa");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogAdicionar(String tipo) {
        int layoutResId = "receita".equals(tipo) ? R.layout.float_receita : R.layout.float_despesa;
        View dialogView = LayoutInflater.from(this).inflate(layoutResId, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etNome;
        Button btnSalvar;

        if ("receita".equals(tipo)) {
            etNome = dialogView.findViewById(R.id.etNomeReceita);
            btnSalvar = dialogView.findViewById(R.id.btnSalvarReceita);
        } else {
            etNome = dialogView.findViewById(R.id.etNomeDespesa);
            btnSalvar = dialogView.findViewById(R.id.btnSalvarDespesa);
        }

        btnSalvar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(this, "Digite o nome!", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                Categoria nova = new Categoria(nome, tipo);
                db.categoriaDao().insert(nova);
                runOnUiThread(() -> {
                    carregarCategorias();
                    dialog.dismiss();
                });
            });
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        AppDatabase.destroyInstance();
    }
}
