package krolkozak.project.app.tworzenietrasy.popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import com.google.type.DateTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import krolkozak.project.app.R;
import krolkozak.project.app.tworzenietrasy.TworzenieTrasy;

public class PopupCzas extends Activity {
    private DatePicker wyborDaty;
    private TimePicker wyborGodziny;
    private int minuta, godzina, dzien, miesiac, rok;
    public static boolean dataWybrana = false;
    private OffsetDateTime czasWyjazdu;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_czas);
        DisplayMetrics parametryOkna = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(parametryOkna);
        int szerokosc = parametryOkna.widthPixels;
        int wysokosc = parametryOkna.heightPixels;
        getWindow().setLayout((int) (szerokosc * .8), (int) (wysokosc * .8));

        Button czasZatwierdzPrzycisk = findViewById(R.id.czasZatwierdzPrzycisk);
        wyborDaty = findViewById(R.id.dataWybor);
        wyborGodziny = findViewById(R.id.czasWybor);

        //ograniczenie kalendarza do 2 tygodzni w przód
        ustawOknoCzasu(wyborDaty);

        czasZatwierdzPrzycisk.setOnClickListener(v -> {
            //zapisanie daty i czasu
            minuta = wyborGodziny.getMinute();
            godzina = wyborGodziny.getHour();
            dzien = wyborDaty.getDayOfMonth();
            miesiac = wyborDaty.getMonth() + 1;   //DatePicker indeksuje miesiące od zera. "+1" jest niezbędne do poprawnego przeliczenia
            rok = wyborDaty.getYear();
            dataWybrana = true;

            czasWyjazdu = OffsetDateTime.of(rok, miesiac, dzien, godzina, minuta, 0, 0, ZoneOffset.of(OffsetDateTime.now().getOffset().getId()));

            TworzenieTrasy.aktualnaTrasaTekst.setCzasWyjazduTekst(String.valueOf(czasWyjazdu.toLocalDateTime()));
            TworzenieTrasy.zaktualiujTwojaTrasaTekst();

            Intent intent = new Intent();
            intent.putExtra("czasWyjazdu", czasWyjazdu);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ustawOknoCzasu(DatePicker kalendarz){
        LocalDate teraz= LocalDate.now();
        final int milisekundWDniu=86400000;
        //przeliczenie na milisekundy
        long milisekundyEpoch=teraz.toEpochDay()*milisekundWDniu;
        kalendarz.setMinDate(milisekundyEpoch);
        kalendarz.setMaxDate(milisekundyEpoch+milisekundWDniu*13);
    }
}
