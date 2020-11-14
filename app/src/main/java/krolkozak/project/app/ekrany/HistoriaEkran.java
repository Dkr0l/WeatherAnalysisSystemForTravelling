package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import krolkozak.project.app.R;
import krolkozak.project.app.bazadanych.Historia;
import krolkozak.project.app.pomocnicze.RekordTabeliHistoria;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoriaEkran extends Activity {
    private String idUzytkownika;
    private ArrayList<Historia> dokumentyHistorii;
    private TextView historiaTekstInfo;
    private String[] naglowkiTabeliHistorii = {"START", "KONIEC", "DATA"};
    private String[][] rekordyTabeliHistorii;
    private TableView<String[]> tabelaHistoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historia_ekran);

        idUzytkownika = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dokumentyHistorii = new ArrayList<Historia>();
        historiaTekstInfo = (TextView) findViewById(R.id.historiaTekstInfo);
        tabelaHistoria = (TableView<String[]>) findViewById(R.id.historiaTabela);
        tabelaHistoria.setVisibility(View.INVISIBLE);

        pobierzDokumentyHistoriiUztkownika();
    }

    private void pobierzDokumentyHistoriiUztkownika() {
        Log.i(nazwaApki, "Pobieranie dokumentów historii o id_uzytkownika: " + idUzytkownika);

        FirebaseFirestore.getInstance().collection("historia").whereEqualTo("id_uzytkownika", idUzytkownika).get().addOnSuccessListener(documentSnapshots -> {
            if (documentSnapshots.isEmpty()) {
                Log.i(nazwaApki, "Nie znaleziono dokumentów historii");
                historiaTekstInfo.setText("Brak tras.");
                tabelaHistoria.setVisibility(View.INVISIBLE);
                return;
            }

            Log.i(nazwaApki, "Pobrano dokumeny historii z bazy (" + documentSnapshots.size() + ")");

            ArrayList<Historia> typyDokumentow = (ArrayList<Historia>) documentSnapshots.toObjects(Historia.class);
            dokumentyHistorii.addAll(typyDokumentow);

            for (Historia dokumentHistorii : dokumentyHistorii) {
                Log.i(nazwaApki, "Dokument historii: " + dokumentHistorii.pobierzObiekt());
            }

            historiaTekstInfo.setVisibility(View.GONE);
            wyswietlDokumentyHistorii();
        }).addOnFailureListener(e -> {
            Log.i(nazwaApki, "Błąd pobierania dokumentów historii: " + e.getMessage());
            historiaTekstInfo.setText("Błąd pobierania tras.");
            tabelaHistoria.setVisibility(View.INVISIBLE);
        });
    }

    private void wyswietlDokumentyHistorii() {
        tabelaHistoria.setVisibility(View.VISIBLE);

        tabelaHistoria.setColumnCount(3);
        tabelaHistoria.setHeaderBackgroundColor(Color.GRAY);

        ArrayList<RekordTabeliHistoria> listaRekordowTabeliHistoria = new ArrayList<RekordTabeliHistoria>();

        for (Historia dokumentHistorii : dokumentyHistorii) {
            RekordTabeliHistoria rekordTabeliHistoria = new RekordTabeliHistoria(dokumentHistorii.getLokalizacja_poczatkowa(), dokumentHistorii.getLokalizacja_koncowa(), dokumentHistorii.getCzas_wyjazdu());
            listaRekordowTabeliHistoria.add(rekordTabeliHistoria);
        }

        rekordyTabeliHistorii = new String[listaRekordowTabeliHistoria.size()][3];

        for (int i = 0; i < listaRekordowTabeliHistoria.size(); i++) {
            RekordTabeliHistoria rekord = listaRekordowTabeliHistoria.get(i);

            rekordyTabeliHistorii[i][0] = rekord.getStart();
            rekordyTabeliHistorii[i][1] = rekord.getKoniec();
            rekordyTabeliHistorii[i][2] = rekord.getData();
        }

        tabelaHistoria.setHeaderAdapter(new SimpleTableHeaderAdapter(this, naglowkiTabeliHistorii));
        tabelaHistoria.setDataAdapter(new SimpleTableDataAdapter(this, rekordyTabeliHistorii));

        dodajNasluchiwaczKliknieciaRekordu();
    }

    private void dodajNasluchiwaczKliknieciaRekordu() {
        tabelaHistoria.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                String[] rekord = (String[]) clickedData;
                RekordTabeliHistoria rekordTabeliHistoria = new RekordTabeliHistoria(rekord[0], rekord[1], rekord[2]);

                Log.i(nazwaApki, "Kliknięty wiesz nr " + rowIndex + ", dane: " + rekordTabeliHistoria.pobierzObiekt());

                // Toast.makeText(getApplicationContext(), "Wiersz numer " + (rowIndex + 1) + ", trasa: " + rekordTabeliHistoria.pobierzObiekt(), Toast.LENGTH_LONG).show();

                Historia dokumentHistorii = dokumentyHistorii.get(rowIndex);
                Log.i(nazwaApki, "Kliknięty dokument historii w bazie: " + dokumentHistorii.pobierzObiekt());

                Intent ekranMapaHistoria = new Intent(getApplicationContext(), MapaHistoria.class);
                ekranMapaHistoria.putExtra("dokument_historii", dokumentHistorii);
                startActivity(ekranMapaHistoria);
            }
        });
    }
}