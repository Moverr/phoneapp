package com.example.phoneproject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert
    void insert(Contact contact);

    @Query("SELECT * FROM contacts")
    List<Contact> getAllContacts();

}

