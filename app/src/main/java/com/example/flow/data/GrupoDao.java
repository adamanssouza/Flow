package com.example.flow.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GrupoDao {
    @Query("SELECT * FROM grupos")
    List<Grupo> getAllGrupos();

    @Insert
    void insert(Grupo grupo);

    @Update
    void update(Grupo grupo);

    @Delete
    void delete(Grupo grupo);

    @Query("SELECT * FROM grupos WHERE nome = :name")
    Grupo getGrupoByName(String name);
}
