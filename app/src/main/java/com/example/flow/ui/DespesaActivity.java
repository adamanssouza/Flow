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

public class DespesaActivity extends AppCompatActivity {

    private EditText edtValor, edtNota;
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
        setContentView(R.layout.float_despesa);

        db = AppDatabase.getInstance(getApplicationContext());

        edtValor = findViewById(R.id.edtValor);
        edtNota = findViewById(R.id.edtNota);
        txtData = findViewById(R.id.txtData);
        spinnerMetodoPagamento = findViewById(R.id.spinnerMetodoPagamento);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);
        campoData = findViewById(R.id.campoData);

        setupDatePicker();
        setupMetodoPagamentoSpinner();

        int categoriaId = getIntent().getIntExtra("categoria_id", -1);

        if (categoriaId != -1) {
            carregarCategoria(categoriaId);
        }

        btnSalvar.setOnClickListener(v -> salvarDespesa());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarCategoria(int id) {
        executor.execute(() -> {
            categoriaExistente = db.categoriaDao().getCategoriaById(id);
            runOnUiThread(() -> {
                if (categoriaExistente != null) {
                    edtNota.setText(categoriaExistente.getNota());
                    edtValor.setText(String.valueOf(categoriaExistente.getValor()));
                    txtData.setText(categoriaExistente.getData());

                    if (spinnerMetodoPagamento.getAdapter() instanceof ArrayAdapter) {
                        @SuppressWarnings("unchecked")
                        ArrayAdapter<CharSequence> metodoAdapter = (ArrayAdapter<CharSequence>) spinnerMetodoPagamento.getAdapter();
                        int metodoPosition = metodoAdapter.getPosition(categoriaExistente.getMetodoPagamento());
                        spinnerMetodoPagamento.setSelection(metodoPosition);
                    }
                }
            });
        });
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        };
        campoData.setOnClickListener(v -> new DatePickerDialog(this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show());
        updateLabel();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        txtData.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupMetodoPagamentoSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetodoPagamento.setAdapter(adapter);
    }

    private void salvarDespesa() {
        String nota = edtNota.getText().toString().trim();
        String valorStr = edtValor.getText().toString().trim();
        String data = txtData.getText().toString().trim();
        String metodoPagamento = spinnerMetodoPagamento.getSelectedItem().toString();

        if (valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha o valor!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nomeDaTransacao = nota.isEmpty() ? "Despesa" : nota;
        double valor = Double.parseDouble(valorStr);

        executor.execute(() -> {
            if (categoriaExistente != null) {
                categoriaExistente.setNome(nomeDaTransacao);
                categoriaExistente.setNota(nota);
                categoriaExistente.setValor(valor);
                categoriaExistente.setData(data);
                categoriaExistente.setMetodoPagamento(metodoPagamento);
                // O campo 'grupo' não é mais definido aqui
                db.categoriaDao().update(categoriaExistente);
            } else {
                // O campo 'grupo' é passado como null ou uma string vazia
                Categoria nova = new Categoria(nomeDaTransacao, "despesa", valor, data, metodoPagamento, nota, "");
                db.categoriaDao().insert(nova);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show();
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
