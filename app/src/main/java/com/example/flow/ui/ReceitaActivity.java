package com.example.flow.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flow.R;
import com.example.flow.data.AppDatabase;
import com.example.flow.data.Categoria;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceitaActivity extends AppCompatActivity {

    private EditText edtCategoria, edtValor, edtNota;
    private TextView txtData;
    private Spinner spinnerMetodoPagamento;
    private Button btnSalvar;
    private ImageView btnVoltar;
    private LinearLayout campoData;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Calendar myCalendar = Calendar.getInstance();
    private Categoria categoriaExistente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.float_receita);

        db = AppDatabase.getInstance(getApplicationContext());

        edtCategoria = findViewById(R.id.edtCategoria);
        edtValor = findViewById(R.id.edtValor);
        edtNota = findViewById(R.id.edtNota);
        txtData = findViewById(R.id.txtData);
        spinnerMetodoPagamento = findViewById(R.id.spinnerMetodoPagamento);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);
        campoData = findViewById(R.id.campoData);

        setupDatePicker();
        setupSpinner();

        int categoriaId = getIntent().getIntExtra("categoria_id", -1);
        if (categoriaId != -1) {
            carregarCategoria(categoriaId);
        }

        btnSalvar.setOnClickListener(v -> salvarReceita());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarCategoria(int id) {
        executor.execute(() -> {
            categoriaExistente = db.categoriaDao().getCategoriaById(id);
            runOnUiThread(() -> {
                if (categoriaExistente != null) {
                    edtCategoria.setText(categoriaExistente.getNome());
                    edtValor.setText(String.valueOf(categoriaExistente.getValor()));
                    txtData.setText(categoriaExistente.getData());
                    edtNota.setText(categoriaExistente.getNota());
                    // Selecionar o método de pagamento no spinner
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerMetodoPagamento.getAdapter();
                    int position = adapter.getPosition(categoriaExistente.getMetodoPagamento());
                    spinnerMetodoPagamento.setSelection(position);
                }
            });
        });
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        campoData.setOnClickListener(v -> new DatePickerDialog(ReceitaActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        txtData.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetodoPagamento.setAdapter(adapter);
    }

    private void salvarReceita() {
        String nome = edtCategoria.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();
        String data = txtData.getText().toString().trim();
        String metodoPagamento = spinnerMetodoPagamento.getSelectedItem().toString();
        String nota = edtNota.getText().toString().trim();

        if (nome.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha o nome e o valor!", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor = Double.parseDouble(valorStr);

        executor.execute(() -> {
            if (categoriaExistente != null) {
                // Atualizar categoria existente
                categoriaExistente.setNome(nome);
                categoriaExistente.setValor(valor);
                categoriaExistente.setData(data);
                categoriaExistente.setMetodoPagamento(metodoPagamento);
                categoriaExistente.setNota(nota);
                db.categoriaDao().update(categoriaExistente);
            } else {
                // Criar nova categoria
                Categoria nova = new Categoria(nome, "receita", valor, data, metodoPagamento, nota);
                db.categoriaDao().insert(nova);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Receita salva!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
