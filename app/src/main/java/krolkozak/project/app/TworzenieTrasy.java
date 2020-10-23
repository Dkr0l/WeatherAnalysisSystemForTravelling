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
    public static AutoCompleteTextView poczatekAutouzupelnianie;
    public static AutoCompleteTextView koniecAutouzupelnianie;
    // przyciski
    public static Button wyczyscPoczatekPrzycisk;
    public static Button wyczyscKoniecPrzycisk;
    // klawiatura
    public static InputMethodManager inputMethodManager;
    // menadżer lokalizacji
    public static LocationManager manadzerLokalizacji;
    // obiekt klasy Autouzupelnianie
    private Autouzupelnianie autouzupelnianie;
    public static Button zatwierdzTrasePrzycisk;
    // komunikacja między aktywnościami
    public static final int DATA_WYJAZDU = 101;

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
        autouzupelnianie = new Autouzupelnianie(GlownaAktywnosc.kontekst, GlownaAktywnosc.trasa);

        // przypisanie okna tworzenia trasy do przycisku
        ((Button) findViewById(R.id.czasPrzycisk)).setOnClickListener(v -> {
            Intent popupIntent = new Intent(this, Popup.class);
            startActivityForResult(popupIntent, DATA_WYJAZDU);
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