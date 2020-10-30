package krolkozak.project.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static krolkozak.project.app.GlownaAktywnosc.trasa;

public class PopupPrzystanek extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupprzystanek);
        DisplayMetrics parametryOkna = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(parametryOkna);
        int szerokosc = parametryOkna.widthPixels;
        int wysokosc = parametryOkna.heightPixels;
        getWindow().setLayout((int) (szerokosc * .8), (int) (wysokosc * .8));

        AutoCompleteTextView przystanekAutouzupelnianie = (AutoCompleteTextView) findViewById(R.id.przystanekAuto);
        Button dodajPrzystanekPrzycisk = (Button) findViewById(R.id.czasDodajPrzystanekPrzycisk);
        NumberPicker dlugoscPostojuGodziny=(NumberPicker) findViewById(R.id.dlugoscPostojuGodziny);
        NumberPicker dlugoscPostojuMinuty=(NumberPicker) findViewById(R.id.dlugoscPostojuMinuty);
        dlugoscPostojuMinuty.setMaxValue(59);
        dlugoscPostojuGodziny.setMaxValue(24);

        int czasPostojuMinuty=dlugoscPostojuMinuty.getValue()+60*dlugoscPostojuGodziny.getValue();
        Autouzupelnianie przystanekAuto=new Autouzupelnianie(przystanekAutouzupelnianie);
        dodajPrzystanekPrzycisk.setOnClickListener(v -> {
            //dodanie przystanku do listy punktów
            GlownaAktywnosc.trasa.przystanki.add(new PunkPostoju(przystanekAuto.pomocSzerGeog, przystanekAuto.pomocDlugGeog, przystanekAuto.nazwaMiejsca, czasPostojuMinuty));
            finish();
        });
    }
}
