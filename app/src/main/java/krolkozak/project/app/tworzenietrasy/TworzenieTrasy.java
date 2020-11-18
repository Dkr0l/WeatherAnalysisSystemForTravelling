package krolkozak.project.app.tworzenietrasy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.time.OffsetDateTime;
import java.util.Objects;

import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.pomocnicze.AktualnaTrasaTekst;
import krolkozak.project.app.tworzenietrasy.popup.PopupCzas;
import krolkozak.project.app.tworzenietrasy.popup.PopupPrzystanek;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static krolkozak.project.app.tworzenietrasy.Mapa.kontekst;
import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

public class TworzenieTrasy extends Activity {
    // pola tekstowe z autouzupełnianiem
    protected static AutoCompleteTextView poczatekAutouzupelnianie;
    protected static AutoCompleteTextView koniecAutouzupelnianie;
    // przyciski
    protected static Button wyczyscPoczatekPrzycisk;
    protected static Button wyczyscKoniecPrzycisk;
    protected static Button zatwierdzTrasePrzycisk;
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
        poczatekAutouzupelnianie = (AutoCompleteTextView) findViewById(R.id.poczatekAuto);
        koniecAutouzupelnianie = (AutoCompleteTextView) findViewById(R.id.koniecAuto);

        // przycisków
        wyczyscPoczatekPrzycisk = (Button) findViewById(R.id.wyczyscPoczatekPrzycisk);
        wyczyscKoniecPrzycisk = (Button) findViewById(R.id.wyczyscKoniecPrzycisk);
        zatwierdzTrasePrzycisk = (Button) findViewById(R.id.zatwierdzTrasePrzycisk);

        // klawiatury i lokalizacji
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manadzerLokalizacji = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // przypisanie widoku wyświetlającego tekst aktualnych danych trasy do zmiennej i dodanie paska przewijania
        twojaTrasaTekst = (TextView) findViewById(R.id.twojaTrasaTekst);
        twojaTrasaTekst.setMovementMethod(new ScrollingMovementMethod());
        aktualnaTrasaTekst = new AktualnaTrasaTekst();

        // utworzenie obkietu klasy Autouzupelnianie
        //autouzupelnianie = new Autouzupelnianie();
        Autouzupelnianie poczAuto = new Autouzupelnianie(poczatekAutouzupelnianie, wyczyscPoczatekPrzycisk, 1);
        Autouzupelnianie koniecAuto = new Autouzupelnianie(koniecAutouzupelnianie, wyczyscKoniecPrzycisk, 2);

        // przypisanie okna wybierania czasu wyjazdu do przycisku
        ((Button) findViewById(R.id.czasPrzycisk)).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, PopupCzas.class);
            startActivityForResult(popupIntent, DATA_WYJAZDU);
        });

        // przypisanie okna dodawania przystanku do przycisku
        ((Button) findViewById(R.id.dodajPunktPrzycisk)).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, PopupPrzystanek.class);
            startActivity(popupIntent);
        });

        // ustawienie pozycji na liście wyboru środka transportu
        Spinner lista_transport = (Spinner) findViewById(R.id.wybor_srodka_transportu);

        zatwierdzTrasePrzycisk.setOnClickListener(v -> {
            String srodekTransportu=(String)lista_transport.getSelectedItem();
            int wybranyTransport=lista_transport.getSelectedItemPosition();
            if(odlegloscKM(poczAuto.pomocDlugGeog, poczAuto.pomocSzerGeog, koniecAuto.pomocDlugGeog, koniecAuto.pomocSzerGeog)<200 ||(wybranyTransport!=2 && wybranyTransport!=3)) {
                Intent intent = new Intent();
                intent.putExtra("stworzono trase", srodekTransportu);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Trasa za długa na wybrany środek transportu!", Toast.LENGTH_LONG).show();
            }
        });

        // dodanie nasłuchiwacza kliknięcia w przycisk "ZNAJDŹ TRASĘ"
        Mapa.znajdzTrasePrzycisk.setOnClickListener(v -> {
            // przypisz pobrane szerkości i długości geograficzne z pierwszego i drugiego pola wyboru trasy
            // do zmiennych, które zostaną użyte przy tworzeniu trasy na mapie
            Mapa.trasa.szerGeog1 = poczAuto.pomocSzerGeog;
            Mapa.trasa.dlugGeog1 = poczAuto.pomocDlugGeog;
            Mapa.trasa.szerGeog2 = koniecAuto.pomocSzerGeog;
            Mapa.trasa.dlugGeog2 = koniecAuto.pomocDlugGeog;

            // wywołanie metody, która obliczy i wyświetli trasę wraz z punktami pogodowymi na mapie
            try {
                Mapa.trasa.odswiezMape(Mapa.kontekst);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Mapa.trasa.przystanki.clear();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DATA_WYJAZDU:
                if (resultCode == Activity.RESULT_OK) {
                    if (PopupCzas.dataWybrana) {
                        Mapa.trasa.czasWyjazdu = (OffsetDateTime) Objects.requireNonNull(data.getExtras()).getSerializable("czasWyjazdu");
                        Log.i(nazwaApki, "Ustawiono czas wyjazdu: " + Mapa.trasa.czasWyjazdu);
                    } else {
                        Mapa.trasa.czasWyjazdu = OffsetDateTime.now();
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