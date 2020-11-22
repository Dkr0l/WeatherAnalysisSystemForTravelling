package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import krolkozak.project.app.R;
import krolkozak.project.app.bazadanych.Historia;
import krolkozak.project.app.pomocnicze.RekordTabeliHistoria;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

/*

TODO:
 - dodać tryb ciemny
 - dodać przycisk do wprowadzenia danych wybranej trasy z historii do widoku tworzenia trasy

*/

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoriaEkran extends Activity {
    private String idUzytkownika;
    private ArrayList<Historia> dokumentyHistorii;
    private ArrayList<String> idDokumentowHistorii;
    private TextView historiaTekstInfo;
    private String[] naglowkiTabeliHistorii = {"LP", "START", "KONIEC", "DATA"};
    private String[][] rekordyTabeliHistorii;
    private TableView<String[]> tabelaHistoria;
    private TextView historiaTekstInfo2;
    private EditText historiaNumerRekordu;
    private Button historiaUsunRekordPrzycisk;
    private LinearLayout historiaLayoutDolny;
    private boolean dodanoNasluchiwaczKlieknieciaRekordu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historia_ekran);

        idUzytkownika = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dokumentyHistorii = new ArrayList<Historia>();
        idDokumentowHistorii = new ArrayList<String>();
        historiaTekstInfo = (TextView) findViewById(R.id.historiaTekstInfo);
        tabelaHistoria = (TableView<String[]>) findViewById(R.id.historiaTabela);
        tabelaHistoria.setVisibility(View.INVISIBLE);

        historiaLayoutDolny = (LinearLayout) findViewById(R.id.historiaLayoutDolny);
        historiaLayoutDolny.setVisibility(View.INVISIBLE);

        historiaNumerRekordu = (EditText) findViewById(R.id.historiaNumerRekordu);
        historiaUsunRekordPrzycisk = (Button) findViewById(R.id.historiaUsunRekordPrzycisk);
        historiaTekstInfo2 = (TextView) findViewById(R.id.historiaTekstInfo2);

        dodanoNasluchiwaczKlieknieciaRekordu = false;

        pobierzDokumentyUztkownika();

        // PRZYCISK "USUŃ REKORD"
        historiaUsunRekordPrzycisk.setOnClickListener(v -> {
            int indeks = Integer.parseInt(historiaNumerRekordu.getText().toString()) - 1;

            if (indeks >= 0 && indeks < dokumentyHistorii.size()) {
                usunRekordTabeli(indeks);

                View widok = getCurrentFocus();
                if (widok != null) {
                    InputMethodManager manadzerWprowadzania = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manadzerWprowadzania.hideSoftInputFromWindow(widok.getWindowToken(), 0);
                }

                historiaNumerRekordu.setText("");
            } else {
                String tekstPrzedialDoWyswietlenia = dokumentyHistorii.size() > 1 ? ("1-" + dokumentyHistorii.size()) : String.valueOf(dokumentyHistorii.size());
                Toast.makeText(getApplicationContext(), "Wprowadź prawidłowy numer rekordu tabeli (" + tekstPrzedialDoWyswietlenia + ")", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void usunRekordTabeli(int indeks) {
        Log.i(nazwaApki, "Usuwanie dokumentu o indeksie: " + indeks);

        final String idDokumentu = idDokumentowHistorii.get(indeks);

        FirebaseFirestore.getInstance().collection("historia").document(idDokumentu).delete().addOnSuccessListener(aVoid -> {
            Log.i(nazwaApki, "Pomyślnie usunięto rekord z bazy danych");

            dokumentyHistorii.remove(indeks);
            idDokumentowHistorii.remove(indeks);

            wyswietlDokumenty();
        }).addOnFailureListener(e -> Log.i(nazwaApki, "Błąd usuwania rekordu z bazy danych: " + e.getMessage()));
    }

    private void pobierzDokumentyUztkownika() {
        Log.i(nazwaApki, "Pobieranie dokumentów historii o id_uzytkownika: " + idUzytkownika);

        FirebaseFirestore.getInstance().collection("historia").whereEqualTo("id_uzytkownika", idUzytkownika).get().addOnSuccessListener(documentSnapshots -> {
            if (documentSnapshots.isEmpty()) {
                Log.i(nazwaApki, "Nie znaleziono dokumentów historii");
                historiaTekstInfo.setText("Brak tras.");
                tabelaHistoria.setVisibility(View.INVISIBLE);
                historiaLayoutDolny.setVisibility(View.INVISIBLE);
                return;
            }

            Log.i(nazwaApki, "Pobrano dokumenty historii z bazy (" + documentSnapshots.size() + ")");

            ArrayList<Historia> typyDokumentow = (ArrayList<Historia>) documentSnapshots.toObjects(Historia.class);
            dokumentyHistorii.addAll(typyDokumentow);

            List<DocumentSnapshot> migawkaDokumentowHistorii = documentSnapshots.getDocuments();

            for (int i = 0; i < dokumentyHistorii.size(); i++) {
                Historia dokumentHistorii = dokumentyHistorii.get(i);

                final String idDokumentu = migawkaDokumentowHistorii.get(i).getId();
                idDokumentowHistorii.add(idDokumentu);

                Log.i(nazwaApki, "Dokument historii (" + idDokumentu + "): " + dokumentHistorii.pobierzObiekt());
            }

            historiaTekstInfo.setVisibility(View.GONE);
            historiaLayoutDolny.setVisibility(View.GONE);
            wyswietlDokumenty();
        }).addOnFailureListener(e -> {
            Log.i(nazwaApki, "Błąd pobierania dokumentów historii: " + e.getMessage());
            historiaTekstInfo.setText("Błąd pobierania tras.");
            tabelaHistoria.setVisibility(View.INVISIBLE);
            historiaLayoutDolny.setVisibility(View.INVISIBLE);
        });
    }

    private void wyswietlDokumenty() {
        String tekstPrzedialDoWyswietlenia = dokumentyHistorii.size() > 1 ? ("1-" + dokumentyHistorii.size()) : String.valueOf(dokumentyHistorii.size());
        historiaTekstInfo2.setText("Wprowadź numer rekordu (" + tekstPrzedialDoWyswietlenia + "):");

        if (dokumentyHistorii.size() == 0) {
            historiaTekstInfo.setVisibility(View.VISIBLE);
            historiaTekstInfo.setText("Brak tras.");

            tabelaHistoria.setVisibility(View.INVISIBLE);
            historiaLayoutDolny.setVisibility(View.INVISIBLE);

            return;
        }

        tabelaHistoria.setVisibility(View.VISIBLE);
        historiaLayoutDolny.setVisibility(View.VISIBLE);

        tabelaHistoria.setColumnCount(naglowkiTabeliHistorii.length);
        tabelaHistoria.setHeaderBackgroundColor(Color.GRAY);

        ArrayList<RekordTabeliHistoria> listaRekordowTabeliHistoria = new ArrayList<RekordTabeliHistoria>();

        for (Historia dokumentHistorii : dokumentyHistorii) {
            RekordTabeliHistoria rekordTabeliHistoria = new RekordTabeliHistoria(dokumentHistorii.getLokalizacja_poczatkowa(), dokumentHistorii.getLokalizacja_koncowa(), dokumentHistorii.getCzas_wyjazdu());
            listaRekordowTabeliHistoria.add(rekordTabeliHistoria);
        }

        rekordyTabeliHistorii = new String[listaRekordowTabeliHistoria.size()][naglowkiTabeliHistorii.length];

        for (int i = 0; i < listaRekordowTabeliHistoria.size(); i++) {
            RekordTabeliHistoria rekord = listaRekordowTabeliHistoria.get(i);

            rekordyTabeliHistorii[i][0] = String.valueOf(i + 1);
            rekordyTabeliHistorii[i][1] = rekord.getStart();
            rekordyTabeliHistorii[i][2] = rekord.getKoniec();
            rekordyTabeliHistorii[i][3] = rekord.getData();
        }

        tabelaHistoria.setHeaderAdapter(new SimpleTableHeaderAdapter(this, naglowkiTabeliHistorii));
        tabelaHistoria.setDataAdapter(new SimpleTableDataAdapter(this, rekordyTabeliHistorii));

        if (!dodanoNasluchiwaczKlieknieciaRekordu) {
            dodajNasluchiwaczKliknieciaRekordu();
            dodanoNasluchiwaczKlieknieciaRekordu = true;
        }
    }

    private void dodajNasluchiwaczKliknieciaRekordu() {
        tabelaHistoria.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                String[] rekord = (String[]) clickedData;
                RekordTabeliHistoria rekordTabeliHistoria = new RekordTabeliHistoria(rekord[1], rekord[2], rekord[3]);

                Log.i(nazwaApki, "Kliknięty wiesz nr " + rowIndex + ", dane: " + rekordTabeliHistoria.pobierzObiekt());

                Historia dokumentHistorii = dokumentyHistorii.get(rowIndex);
                final String idDokumentu = idDokumentowHistorii.get(rowIndex);

                Log.i(nazwaApki, "Kliknięty dokument historii w bazie (" + idDokumentu + "): " + dokumentHistorii.pobierzObiekt());

                Intent ekranMapaHistoria = new Intent(getApplicationContext(), MapaHistoria.class);
                ekranMapaHistoria.putExtra("dokument_historii", dokumentHistorii);
                startActivity(ekranMapaHistoria);
            }
        });
    }
}