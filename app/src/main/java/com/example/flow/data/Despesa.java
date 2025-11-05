package com.example.flow.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "despesas",
        foreignKeys = @ForeignKey(entity = Categoria.class,
                                  parentColumns = "id",
                                  childColumns = "categoriaId",
                                  onDelete = ForeignKey.CASCADE))
public class Despesa {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String descricao;
    private double valor;
    private int categoriaId;

    public Despesa(String descricao, double valor, int categoriaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.categoriaId = categoriaId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
}
