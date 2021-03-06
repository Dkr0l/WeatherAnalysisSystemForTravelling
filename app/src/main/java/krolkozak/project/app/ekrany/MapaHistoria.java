package krolkozak.project.app.ekrany;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

import krolkozak.project.app.R;
import krolkozak.project.app.bazadanych.Historia;
import krolkozak.project.app.pomocnicze.WyswietlanieMapy;
import krolkozak.project.app.tworzenietrasy.Mapa;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MapaHistoria extends Activity {
    private Historia dokumentHistorii;
    private MapView mapaHistoriaWidok;
    private Context kontekst;
    private final int kodZadaniaUprawnien = 1;
    private final int ROZMIAR_RAMKI = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa_historia);

        kontekst = getApplicationContext();
        mapaHistoriaWidok = (MapView) findViewById(R.id.mapaHistoriaWidok);

        Intent ekranMapaHistoria = getIntent();
        dokumentHistorii = (Historia) ekranMapaHistoria.getExtras().getSerializable("dokument_historii");
        Log.i(nazwaApki, "Pobrany dokument historii z aktywności historii: " + dokumentHistorii.pobierzObiekt());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Configuration.getInstance().load(kontekst, PreferenceManager.getDefaultSharedPreferences(kontekst));
        zapytajOUprawnieniaJesliKonieczne(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});

        WyswietlanieMapy.wyswietlWidokMapy(mapaHistoriaWidok);

        try {
            wyswietlPunktyPogodowe();
        } catch (JSONException e) {
            Log.i(nazwaApki, "Nie udało się wyświetlić historii punktów pogodowych");
        }

        try {
            ArrayList<GeoPoint> punkty = WyswietlanieMapy.wyswietlTraseNaMapie(mapaHistoriaWidok, dokumentHistorii.getPunkty_trasy(), dokumentHistorii.getTyp_trasy());

            mapaHistoriaWidok.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
                BoundingBox obszarDoWyswietlenia = BoundingBox.fromGeoPoints(punkty);
                mapaHistoriaWidok.zoomToBoundingBox(obszarDoWyswietlenia, true, ROZMIAR_RAMKI);
                mapaHistoriaWidok.invalidate();
            });
        } catch (JSONException e) {
            Log.i(nazwaApki, "Nie udało się wyświetlić trasy na mapie: " + e.getMessage());
        }

        // PRZYCISK "ODTWÓRZ TRASĘ"
        ((Button) findViewById(R.id.mapaHistoriaOdtworz)).setOnClickListener(v -> {
            Intent ekranMapa = new Intent(getApplicationContext(), Mapa.class);
            ekranMapa.putExtra("punkty_trasy", dokumentHistorii.getPunkty_trasy());
            startActivity(ekranMapa);
        });
    }

    private void wyswietlPunktyPogodowe() throws JSONException {
        JSONArray miejscaPogodowe = new JSONArray(dokumentHistorii.getMiejsca_pogodowe());

        for (int i = 0; i < miejscaPogodowe.length(); i++) {
            JSONObject obiektPogodowy = (JSONObject) miejscaPogodowe.getJSONObject(i);

            JSONObject koordynaty = (JSONObject) obiektPogodowy.get("koordynaty");
            double szerGeog = koordynaty.getDouble("szer_geog");
            double dlugGeog = koordynaty.getDouble("dlug_geog");
            GeoPoint punktGeog = new GeoPoint(szerGeog, dlugGeog);

            int indeksObrazka = obiektPogodowy.getInt("indeks_obrazka");

            String pogodaOdpowiedzApi = obiektPogodowy.getString("pogoda_odpowiedz_api");
            String typPrognozy = obiektPogodowy.getString("typ_prognozy");

            String lokalizacja = obiektPogodowy.getString("lokalizacja");
            String czas = obiektPogodowy.getString("czas");
            String opisZnacznika = lokalizacja + "<br>" + czas;

            WyswietlanieMapy.wyswietlZnacznikNaMapie(kontekst, mapaHistoriaWidok, punktGeog, pogodaOdpowiedzApi, typPrognozy, opisZnacznika, indeksObrazka);
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
        mapaHistoriaWidok.onResume();
    }

    @Override
    // metoda wywoływana, gdy aplikacja się zatrzyma
    public void onPause() {
        // wywołanie metod zatrzymania konstruktora klasy nadrzędnej oraz mapy
        super.onPause();
        mapaHistoriaWidok.onPause();
    }


}