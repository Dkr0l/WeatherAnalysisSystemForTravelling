package krolkozak.project.app.bazadanych.uzytkownicy;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// DAO (Data Access Objects) - Interfejs zawięrający metody dostępu do danych tabeli uzytkownicy
@Dao
public interface UzytkownikDAO {
    // Wprowadzenie nowego użytkownika do tabeli - rejestracja
    @Insert
    public void wprowadzUzytkownika(Uzytkownik uzytkownik);

    // Znalezienie użytkownika o podanym loginie i haśle - logowanie
    @Query("SELECT * FROM uzytkownicy WHERE login=:login AND haslo=:haslo")
    public Uzytkownik znajdzUzytkownika(String login, String haslo);

    // Metoda zwracająca ilość użytkowników w tabeli
    @Query("SELECT COUNT(*) FROM uzytkownicy")
    public int pobierzIloscUzytkownikow();
}