package com.example.flow.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DespesaDao {
    @Insert
    void insert(Despesa despesa);

    @Query("SELECT * FROM despesas WHERE categoriaId = :categoriaId")
    List<Despesa> getDespesasPorCategoria(int categoriaId);
}
