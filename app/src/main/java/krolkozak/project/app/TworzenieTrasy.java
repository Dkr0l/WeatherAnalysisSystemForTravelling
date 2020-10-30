package krolkozak.project.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;

import java.time.OffsetDateTime;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tworzenie_trasy);

        Log.i(GlownaAktywnosc.nazwaApki, "Test");

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

        // utworzenie obkietu klasy Autouzupelnianie
        //autouzupelnianie = new Autouzupelnianie();
        Autouzupelnianie poczAuto=new Autouzupelnianie(poczatekAutouzupelnianie);
        Autouzupelnianie koniecAuto=new Autouzupelnianie(koniecAutouzupelnianie);

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
        ArrayAdapter<CharSequence> adapter_transport = ArrayAdapter.createFromResource(this, R.array.srodek_transportu, android.R.layout.simple_spinner_item);
        adapter_transport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lista_transport.setAdapter(adapter_transport);

        zatwierdzTrasePrzycisk.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("stworzono trase", (String)lista_transport.getSelectedItem());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });

        // dodanie nasłuchiwacza kliknięcia w przycisk "ZNAJDŹ TRASĘ"
        GlownaAktywnosc.znajdzTrasePrzycisk.setOnClickListener(v -> {
            // przypisz pobrane szerkości i długości geograficzne z pierwszego i drugiego pola wyboru trasy
            // do zmiennych, które zostaną użyte przy tworzeniu trasy na mapie
            GlownaAktywnosc.trasa.szerGeog1 = poczAuto.pomocSzerGeog;
            GlownaAktywnosc.trasa.dlugGeog1 = poczAuto.pomocDlugGeog;
            GlownaAktywnosc.trasa.szerGeog2 = koniecAuto.pomocSzerGeog;
            GlownaAktywnosc.trasa.dlugGeog2 = koniecAuto.pomocDlugGeog;

            // wywołanie metody, która obliczy i wyświetli trasę wraz z punktami pogodowymi na mapie
            GlownaAktywnosc.trasa.odswiezMape(GlownaAktywnosc.kontekst);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DATA_WYJAZDU:
                if (resultCode == Activity.RESULT_OK) {
                    GlownaAktywnosc.trasa.czasWyjazdu = (OffsetDateTime) data.getExtras().getSerializable("czasWyjazdu");
                    Log.i(GlownaAktywnosc.nazwaApki, "Ustawiono czas wyjazdu: " + GlownaAktywnosc.trasa.czasWyjazdu);
                }
        }
    }
}