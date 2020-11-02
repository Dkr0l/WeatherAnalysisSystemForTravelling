package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import krolkozak.project.app.R;
import krolkozak.project.app.bazadanych.Uzytkownik;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Logowanie extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logowanie);

        // Kliknięcie w przycisk "ZALOGUJ SIĘ"
        ((Button) findViewById(R.id.zalogujPrzycisk)).setOnClickListener(v -> {
            // Przechodzi do ekranu menu
            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        });

        // TESTOWANIE BAZY DANYCH -----------------------------------------------
        // Odnośnik do bazy danych
        FirebaseFirestore bazaDanychRef = FirebaseFirestore.getInstance();

        // Wprowadzenie nowego użytkownika do tabeli uzytkownicy
//        Uzytkownik uzytkownik1 = new Uzytkownik("id1", "login1", "haslo1", "email1", String.valueOf(OffsetDateTime.now()));
//        Log.i(nazwaApki, "Wprowadzanie nowego użytkownika: " + uzytkownik1.pobierzPelneDane());
//        bazaDanychRef.collection("uzytkownicy").add(uzytkownik1);

        // Pobieranie wszystkich rekordów z tabeli uzytkownicy
        Log.i(nazwaApki, "Pobieranie użytkowników z bazy");
        bazaDanychRef.collection("uzytkownicy").get().addOnSuccessListener(documentSnapshots -> {
            if (documentSnapshots.isEmpty()) {
                Log.i(nazwaApki, "Nie znaleziono użytkowników");
                return;
            } else {
                Log.i(nazwaApki, "Pobrano użytkowników z bazy (" + documentSnapshots.size() + "):");

                ArrayList<Uzytkownik> uzytkownicy = new ArrayList<Uzytkownik>();
                ArrayList<Uzytkownik> types = (ArrayList<Uzytkownik>) documentSnapshots.toObjects(Uzytkownik.class);
                uzytkownicy.addAll(types);

                for (Uzytkownik uzytkownik : uzytkownicy) {
                    Log.i(nazwaApki, "Użytkownik: " + uzytkownik.pobierzPelneDane());
                }

            }
        });
    }
}