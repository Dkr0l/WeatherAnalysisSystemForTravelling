package krolkozak.project.app.tworzenietrasy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.osmdroid.config.Configuration;

import java.util.ArrayList;

import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.pomocnicze.WyswietlanieMapy;

/*

TODO:
 - dodać wyświetlanie informacji o aktualnej trasie
*/

@RequiresApi(api = Build.VERSION_CODES.O)
public class Mapa extends Activity {

    // -------------- ZMIENNE KLASY --------------
    // kod żądania uprawnień od użytkownika
    private final int kodZadaniaUprawnien = 1;
    // pomocnicza nazwa aplikacji do debuggowania
    public static final String nazwaApki = "TRAVEL_APP";
    // przyciski
    public static Button znajdzTrasePrzycisk;
    // komunikacja między aktywnościami
    private static final int DANE_TRASY = 102;
    // obiekt klasy Trasa
    public static Trasa trasa = new Trasa();
    //kontekst aplikacji
    protected static Context kontekst;
    //do paska postepu
    private static ProgressBar pasekPostepu;
    public static void ustawPostep(int postep0do100) {
        pasekPostepu.setProgress(postep0do100);
    }
    private static TextView opisPostepu;
    public static void ustawPodpisPostepu(CharSequence opis) {
        opisPostepu.setText(opis);
    }
    public static Handler handler=new Handler();
    private boolean wygenerowane=false;
    public void opoznioneOdswierzanie(final int czas_ms)
    {
        handler.postDelayed(() -> {
            if (!wygenerowane) {
                handler.post(() -> ustawPodpisPostepu(Trasa.opis));
                handler.post(() -> ustawPostep(Trasa.postep));
                opoznioneOdswierzanie(czas_ms);
            }
        }, czas_ms);
    }

    // -------------- GŁÓWNA METODA APLIKACJI WYWOŁYWANA PO URUCHOMIENIU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // wywołanie konstruktora klasy nadrzędnej z parametrem zapisanego stanu aplikacji i
        // ustawienie układu aplikacji (layout'u)
        super.onCreate(savedInstanceState);
        if(Ustawienia.trybCiemnyAktywny()) {
            setContentView(R.layout.trybciemnymapa);
        }else{
            setContentView(R.layout.mapa);
        }

        Intent ekranMapa = getIntent();
        String punktyTrasy = ekranMapa.getStringExtra("punkty_trasy");
        if(punktyTrasy != null) {
            Log.i(nazwaApki, "Punkty trasy do odtworzenia z historii (ekrany mapy): " + punktyTrasy);

            trasa.przystanki.clear();

            Intent ekranTworzenieTrasy = new Intent(getApplicationContext(), TworzenieTrasy.class);
            ekranTworzenieTrasy.putExtra("punkty_trasy", punktyTrasy);
            startActivityForResult(ekranTworzenieTrasy, DANE_TRASY);
        }

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

        //elementy paska ladowania
        pasekPostepu = findViewById(R.id.pasekPostepu);
        opisPostepu = findViewById(R.id.postepInfo);

        // przypisanie okna tworzenia trasy do przycisku
        ((Button) findViewById(R.id.stworzTrasePrzycisk)).setOnClickListener(v -> {
            trasa.przystanki.clear();
            Intent popupIntent = new Intent(this, TworzenieTrasy.class);
            startActivityForResult(popupIntent, DANE_TRASY);
        });

        // wywołanie metody, która ustawia początkowe parametry mapy oraz wyświetla na ekranie
        trasa.zainicjujMape();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && Ustawienia.trybCiemnyAktywny()){
            WyswietlanieMapy.wlaczCiemnyTrybMapy(trasa.mapa);
        }

        // dodanie nasłuchiwacza kliknięcia w przycisk "ZNAJDŹ TRASĘ"
        znajdzTrasePrzycisk.setOnClickListener(v -> {
            Trasa.postep=0;
            findViewById(R.id.mapaWidok).setVisibility(View.GONE);
            findViewById(R.id.popupPostep).setVisibility(View.VISIBLE);
            findViewById(R.id.widokmapy).invalidate();
            //wymuszenie odświerzenia widoku przed wykonaniem dalszego kodu
            findViewById(R.id.widokmapy).post(() -> {
                new Thread(new Zadanie()).start();
                opoznioneOdswierzanie(200);
            });
        });

        // wyłączenie przycisków
        Log.i(nazwaApki, "WYŁĄCZONO PRZYCISK");

        znajdzTrasePrzycisk.setEnabled(false);
    }

    class Zadanie implements Runnable{
        @Override
        public void run(){
            handler.post(() -> Mapa.ustawPodpisPostepu("Rozpoczynanie obliczeń"));
            // wywołanie metody, która obliczy i wyświetli trasę wraz z punktami pogodowymi na mapie
            try {
                trasa.odswiezMape(getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            wygenerowane=true;
            handler.post(() -> findViewById(R.id.popupPostep).setVisibility(View.GONE));
            handler.post(() -> findViewById(R.id.mapaWidok).setVisibility(View.VISIBLE));
            handler.post(() -> findViewById(R.id.widokmapy).invalidate());
        }
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
