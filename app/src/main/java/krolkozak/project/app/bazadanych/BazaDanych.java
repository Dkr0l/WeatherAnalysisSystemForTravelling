package krolkozak.project.app.bazadanych;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import krolkozak.project.app.bazadanych.uzytkownicy.Uzytkownik;
import krolkozak.project.app.bazadanych.uzytkownicy.UzytkownikDAO;

// Główny punkt dostępu do podstawowego połączenia z danymi aplikacji
@Database(entities = {Uzytkownik.class}, version = 2)
public abstract class BazaDanych extends RoomDatabase {
    public abstract UzytkownikDAO uzytkownikDAO();
}