package krolkozak.project.app.ekrany;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import krolkozak.project.app.R;
import krolkozak.project.app.tworzenietrasy.Mapa;

public class Menu extends Activity {
    // Odnośnik to widoku tekstu powitalnego
    private TextView menuTekstPowitalny;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        // Jeżeli istnieje zalogowany użytkownik - wyświetl powitalny tekst z jego loginem
        menuTekstPowitalny = (TextView) findViewById(R.id.menuTekstPowitalny);
        FirebaseUser zalogowanyUzytkownik = FirebaseAuth.getInstance().getCurrentUser();
        if (zalogowanyUzytkownik != null) {
            menuTekstPowitalny.setText("Witaj " + zalogowanyUzytkownik.getDisplayName() + "!");
        } else {
            menuTekstPowitalny.setText("Witaj!");
        }

        // Kliknięcie w przycisk "MAPA"
        ((Button) findViewById(R.id.mapaPrzycisk)).setOnClickListener(v -> {
            // Przechodzi do ekranu mapy
            Intent intent = new Intent(this, Mapa.class);
            startActivity(intent);
        });

        // Kliknięcie w przycisk "HISTORIA"
        ((Button) findViewById(R.id.historiaPrzycisk)).setOnClickListener(v -> {
            // Przechodzi do ekranu mapy
            Intent intent = new Intent(this, Mapa.class);
            startActivity(intent);
        });

        // Kliknięcie w przycisk "WYLOGUJ"
        ((Button) findViewById(R.id.wylogujPrzycisk)).setOnClickListener(v -> {
            // Pobranie instancji firebase uwierzytelnionego użytkownika i wylogowanie go
            FirebaseAuth.getInstance().signOut();

            // Przejście do ekranu logowania
            Intent intent = new Intent(this, Logowanie.class);
            startActivity(intent);
        });
    }
}