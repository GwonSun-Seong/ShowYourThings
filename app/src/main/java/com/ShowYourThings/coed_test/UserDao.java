package com.ShowYourThings.coed_test;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void setInsertUser(User user);

    @Update
    void setUpdateUser(User user);

    @Delete
    void setDeleteUser(User user);

    @Query("SELECT * FROM User")
    List<User> getUserAll();

    @Query("SELECT ttsspeed FROM User LIMIT 1")
    float getTtsSpeed();

    @Query("SELECT server FROM User LIMIT 1")
    String getServer();

}
