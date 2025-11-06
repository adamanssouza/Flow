package com.example.flow.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoriaDao {
    @Insert
    void insert(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY id DESC")
    List<Categoria> getAllCategorias();
}
