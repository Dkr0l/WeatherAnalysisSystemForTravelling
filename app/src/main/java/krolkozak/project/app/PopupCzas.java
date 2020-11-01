package krolkozak.project.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PopupCzas extends Activity {
    private DatePicker wyborDaty;
    private TimePicker wyborGodziny;
    private int minuta, godzina, dzien, miesiac, rok;
    protected static boolean dataWybrana = false;
    private OffsetDateTime czasWyjazdu;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupczas);
        DisplayMetrics parametryOkna = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(parametryOkna);
        int szerokosc = parametryOkna.widthPixels;
        int wysokosc = parametryOkna.heightPixels;
        getWindow().setLayout((int) (szerokosc * .8), (int) (wysokosc * .8));

        Button czasZatwierdzPrzycisk = (Button) findViewById(R.id.czasZatwierdzPrzycisk);
        czasZatwierdzPrzycisk.setOnClickListener(v -> {
            //zapisanie daty i czasu
            wyborDaty = (DatePicker) findViewById(R.id.dataWybor);
            wyborGodziny = (TimePicker) findViewById(R.id.czasWybor);
            minuta = wyborGodziny.getMinute();
            godzina = wyborGodziny.getHour();
            dzien = wyborDaty.getDayOfMonth();
            miesiac = wyborDaty.getMonth() + 1;   //DatePicker indeksuje miesiące od zera. "+1" jest niezbędne do poprawnego przeliczenia
            rok = wyborDaty.getYear();
            dataWybrana = true;

            czasWyjazdu = OffsetDateTime.of(rok, miesiac, dzien, godzina, minuta, 0, 0, ZoneOffset.of(OffsetDateTime.now().getOffset().getId()));

            Intent intent = new Intent();
            intent.putExtra("czasWyjazdu", czasWyjazdu);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }
}
