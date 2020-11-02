package krolkozak.project.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class GlownaAktywnosc extends Activity {

    // -------------- ZMIENNE KLASY --------------
    // kod żądania uprawnień od użytkownika
    private final int kodZadaniaUprawnien = 1;
    // pomocnicza nazwa aplikacji do debuggowania
    protected static final String nazwaApki = "TRAVEL_APP";
    // przyciski
    protected static Button znajdzTrasePrzycisk;
    // komunikacja między aktywnościami
    private static final int DANE_TRASY = 102;
    // obiekt klasy Trasa
    protected static Trasa trasa = new Trasa();
    //kontekst aplikacji
    protected static Context kontekst;

    // -------------- GŁÓWNA METODA APLIKACJI WYWOŁYWANA PO URUCHOMIENIU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // wywołanie konstruktora klasy nadrzędnej z parametrem zapisanego stanu aplikacji i
        // ustawienie układu aplikacji (layout'u)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.glowna_aktywnosc);

        // -------------- POLITYKA I UPRAWNIENIA --------------
        // zezwolenie na wszystkie potrzebne uprawnienia
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // zapisanie kontekstu aplikacji
        kontekst = getApplicationContext();

        // zapytanie użytkownika o uprawnienia potrzebne do działania aplikacji (zapisywanie i lokalizacja)
        Configuration.getInstance().load(kontekst, PreferenceManager.getDefaultSharedPreferences(kontekst));
        zapytajOUprawnieniaJesliKonieczne(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        zapytajOUprawnieniaJesliKonieczne(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});

        // -------------- INICJALIZACJA ZMIENNYCH --------------
        // przypisanie referencji
        // mapy
        trasa.mapa = findViewById(R.id.mapaWidok);

        // przycisków
        znajdzTrasePrzycisk = findViewById(R.id.znajdzTrasePrzycisk);

        // przypisanie okna tworzenia trasy do przycisku
        ((Button) findViewById(R.id.stworzTrasePrzycisk)).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, TworzenieTrasy.class);
            startActivityForResult(popupIntent, DANE_TRASY);
        });

        // wywołanie metody, która ustawia początkowe parametry mapy oraz wyświetla na ekranie
        trasa.zainicjujMape();

        // wyłączenie przycisków
        Log.i(nazwaApki, "WYŁĄCZONO PRZYCISK");

        znajdzTrasePrzycisk.setText("STWÓRZ TRASĘ");
        znajdzTrasePrzycisk.setEnabled(false);

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

    // metoda prosząca użytkownika o zezwolenia na uprawnienia
    public void zapytajOUprawnieniaJesliKonieczne(String[] uprawnienia) {
        ArrayList<String> uprawnieniaDoUzyskania = new ArrayList<>();

        // jeśli nie nadano uprawnień - dodaj uprawnienie do listy uprawnień do uzyskania
        for (String uprawnienie : uprawnienia) {
            if (ContextCompat.checkSelfPermission(kontekst, uprawnienie) != PackageManager.PERMISSION_GRANTED) {
                uprawnieniaDoUzyskania.add(uprawnienie);
            }
        }

        // jeśli lista nie jest pusta - zapytaj o uprawnienia
        if (uprawnieniaDoUzyskania.size() > 0) {
            ActivityCompat.requestPermissions(this, uprawnieniaDoUzyskania.toArray(new String[0]), kodZadaniaUprawnien);
        }
    }

    // -------------- ZAPYTANIE O POZWOLENIA OD UŻYTKOWNIKA --------------
    @Override
    // metoda wywoływana jeśli użytkownik podjął decyzję czy nadać uprawnienia czy nie
    public void onRequestPermissionsResult(int kodUzyskania, String[] uprawnienia,
                                           int[] rezultatyNadania) {
        ArrayList<String> uprawnieniaDoUzyskania = new ArrayList<>();

        // dodanie wszystkich uprawnień do listy
        for (int i = 0; i < rezultatyNadania.length; i++) {
            uprawnieniaDoUzyskania.add(uprawnienia[i]);
        }

        // jeśli lista nie jest pusta - zapytaj o uprawnienia
        if (uprawnieniaDoUzyskania.size() > 0) {
            ActivityCompat.requestPermissions(this, uprawnieniaDoUzyskania.toArray(new String[0]), kodZadaniaUprawnien);
        }
    }

    @Override
    // metoda wywoływana, gdy aplikacja wróci do działania
    public void onResume() {
        // wywołanie metod powrotu do działania konstruktora klasy nadrzędnej oraz mapy
        super.onResume();
        trasa.zatrzymaj();
    }

    @Override
    // metoda wywoływana, gdy aplikacja się zatrzyma
    public void onPause() {
        // wywołanie metod zatrzymania konstruktora klasy nadrzędnej oraz mapy
        super.onPause();
        trasa.wznow();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DANE_TRASY:
                if (resultCode == Activity.RESULT_OK) {
                    trasa.srodek_transportu = data.getStringExtra("stworzono trase");
                    Log.i(nazwaApki, "stworzono trase " + trasa.srodek_transportu);
                }
        }
    }

}
