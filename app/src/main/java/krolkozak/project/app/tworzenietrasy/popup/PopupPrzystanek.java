package krolkozak.project.app.tworzenietrasy.popup;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.RequiresApi;

import krolkozak.project.app.R;
import krolkozak.project.app.tworzenietrasy.Autouzupelnianie;
import krolkozak.project.app.tworzenietrasy.Mapa;
import krolkozak.project.app.tworzenietrasy.PunkPostoju;
import krolkozak.project.app.tworzenietrasy.TworzenieTrasy;

public class PopupPrzystanek extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_przystanek);
        DisplayMetrics parametryOkna = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(parametryOkna);
        int szerokosc = parametryOkna.widthPixels;
        int wysokosc = parametryOkna.heightPixels;
        getWindow().setLayout((int) (szerokosc * .8), (int) (wysokosc * .8));

        AutoCompleteTextView przystanekAutouzupelnianie = (AutoCompleteTextView) findViewById(R.id.przystanekAuto);
        Button dodajPrzystanekPrzycisk = (Button) findViewById(R.id.czasDodajPrzystanekPrzycisk);
        NumberPicker dlugoscPostojuGodziny = (NumberPicker) findViewById(R.id.dlugoscPostojuGodziny);
        NumberPicker dlugoscPostojuMinuty = (NumberPicker) findViewById(R.id.dlugoscPostojuMinuty);
        dlugoscPostojuMinuty.setMaxValue(59);
        dlugoscPostojuGodziny.setMaxValue(24);

        Autouzupelnianie przystanekAuto = new Autouzupelnianie(przystanekAutouzupelnianie, findViewById(R.id.wyczyscPrzystanekPrzycisk), 3);
        dodajPrzystanekPrzycisk.setOnClickListener(v -> {
            int czasPostojuMinuty = dlugoscPostojuMinuty.getValue() + 60 * dlugoscPostojuGodziny.getValue();
            //dodanie przystanku do listy punktów
            Mapa.trasa.przystanki.add(new PunkPostoju(przystanekAuto.pomocSzerGeog, przystanekAuto.pomocDlugGeog, przystanekAuto.nazwaMiejsca, czasPostojuMinuty));

            String aktualnePrzystanki = TworzenieTrasy.aktualnaTrasaTekst.getPrzystankiTekst();
            String nowyPrzystanekTekst = przystanekAuto.nazwaMiejsca + " (" + czasPostojuMinuty + "min)";
            String nowePrzystanki = aktualnePrzystanki == "" ? nowyPrzystanekTekst : aktualnePrzystanki + ", " + nowyPrzystanekTekst;
            TworzenieTrasy.aktualnaTrasaTekst.setPrzystankiTekst(nowePrzystanki);
            TworzenieTrasy.zaktualiujTwojaTrasaTekst();

            finish();
        });
    }
}
