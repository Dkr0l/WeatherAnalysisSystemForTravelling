package krolkozak.project.app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.RequiresApi;

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
        Autouzupelnianie przystanekAuto=new Autouzupelnianie(przystanekAutouzupelnianie, findViewById(R.id.wyczyscPrzystanekPrzycisk));
        dodajPrzystanekPrzycisk.setOnClickListener(v -> {
            //dodanie przystanku do listy punktów
            GlownaAktywnosc.trasa.przystanki.add(new PunkPostoju(przystanekAuto.pomocSzerGeog, przystanekAuto.pomocDlugGeog, przystanekAuto.nazwaMiejsca, czasPostojuMinuty));
            finish();
        });
    }
}
