package krolkozak.project.app.tworzenietrasy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Objects;

import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.pomocnicze.AktualnaTrasaTekst;
import krolkozak.project.app.tworzenietrasy.popup.PopupCzas;
import krolkozak.project.app.tworzenietrasy.popup.PopupPrzystanek;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;
import static krolkozak.project.app.tworzenietrasy.Mapa.trasa;

public class TworzenieTrasy extends Activity {
    // pola tekstowe z autouzupełnianiem
    protected static AutoCompleteTextView poczatekAutouzupelnianie;
    protected static AutoCompleteTextView koniecAutouzupelnianie;
    // przyciski
    protected static Button wyczyscPoczatekPrzycisk;
    protected static Button wyczyscKoniecPrzycisk;
    protected static Button zatwierdzTrasePrzycisk;
    private static Button wyczyscTrasePrzycisk;
    // komunikacja między aktywnościami
    private static final int DATA_WYJAZDU = 101;
    // klawiatura
    protected static InputMethodManager inputMethodManager;
    // menadżer lokalizacji
    protected static LocationManager manadzerLokalizacji;
    // tekst aktualnej trasy
    protected static TextView twojaTrasaTekst;
    public static AktualnaTrasaTekst aktualnaTrasaTekst;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Ustawienia.trybCiemnyAktywny()) {
            setContentView(R.layout.trybciemnytworzenietrasy);
        }else{
            setContentView(R.layout.tworzenietrasy);
        }

        Log.i(nazwaApki, "Tworzenie trasy");

        // -------------- INICJALIZACJA ZMIENNYCH --------------
        // przypisanie referencji
        // pól tekstowych z automatycznymi podpowiedziami do zmiennych klasy
        poczatekAutouzupelnianie = findViewById(R.id.poczatekAuto);
        koniecAutouzupelnianie = findViewById(R.id.koniecAuto);
        if(aktualnaTrasaTekst==null)aktualnaTrasaTekst = new AktualnaTrasaTekst();

        // przycisków
        wyczyscPoczatekPrzycisk = findViewById(R.id.wyczyscPoczatekPrzycisk);
        wyczyscKoniecPrzycisk = findViewById(R.id.wyczyscKoniecPrzycisk);
        zatwierdzTrasePrzycisk = findViewById(R.id.zatwierdzTrasePrzycisk);
        wyczyscTrasePrzycisk = findViewById(R.id.wyczyscTrasePrzycisk);

        // klawiatury i lokalizacji
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manadzerLokalizacji = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // przypisanie widoku wyświetlającego tekst aktualnych danych trasy do zmiennej i dodanie paska przewijania
        twojaTrasaTekst = findViewById(R.id.twojaTrasaTekst);
        twojaTrasaTekst.setMovementMethod(new ScrollingMovementMethod());

        // utworzenie obkietu klasy Autouzupelnianie
        //autouzupelnianie = new Autouzupelnianie();
        Autouzupelnianie poczAuto = new Autouzupelnianie(poczatekAutouzupelnianie, wyczyscPoczatekPrzycisk, 1);
        Autouzupelnianie koniecAuto = new Autouzupelnianie(koniecAutouzupelnianie, wyczyscKoniecPrzycisk, 2);

        // przypisanie okna wybierania czasu wyjazdu do przycisku
        findViewById(R.id.czasPrzycisk).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, PopupCzas.class);
            startActivityForResult(popupIntent, DATA_WYJAZDU);
        });

        // przypisanie okna dodawania przystanku do przycisku
        findViewById(R.id.dodajPunktPrzycisk).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, PopupPrzystanek.class);
            startActivity(popupIntent);
        });

        // ustawienie pozycji na liście wyboru środka transportu
        Spinner lista_transport = findViewById(R.id.wybor_srodka_transportu);

        //Utworzenie nowej, pustej trasy
        wyczyscTrasePrzycisk.setOnClickListener(v -> {
            trasa.przystanki.clear();
            aktualnaTrasaTekst = new AktualnaTrasaTekst();
            zaktualiujTwojaTrasaTekst();
            poczatekAutouzupelnianie.setText("");
            koniecAutouzupelnianie.setText("");
            lista_transport.setSelection(0);
            zatwierdzTrasePrzycisk.setText("WYBIERZ PUNKTY");
            zatwierdzTrasePrzycisk.setEnabled(false);
        });

        zatwierdzTrasePrzycisk.setOnClickListener(v -> {
            String srodekTransportu=(String)lista_transport.getSelectedItem();
            int wybranyTransport=lista_transport.getSelectedItemPosition();
            if(odlegloscKM(poczAuto.pomocDlugGeog, poczAuto.pomocSzerGeog, koniecAuto.pomocDlugGeog, koniecAuto.pomocSzerGeog)<200 ||(wybranyTransport!=2 && wybranyTransport!=3)) {
                // przypisz pobrane szerkości i długości geograficzne z pierwszego i drugiego pola wyboru trasy
                // do zmiennych, które zostaną użyte przy tworzeniu trasy na mapie
                trasa.szerGeog1 = poczAuto.pomocSzerGeog;
                trasa.dlugGeog1 = poczAuto.pomocDlugGeog;
                trasa.szerGeog2 = koniecAuto.pomocSzerGeog;
                trasa.dlugGeog2 = koniecAuto.pomocDlugGeog;

                Mapa.wyswietlTrasePrzycisk.setEnabled(true);

                Intent intent = new Intent();
                intent.putExtra("stworzono trase", srodekTransportu);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Trasa za długa na wybrany środek transportu!", Toast.LENGTH_LONG).show();
            }
        });

        //odtwarzanie trasy z historii
        Intent ekranTworzenieTrasy = getIntent();
        String punktyTrasy = ekranTworzenieTrasy.getStringExtra("punkty_trasy");
        if(punktyTrasy != null) {
            Log.i(nazwaApki, "Punkty trasy do odtworzenia z historii (ekran tworzenia trasy): " + punktyTrasy);

            trasa.przystanki.clear();

            try {
                JSONArray punktyTrasyJSON = new JSONArray(punktyTrasy);
                String przystankiTekst = "";

                for (int i = 0; i < punktyTrasyJSON.length(); i++) {
                    JSONObject punktyTrasyJSONObject = (JSONObject) punktyTrasyJSON.getJSONObject(i);

                    String nazwa = punktyTrasyJSONObject.getString("nazwa");
                    double szerGeog = punktyTrasyJSONObject.getDouble("szer_geog");
                    double dlugGeog = punktyTrasyJSONObject.getDouble("dlug_geog");
                    String indeks = punktyTrasyJSONObject.getString("indeks");
                    int czasPostojuMinuty = punktyTrasyJSONObject.getInt("czas_postoju");

                    if(indeks.equals("POCZATEK")) {
                        poczatekAutouzupelnianie.setText(nazwa);
                        aktualnaTrasaTekst.setPoczątekTrasyTekst(nazwa);

                        poczAuto.pomocSzerGeog = szerGeog;
                        poczAuto.pomocDlugGeog = dlugGeog;
                    } else if(indeks.equals("KONIEC")) {
                        koniecAutouzupelnianie.setText(nazwa);
                        aktualnaTrasaTekst.setKoniecTrasyTekst(nazwa);

                        koniecAuto.pomocSzerGeog = szerGeog;
                        koniecAuto.pomocDlugGeog = dlugGeog;
                    } else if(indeks.equals("PRZYSTANEK")) {
                        PunktPostoju punktPostoju = new PunktPostoju(szerGeog, dlugGeog, nazwa, czasPostojuMinuty);
                        trasa.przystanki.add(punktPostoju);

                        String nowyPrzystanekTekst = nazwa + " (" + czasPostojuMinuty + "min)";
                        przystankiTekst = przystankiTekst.equals("") ? nowyPrzystanekTekst : przystankiTekst + ", " + nowyPrzystanekTekst;
                    }
                }

                aktualnaTrasaTekst.setPrzystankiTekst(przystankiTekst);
                zaktualiujTwojaTrasaTekst();

                TworzenieTrasy.zatwierdzTrasePrzycisk.setText("ZATWIERDŹ TRASĘ");
                TworzenieTrasy.zatwierdzTrasePrzycisk.setEnabled(true);

                Mapa.wyswietlTrasePrzycisk.setEnabled(true);
            } catch (JSONException e) {
                Log.i(nazwaApki, "Nie udało się pobrać punktów trasy do odtworzenia z historii: " + e.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DATA_WYJAZDU:
                if (resultCode == Activity.RESULT_OK) {
                    if (PopupCzas.dataWybrana) {
                        trasa.czasWyjazdu = (OffsetDateTime) Objects.requireNonNull(data.getExtras()).getSerializable("czasWyjazdu");
                        Log.i(nazwaApki, "Ustawiono czas wyjazdu: " + trasa.czasWyjazdu);
                    } else {
                        trasa.czasWyjazdu = OffsetDateTime.now();
                    }
                }
        }
    }

    public static void zaktualiujTwojaTrasaTekst() {
        twojaTrasaTekst.setText(aktualnaTrasaTekst.pobierzPelnyTekstTrasy());
    }

    private double odlegloscKM(double dlugosc1, double szerokosc1, double dlugosc2, double szerokosc2){
        double kmDlugosc=abs(dlugosc1-dlugosc2)*111.32;
        double kmSzerokosc=40.075*cos(abs(szerokosc1-szerokosc2))/360;
        return sqrt(pow(kmDlugosc, 2) + pow(kmSzerokosc, 2));
    }

}