package com.example.flow.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GrupoDao {
    @Insert
    void insert(Grupo grupo);

    @Update
    void update(Grupo grupo);

    @Delete
    void delete(Grupo grupo);

    @Query("SELECT * FROM grupos ORDER BY dataCriacao DESC")
    List<Grupo> getAllGrupos();

    @Query("SELECT * FROM grupos WHERE id = :id")
    Grupo getGrupoById(int id);

    @Query("SELECT * FROM grupos WHERE nome LIKE '%' || :nome || '%' ORDER BY nome")
    List<Grupo> searchGrupos(String nome);

    // Bruno: Buscar grupo por nome exato
    @Query("SELECT * FROM grupos WHERE nome = :nome LIMIT 1")
    Grupo getGrupoByNome(String nome);
}