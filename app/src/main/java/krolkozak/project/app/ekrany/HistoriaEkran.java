package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import krolkozak.project.app.R;
import krolkozak.project.app.bazadanych.Historia;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoriaEkran extends Activity {
    private String id_uzytkownika;
    private ArrayList<Historia> dokumenty_historii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historia_ekran);

        id_uzytkownika = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dokumenty_historii = new ArrayList<Historia>();

        pobierzDokumentyHistoriiUztkownika();
    }

    private void pobierzDokumentyHistoriiUztkownika() {
        Log.i(nazwaApki, "Pobieranie dokumentów historii o id_uzytkownika: " + id_uzytkownika);

        FirebaseFirestore.getInstance().collection("historia").whereEqualTo("id_uzytkownika", id_uzytkownika).get().addOnSuccessListener(documentSnapshots -> {
            if (documentSnapshots.isEmpty()) {
                Log.i(nazwaApki, "Nie znaleziono dokumentów historii");
                return;
            }

            Log.i(nazwaApki, "Pobrano dokumeny historii z bazy (" + documentSnapshots.size() + "):");

            ArrayList<Historia> typyDokumentow = (ArrayList<Historia>) documentSnapshots.toObjects(Historia.class);
            dokumenty_historii.addAll(typyDokumentow);

            for (Historia dokument_historii : dokumenty_historii) {
                Log.i(nazwaApki, "Dokument historii: " + dokument_historii.pobierzObiekt());
            }
        });
    }
}