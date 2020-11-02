package krolkozak.project.app.ekrany;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import krolkozak.project.app.R;
import krolkozak.project.app.tworzenietrasy.Mapa;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        getSupportActionBar().hide();

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
    }
}