package krolkozak.project.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Switch;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;

import krolkozak.project.app.ekrany.Menu;

public class Ustawienia extends Activity {

    private Switch przelacznikTrybuCiemnego;
    private Spinner wyborJednostkiOpady;
    private Spinner wyborJednostkiWiatr;
    private Spinner wyborJednostkiTemperatura;

    private static boolean trybCiemnyAktywny=false;
    private static String jednostkiOpady="mm/h";
    private static String jednostkiWiatr="km/h";
    private static String jednostkiTemperatura="c";

    public static boolean trybCiemnyAktywny() {
        return trybCiemnyAktywny;
    }
    public static String jednostkaOpadow(){return jednostkiOpady;}
    public static String jednostkaWiatru(){return jednostkiWiatr;}
    public static String jednostkaTemperatury(){return jednostkiTemperatura;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(trybCiemnyAktywny()) {
            setContentView(R.layout.trybciemnyustawienia);
        }else{
            setContentView(R.layout.ustawienia);
        }
        przelacznikTrybuCiemnego=findViewById(R.id.ustawieniaTrybCiemny);
        wyborJednostkiOpady =findViewById(R.id.ustawieniaJednostkaOpadow);
        wyborJednostkiWiatr =findViewById(R.id.ustawieniaJednostkaPredkosciWiatru);
        wyborJednostkiTemperatura=findViewById(R.id.ustawieniaJednostkaTemperatury);

        //wyświetlenie zapisanych ustawień w interfejsie
        przywrocenieUstawien();

        // zatwierdzenie ustawień
        findViewById(R.id.zatwierdzUstawienia).setOnClickListener(v -> {
            //z interfejsu to zmiennych tymczasowych
            trybCiemnyAktywny=przelacznikTrybuCiemnego.isChecked();
            jednostkiOpady=wyborJednostkiOpady.getSelectedItem().toString();
            jednostkiWiatr=wyborJednostkiWiatr.getSelectedItem().toString();
            jednostkiTemperatura=wyborJednostkiTemperatura.getSelectedItem().toString();

            //ze zmiennych tymczasowych do pamięci urządzenia
            SharedPreferences ustawienia=getSharedPreferences("ApkaPogodowa", MODE_PRIVATE);
            SharedPreferences.Editor edytorUstawien=ustawienia.edit();

            edytorUstawien.putBoolean("trybCiemny", trybCiemnyAktywny);
            edytorUstawien.putString("jednostkiWiatr", jednostkiWiatr);
            edytorUstawien.putString("jednostkiOpady", jednostkiOpady);
            edytorUstawien.putString("jednostkiTemperatura", jednostkiTemperatura);

            edytorUstawien.apply();

            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        });
    }

    private void przywrocenieUstawien(){
        przelacznikTrybuCiemnego.setChecked(trybCiemnyAktywny);

        if(jednostkiOpady!= null && jednostkiOpady.contentEquals("l/m2h")) wyborJednostkiOpady.setSelection(0);
        else if(jednostkiOpady!= null && jednostkiOpady.contentEquals("mm/h")) wyborJednostkiOpady.setSelection(1);

        if(jednostkiWiatr!=null && jednostkiWiatr.contentEquals("km/h")) wyborJednostkiWiatr.setSelection(0);
        else if(jednostkiWiatr!=null && jednostkiWiatr.contentEquals("mph")) wyborJednostkiWiatr.setSelection(1);
        else if(jednostkiWiatr!=null && jednostkiWiatr.contentEquals("m/s")) wyborJednostkiWiatr.setSelection(2);

        if(jednostkiTemperatura!=null && jednostkiTemperatura.contentEquals("C")) wyborJednostkiTemperatura.setSelection(0);
        else if(jednostkiTemperatura!=null && jednostkiTemperatura.contentEquals("F")) wyborJednostkiTemperatura.setSelection(1);
    }

    public static void wczytajZPamieci(Map<String, ?> ustawienia){
        trybCiemnyAktywny=(Boolean) ustawienia.get("trybCiemny");
        jednostkiWiatr=(String) ustawienia.get("jednostkiWiatr");
        jednostkiOpady=(String) ustawienia.get("jednostkiOpady");
        jednostkiTemperatura=(String) ustawienia.get("jednostkiTemperatura");
    }

}
