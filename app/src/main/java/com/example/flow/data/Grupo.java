package com.example.flow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "grupos")
public class Grupo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nome;
    private String cor; // Cor em hex: "#FF5722"
    private String icone; // Nome do ícone: "shopping_cart", "food", etc.
    private String dataCriacao;

    // Construtor
    public Grupo(String nome, String cor, String icone) {
        this.nome = nome;
        this.cor = cor;
        this.icone = icone;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.dataCriacao = sdf.format(new java.util.Date());
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }
}