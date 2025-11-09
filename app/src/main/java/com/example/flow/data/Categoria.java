package com.example.flow.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
@Entity(tableName = "categorias")

public class Categoria {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nome;
    private String tipo; // "receita" ou "despesa"
    private double valor;
    private String data;
    private String metodoPagamento;
    private String nota;
    private Integer grupoId; // NOVO: Relacionamento com Grupo (pode ser nulo para compatibilidade)

    // Construtor
    public Categoria(String nome, String tipo, double valor, String data, String metodoPagamento, String nota, Integer grupoId) {
        this.nome = nome;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.metodoPagamento = metodoPagamento;
        this.nota = nota;
        this.grupoId = grupoId;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public Integer getGrupoId() { return grupoId; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
}
