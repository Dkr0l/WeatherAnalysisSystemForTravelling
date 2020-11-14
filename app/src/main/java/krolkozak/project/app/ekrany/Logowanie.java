package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.OffsetDateTime;

import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.bazadanych.Uzytkownik;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Logowanie extends Activity {
    private GoogleSignInClient klientGoogle;
    private static final int KOD_LOGOWANIA_GOOGLE = 12345;
    private FirebaseAuth uwierzytelnianieFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //wczytanie ustawień z pamięci urządzenia
        try{
            SharedPreferences ustawienia=getSharedPreferences("ApkaPogodowa", MODE_PRIVATE);
            Ustawienia.wczytajZPamieci(ustawienia.getAll());
        }catch(NullPointerException e){
            Log.i(nazwaApki, "Nie udało się wczytać ustawień z pamięci urządzenia!");
        }

        setContentView(R.layout.logowanie);

        // Pobranie instancji uwierzytelniania firebase
        uwierzytelnianieFirebase = FirebaseAuth.getInstance();

        // Wywołanie metody konfiguracji google
        utworzKlientaGoogle();

        // Kliknięcie w przycisk "ZALOGUJ SIĘ"
        ((Button) findViewById(R.id.zalogujPrzycisk)).setOnClickListener(v -> {
            // Wywołanie metody przeprowadzającej procej logowanie z kontem google
            zalogujZGoogle();
        });

    }

    // Metoda wywołana na starcie aktywności
    @Override
    protected void onStart() {
        super.onStart();

        // Pobranie obecnego użytkownika firebase
        FirebaseUser uzytkownikFirebase = uwierzytelnianieFirebase.getCurrentUser();

        // Jeżeli istnieje zalogowany użytkownik aplikacja przechodzi do ekranu menu
        if (uzytkownikFirebase != null) {
            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        }
    }

    // Metoda tworządza żądanie utworzenie konfiguracji klienta google
    private void utworzKlientaGoogle() {
        Log.i(nazwaApki, "Tworzenie żądania konfiguracji klienta google");

        // Konfiguracja logowania z kontem google
        GoogleSignInOptions opcjeLogowaniaGoogle = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Utworzenie obiektu klienta google z podaną konfiguracją
        klientGoogle = GoogleSignIn.getClient(this, opcjeLogowaniaGoogle);
    }

    // Metoda przeprowadzająca proces logowania za pomocą konta google
    private void zalogujZGoogle() {
        Log.i(nazwaApki, "Logowanie z kontem google");

        // Wywołanie okna wyboru konta google
        Intent intentLogowanie = klientGoogle.getSignInIntent();
        startActivityForResult(intentLogowanie, KOD_LOGOWANIA_GOOGLE);
    }

    // Metoda wywołana po zakmnięciu okna intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Rezultat zwrócony z metody klientGoogle.getSignInIntent()
        if (requestCode == KOD_LOGOWANIA_GOOGLE) {
            Task<GoogleSignInAccount> zadanie = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Uwierzytelnianie firebase
                GoogleSignInAccount kontoGoogle = zadanie.getResult(ApiException.class);

                // Pomyślne zalogowanie
                Log.i(nazwaApki, "Pomyślne logowanie z kontem google");
                Log.i(nazwaApki, "Id konta google: " + kontoGoogle.getId());

                uwierzytelnianieFirebaseZGoogle(kontoGoogle.getIdToken());
            } catch (ApiException wyjatek) {
                Log.i(nazwaApki, "Błąd logowania z kontem google: " + wyjatek.getMessage());
            }
        }
    }

    // Metoda uwierzytelniająca firebase z google
    private void uwierzytelnianieFirebaseZGoogle(String idToken) {
        // Utworzenie danych uwierzytelniających z google
        AuthCredential daneUwierzytelniajace = GoogleAuthProvider.getCredential(idToken, null);

        // Logowanie z danymi uwierzytelniającymi i nasłuchiwanie rezultatu
        uwierzytelnianieFirebase.signInWithCredential(daneUwierzytelniajace).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> zadanie) {
                if (zadanie.isSuccessful()) {
                    // Pomyślne logowanie z danymi uwierzytelniającymi
                    Log.i(nazwaApki, "Pomyślne logowanie z danymi uwierzytelniającymi");

                    dodajUzytkownikaDoBazy();

                    // Przejście do ekranu menu
                    Intent intent = new Intent(getApplicationContext(), Menu.class);
                    startActivity(intent);
                } else {
                    // łąd logowanie z danymi uwierzytelniającymi
                    Log.i(nazwaApki, "Błąd logowanie z danymi uwierzytelniającymi: " + zadanie.getException().getMessage());
                }
            }
        });
    }

    // Metoda wprowadząjącego nowego użytkownika do tabeli uzytkownicy
    private void dodajUzytkownikaDoBazy() {
        // Pobranie instancji bazy danych
        FirebaseFirestore bazaDanychRef = FirebaseFirestore.getInstance();

        // Utworzenie obiektu użytkownika do wprowadzenia na podstawie informacji z zalogowanego konta google
        FirebaseUser uzytkownikFirebase = uwierzytelnianieFirebase.getCurrentUser();
        final String uzytkownikId = uzytkownikFirebase.getUid();
        final String uzytkownikLogin = uzytkownikFirebase.getDisplayName();
        final String uzytkownikEmail = uzytkownikFirebase.getEmail();
        final String uzytkownikDataUtworzenia = String.valueOf(OffsetDateTime.now());
        Uzytkownik uzytkownikDoWprowadzenia = new Uzytkownik(uzytkownikId, uzytkownikLogin, uzytkownikEmail, uzytkownikDataUtworzenia);

        Log.i(nazwaApki, "Wprowadzanie nowego użytkownika: " + uzytkownikDoWprowadzenia.pobierzPelneDane());

        // Wywołanie zapytania bazy o dokument z id użytkownika - jeśli nie zostanie zwrócony żadny dokument to nastąpi wprowadzenie użytkownika do tabeli
        bazaDanychRef.collection("uzytkownicy").document(uzytkownikId).get().addOnCompleteListener(new OnCompleteListener() {
            public void onComplete(@NonNull Task zadanie) {
                if (zadanie.isSuccessful()) {
                    // Pobranie migawki dokumentu jeśli udało się wykonać zapytanie
                    DocumentSnapshot migawkaDokumentu = (DocumentSnapshot) zadanie.getResult();
                    Log.i(nazwaApki, "Pobrano dokument: " + migawkaDokumentu);

                    // Jeśli dokument nie jest pusty = wprowadź użytkownika do tabeli
                    if (migawkaDokumentu.getData() == null) {
                        bazaDanychRef.collection("uzytkownicy").document(uzytkownikId).set(uzytkownikDoWprowadzenia);

                        Log.i(nazwaApki, "Wprowadzono użytkownika do bazy");
                    } else {
                        Log.i(nazwaApki, "Użytkownik istnieje już w bazie");
                    }
                } else {
                    Log.i(nazwaApki, "Błąd pobierania dokumentu: " + zadanie.getException().getMessage());
                }
            }
        });
    }
}