package krolkozak.project.app.bazadanych.uzytkownicy;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// MODEL TABELI uzytkownicy
@Entity(tableName = "uzytkownicy", indices = {@Index(value = {"login", "email"}, unique = true)})
public class Uzytkownik {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "login")                 // nazwa użytkownika
    public String login;

    @ColumnInfo(name = "haslo")                 // hasło
    public String haslo;

    @ColumnInfo(name = "email")                 // adres e-mail
    public String email;

    @ColumnInfo(name = "data_utworzenia")      // data utworzenia konta
    public String data_utworzenia;
}

///////////////////// TESTOWANIE BAZY - TABELA uzytkownicy
// !!!! fallbackToDestructiveMigration() - tworzy od nowa bazę przy zmianie wersji
    /*
    final BazaDanych bazaDanych = Room.databaseBuilder(getApplicationContext(), BazaDanych.class, "travel_app").allowMainThreadQueries().fallbackToDestructiveMigration().build();

    wprowadzUzytkownikaDoBazy(bazaDanych, "login1", "haslo1", "email1", String.valueOf(OffsetDateTime.now()));

        Log.i(nazwaApki, "Ilość użytkowników w bazie: " + String.valueOf(bazaDanych.uzytkownikDAO().pobierzIloscUzytkownikow()));

        znajdzIWyswietlDaneUzyktownika(bazaDanych,"login1", "haslo1");
        znajdzIWyswietlDaneUzyktownika(bazaDanych,"login2", "haslo2");
     */

    /*

    public void znajdzIWyswietlDaneUzyktownika(BazaDanych bazaDanych, String login, String haslo) {
        Uzytkownik uzytkownik = bazaDanych.uzytkownikDAO().znajdzUzytkownika(login, haslo);
        Log.i(nazwaApki, "Wyszukiwanie użytkownika (" + login + ", " + haslo + ")");

        if(uzytkownik != null) {
            Log.i(nazwaApki, "Znaleziono użytkownika: " + uzytkownik.login + ", " + uzytkownik.haslo + ", " + uzytkownik.email+ ", " + uzytkownik.data_utworzenia);
        } else {
            Log.i(nazwaApki, "Nie znaleziono użytkownika.");
        }
    }

    public void wprowadzUzytkownikaDoBazy(BazaDanych bazaDanych, String login, String haslo, String email, String data_utworzenia) {
        Uzytkownik uzytkownik = new Uzytkownik();
        uzytkownik.login = login;
        uzytkownik.haslo = haslo;
        uzytkownik.email = email;
        uzytkownik.data_utworzenia = data_utworzenia;

        Log.i(nazwaApki, "Wprowadzanie nowego użytkownika do tabeli (" + login + ", " + haslo + ", " + email+ ", " + data_utworzenia + ")");

        try {
            bazaDanych.uzytkownikDAO().wprowadzUzytkownika(uzytkownik);

            Log.i(nazwaApki, "Pomyślnie wprowadzono nowego użytkownika do tabeli.");
        } catch (Exception e) {
            Log.i(nazwaApki, "Nie udało się wprowadzić nowego użytkownika do tabeli.");
        }
    }

     */
