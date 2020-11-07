package krolkozak.project.app.tworzenietrasy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;

import krolkozak.project.app.R;

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

    // -------------- GŁÓWNA METODA APLIKACJI WYWOŁYWANA PO URUCHOMIENIU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // wywołanie konstruktora klasy nadrzędnej z parametrem zapisanego stanu aplikacji i
        // ustawienie układu aplikacji (layout'u)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            darkMode();
        }
        // wyłączenie przycisków
        Log.i(nazwaApki, "WYŁĄCZONO PRZYCISK");

        znajdzTrasePrzycisk.setText("STWÓRZ TRASĘ");
        znajdzTrasePrzycisk.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void darkMode() {
        //DARK MODE
        TilesOverlay plytkiMapy=trasa.mapa.getOverlayManager().getTilesOverlay();
        /*  4x5 matrix for transforming the color and alpha components of a Bitmap. The matrix can be passed as single array, and is treated as follows:
          [ a, b, c, d, e,
            f, g, h, i, j,
            k, l, m, n, o,
            p, q, r, s, t ]
        When applied to a color [R, G, B, A], the resulting color is computed as:
           R’ = a*R + b*G + c*B + d*A + e;
           G’ = f*R + g*G + h*B + i*A + j;
           B’ = k*R + l*G + m*B + n*A + o;
           A’ = p*R + q*G + r*B + s*A + t;      */
        float[] matrycaKolorow={
                0, -1, -1, 0, 450,//Czerwony
                -1, 0, -1, 0, 450,//Zielony
                -1, -1, 0, 0, 450,//Niebieski
                0, 0, 0, 1, 0 //alpha (nie tykać!!)
        };
        plytkiMapy.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(matrycaKolorow)));
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
