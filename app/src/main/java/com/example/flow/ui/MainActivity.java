package com.example.flow.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flow.R;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CategoriaAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView monthText;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Encontrar Componentes ---
        FloatingActionButton btnAddCategoria = findViewById(R.id.btnAddCategoria);
        RecyclerView rvCategorias = findViewById(R.id.rvCategorias);
        monthText = findViewById(R.id.month_text);
        ImageButton prevMonthButton = findViewById(R.id.prev_month_button);
        ImageButton nextMonthButton = findViewById(R.id.next_month_button);

        // --- Inicializar Base de Dados ---
        db = AppDatabase.getInstance(getApplicationContext());

        // --- Configurar a Lista ---
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoriaAdapter(new ArrayList<>());
        rvCategorias.setAdapter(adapter);

        // --- Carregar Dados Iniciais ---
        carregarCategorias();
        updateMonthText();

        // --- Configurar Clique do Botão ---
        btnAddCategoria.setOnClickListener(v -> mostrarDialogoSelecao());

        monthText.setOnClickListener(v -> showDatePickerDialog(null));

        prevMonthButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthText();
        });

        nextMonthButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthText();
        });
    }

    private void showDatePickerDialog(final TextView dateTextView) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (dateTextView != null) {
                updateDateText(dateTextView);
            } else {
                updateMonthText();
            }
        };

        new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateMonthText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        monthText.setText(sdf.format(calendar.getTime()));
    }

    private void updateDateText(TextView textView) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textView.setText(sdf.format(calendar.getTime()));
    }

    private void carregarCategorias() {
        executor.execute(() -> {
            List<Categoria> categorias = db.categoriaDao().getAllCategorias();
            runOnUiThread(() -> adapter.setLista(categorias));
        });
    }

    private void mostrarDialogoSelecao() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.float_categoria, null);
        Button btnReceitas = dialogView.findViewById(R.id.btnReceitas);
        Button btnDespesas = dialogView.findViewById(R.id.btnDespesas);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnReceitas.setOnClickListener(v -> {
            mostrarDialogReceita();
            dialog.dismiss();
        });

        btnDespesas.setOnClickListener(v -> {
            mostrarDialogDespesa();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogReceita() {
        mostrarDialogCrud("receita");
    }

    private void mostrarDialogDespesa() {
        mostrarDialogCrud("despesa");
    }

    private void mostrarDialogCrud(String tipo) {
        int layoutResId = "receita".equals(tipo) ? R.layout.float_receita : R.layout.float_despesa;
        View dialogView = LayoutInflater.from(this).inflate(layoutResId, null);

        ImageView btnVoltar = dialogView.findViewById(R.id.btnVoltar);
        TextView txtData = dialogView.findViewById(R.id.txtData);
        EditText edtCategoria = dialogView.findViewById(R.id.edtCategoria);
        EditText edtValor = dialogView.findViewById(R.id.edtValor);
        TextView txtMetodo = dialogView.findViewById(R.id.txtMetodo);
        EditText edtNota = dialogView.findViewById(R.id.edtNota);
        Button btnSalvar = dialogView.findViewById(R.id.btnSalvar);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        txtData.setOnClickListener(v -> showDatePickerDialog(txtData));
        updateDateText(txtData); // Inicia com a data atual

        btnVoltar.setOnClickListener(v -> dialog.dismiss());

        btnSalvar.setOnClickListener(v -> {
            String nome = edtCategoria.getText().toString().trim();
            String valorStr = edtValor.getText().toString().trim();

            if (nome.isEmpty()) {
                Toast.makeText(this, "Adicione uma categoria!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (valorStr.isEmpty()) {
                Toast.makeText(this, "Adicione um valor!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double valor = Double.parseDouble(valorStr);
                String data = txtData.getText().toString();
                String metodo = txtMetodo.getText().toString();
                String nota = edtNota.getText().toString().trim();

                Categoria novaCategoria = new Categoria(nome, tipo, valor, data, metodo, nota);

                executor.execute(() -> {
                    db.categoriaDao().insert(novaCategoria);
                    runOnUiThread(() -> {
                        carregarCategorias();
                        String msg = "receita".equals(tipo) ? "Receita adicionada!" : "Despesa adicionada!";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show();
            }
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
