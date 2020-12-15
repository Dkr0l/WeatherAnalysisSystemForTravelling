package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.bazadanych.Historia;
import krolkozak.project.app.pomocnicze.RekordTabeliHistoria;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

/*

TODO:
 - podzielić historie na strony (jakby któryś użytkownik nabił 3k rekordów w historii :D )

*/

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoriaEkran extends Activity {
    private String idUzytkownika;
    private ArrayList<Historia> dokumentyHistorii;
    private ArrayList<String> idDokumentowHistorii;
    private TextView historiaTekstInfo;
    private TableLayout tabelaHistoria;
    private TextView historiaTekstInfo2;
    private EditText historiaNumerRekordu;
    private LinearLayout historiaLayoutDolny;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ustawienia.trybCiemnyAktywny()) {
            setContentView(R.layout.trybciemnyhistoria_ekran);
        } else {
            setContentView(R.layout.historia_ekran);
        }

        idUzytkownika = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        dokumentyHistorii = new ArrayList<>();
        idDokumentowHistorii = new ArrayList<>();
        historiaTekstInfo = (TextView) findViewById(R.id.historiaTekstInfo);
        tabelaHistoria = findViewById(R.id.historiaTabela);
        tabelaHistoria.setVisibility(View.INVISIBLE);

        historiaLayoutDolny = (LinearLayout) findViewById(R.id.historiaLayoutDolny);
        historiaLayoutDolny.setVisibility(View.INVISIBLE);

        historiaNumerRekordu = (EditText) findViewById(R.id.historiaNumerRekordu);
        Button historiaUsunRekordPrzycisk = (Button) findViewById(R.id.historiaUsunRekordPrzycisk);
        historiaTekstInfo2 = (TextView) findViewById(R.id.historiaTekstInfo2);

        pobierzDokumentyUztkownika();

        // PRZYCISK "USUŃ REKORD"
        historiaUsunRekordPrzycisk.setOnClickListener(v -> {
            if (historiaNumerRekordu.getText().toString().equals("")) {
                String tekstPrzedialDoWyswietlenia = dokumentyHistorii.size() > 1 ? ("1-" + dokumentyHistorii.size()) : String.valueOf(dokumentyHistorii.size());
                Toast.makeText(getApplicationContext(), "Wprowadź prawidłowy numer rekordu tabeli (" + tekstPrzedialDoWyswietlenia + ")", Toast.LENGTH_LONG).show();
                return;
            }

            int indeks = Integer.parseInt(historiaNumerRekordu.getText().toString()) - 1;

            if (indeks >= 0 && indeks < dokumentyHistorii.size()) {
                usunRekordTabeli(indeks);

                View widok = getCurrentFocus();
                if (widok != null) {
                    InputMethodManager manadzerWprowadzania = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert manadzerWprowadzania != null;
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

            tabelaHistoria.removeViews(1, Math.max(0, tabelaHistoria.getChildCount() - 1));

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

        ArrayList<RekordTabeliHistoria> listaRekordowTabeliHistoria = new ArrayList<RekordTabeliHistoria>();

        for (Historia dokumentHistorii : dokumentyHistorii) {
            RekordTabeliHistoria rekordTabeliHistoria = new RekordTabeliHistoria(dokumentHistorii.getLokalizacja_poczatkowa(), dokumentHistorii.getLokalizacja_koncowa(), dokumentHistorii.getCzas_wyjazdu());
            listaRekordowTabeliHistoria.add(rekordTabeliHistoria);
        }

        for (int i = 0; i < listaRekordowTabeliHistoria.size(); i++) {
            RekordTabeliHistoria rekord = listaRekordowTabeliHistoria.get(i);

            // Utworzenie wiersza
            final TableRow wiersz = new TableRow(this);
            //wiersz.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            // Inicjalizacja pól w wierszu
            TextView[] tekst = {new TextView(getApplicationContext()), new TextView(getApplicationContext()), new TextView(getApplicationContext()), new TextView(getApplicationContext())};
            if (Ustawienia.trybCiemnyAktywny())
                tekst = new TextView[]{new TextView(new ContextThemeWrapper(this, R.style.AppTheme_DarkElementTheme), null, 0),
                        new TextView(new ContextThemeWrapper(this, R.style.AppTheme_DarkElementTheme), null, 0),
                        new TextView(new ContextThemeWrapper(this, R.style.AppTheme_DarkElementTheme), null, 0),
                        new TextView(new ContextThemeWrapper(this, R.style.AppTheme_DarkElementTheme), null, 0)};

            // Wypełnienie pól w wierszu
            tekst[0].setText(String.valueOf(i + 1));
            tekst[1].setText(rekord.getStart().substring(0, 15) + "…");
            tekst[2].setText(rekord.getKoniec().substring(0, 15) + "…");
            tekst[3].setText(rekord.getData().substring(0, 10));

            //z jakiegoś powodu bez tych przepisań nie działa
            int finalneI = i;
            TextView[] finalnyTekst = tekst;

            wiersz.setOnClickListener(v -> {
                Log.i(nazwaApki, "Kliknięty wiesz nr " + finalneI + ", dane: " + finalnyTekst[3].getText());

                Historia dokumentHistorii = dokumentyHistorii.get(finalneI);
                final String idDokumentu = idDokumentowHistorii.get(finalneI);

                Log.i(nazwaApki, "Kliknięty dokument historii w bazie (" + idDokumentu + "): " + dokumentHistorii.pobierzObiekt());

                Intent ekranMapaHistoria = new Intent(getApplicationContext(), MapaHistoria.class);
                ekranMapaHistoria.putExtra("dokument_historii", dokumentHistorii);
                startActivity(ekranMapaHistoria);
            });

            wiersz.addView(tekst[0]);
            wiersz.addView(tekst[1]);
            wiersz.addView(tekst[2]);
            wiersz.addView(tekst[3]);

            tabelaHistoria.addView(wiersz);
        }
    }
}