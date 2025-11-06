package com.example.flow.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoriaDao {
    @Insert
    void insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY id DESC")
    List<Categoria> getAllCategorias();

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria getCategoriaById(int id);
}
