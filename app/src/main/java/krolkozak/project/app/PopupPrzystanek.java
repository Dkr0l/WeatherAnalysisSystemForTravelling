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

public class PopupPrzystanek extends Activity {
    private double szerGeog, dlugGeog;
    private String nazwa;
    private int czasPostojuMinuty;

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

        Button czasDodajPrzystanekPrzycisk = (Button) findViewById(R.id.czasDodajPrzystanekPrzycisk);
        czasDodajPrzystanekPrzycisk.setOnClickListener(v -> {
            //dodanie przystanku do listy punktów

            finish();
        });
    }
}
