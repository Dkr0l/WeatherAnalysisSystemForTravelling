package krolkozak.project.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Map;

import krolkozak.project.app.ekrany.Menu;

public class Ustawienia extends Activity {

    //elementy interfejsu
    private Switch przelacznikTrybuCiemnego;
    private Spinner wyborJednostkiOpady;
    private Spinner wyborJednostkiWiatr;
    private Spinner wyborJednostkiTemperatura;
    private Spinner wyborJednostkiCisnienia;
    private Switch przelacznikTemp;
    private Switch przelacznikTempOdczuwalna;
    private Switch przelacznikOpadyIntensywnosc;
    private Switch przelacznikOpadySzansa;
    private Switch przelacznikWilgotnosc;
    private Switch przelacznikWiatrSr;
    private Switch przelacznikWiatrWPorywach;
    private Switch przelacznikWiatrKierunek;
    private Switch przelacznikCisnienie;
    private Switch przelacznikZachmurzenie;

    //domyślne ustawienia
    private static boolean trybCiemnyAktywny=false;
    private static String jednostkiOpady="mm/h";
    private static String jednostkiWiatr="km/h";
    private static String jednostkiTemperatura="c";
    private static String jednostkiCisnienie="hPa";
    private static boolean temp=false;
    private static boolean tempOdczuwalna=true;
    private static boolean opadyIntensywnosc=false;
    private static boolean opadySzansa=true;
    private static boolean wilgotnosc=false;
    private static boolean wiatrSr=false;
    private static boolean wiatrWPorywach=true;
    private static boolean wiatrKierunek=false;
    private static boolean cisnienie=false;
    private static boolean zachmurzenie=false;

    public static boolean trybCiemnyAktywny() {
        return trybCiemnyAktywny;
    }
    public static String jednostkaOpadow(){return jednostkiOpady;}
    public static String jednostkaWiatru(){return jednostkiWiatr;}
    public static String jednostkaTemperatury(){return jednostkiTemperatura;}
    public static String jednostkaCisnienia(){return jednostkiCisnienie;}
    public static boolean wyswietlicTemp(){return temp;}
    public static boolean wyswietlicTempOdczuwalna(){return tempOdczuwalna;}
    public static boolean wyswietlicOpadyIntensywnosc(){return opadyIntensywnosc;}
    public static boolean wyswietlicOpadySzansa(){return opadySzansa;}
    public static boolean wyswietlicWilgotnosc(){return wilgotnosc;}
    public static boolean wyswietlicWiatrSr(){return wiatrSr;}
    public static boolean wyswietlicWiatrWPorywach(){return wiatrWPorywach;}
    public static boolean wyswietlicWiatrKierunek(){return wiatrKierunek;}
    public static boolean wyswietlicCisnienie(){return cisnienie;}
    public static boolean wyswietlicZachmurzenie(){return zachmurzenie;}

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
        wyborJednostkiCisnienia=findViewById(R.id.ustawieniaJednostkaCisnieniaAtm);
        przelacznikTemp=findViewById(R.id.przelacznikTemperatura);
        przelacznikTempOdczuwalna=findViewById(R.id.przelacznikTemperaturaOdczuwalna);
        przelacznikOpadyIntensywnosc=findViewById(R.id.przelacznikIntensywnoscOpadow);
        przelacznikOpadySzansa=findViewById(R.id.przelacznikPrawdopodobienstwoOpadow);
        przelacznikWilgotnosc=findViewById(R.id.przelacznikWilgotnosc);
        przelacznikWiatrSr=findViewById(R.id.przelacznikPredkascWiatruSr);
        przelacznikWiatrWPorywach=findViewById(R.id.przelacznikPredkascWiatruPor);
        przelacznikWiatrKierunek=findViewById(R.id.przelacznikKierunekWiatru);
        przelacznikCisnienie=findViewById(R.id.przelacznikCisnienieAtm);
        przelacznikZachmurzenie=findViewById(R.id.przelacznikZachmurzenie);

        //wyświetlenie zapisanych ustawień w interfejsie
        przywrocenieUstawien();

        // zatwierdzenie ustawień
        findViewById(R.id.zatwierdzUstawienia).setOnClickListener(v -> {
            //z interfejsu to zmiennych tymczasowych
            trybCiemnyAktywny=przelacznikTrybuCiemnego.isChecked();
            jednostkiOpady=wyborJednostkiOpady.getSelectedItem().toString();
            jednostkiWiatr=wyborJednostkiWiatr.getSelectedItem().toString();
            jednostkiTemperatura=wyborJednostkiTemperatura.getSelectedItem().toString();
            temp=przelacznikTemp.isChecked();
            tempOdczuwalna=przelacznikTempOdczuwalna.isChecked();
            opadyIntensywnosc=przelacznikOpadyIntensywnosc.isChecked();
            opadySzansa=przelacznikOpadySzansa.isChecked();
            wilgotnosc=przelacznikWilgotnosc.isChecked();
            wiatrSr=przelacznikWiatrSr.isChecked();
            wiatrWPorywach=przelacznikWiatrWPorywach.isChecked();
            wiatrKierunek=przelacznikWiatrKierunek.isChecked();
            cisnienie=przelacznikCisnienie.isChecked();
            zachmurzenie=przelacznikZachmurzenie.isChecked();

            //ze zmiennych tymczasowych do pamięci urządzenia
            SharedPreferences ustawienia=getSharedPreferences("ApkaPogodowa", MODE_PRIVATE);
            SharedPreferences.Editor edytorUstawien=ustawienia.edit();

            edytorUstawien.putBoolean("trybCiemny", trybCiemnyAktywny);
            edytorUstawien.putString("jednostkiWiatr", jednostkiWiatr);
            edytorUstawien.putString("jednostkiOpady", jednostkiOpady);
            edytorUstawien.putString("jednostkiTemperatura", jednostkiTemperatura);
            edytorUstawien.putBoolean("temp",temp);
            edytorUstawien.putBoolean("tempOdczuwalna",tempOdczuwalna);
            edytorUstawien.putBoolean("opadyIntensywnosc",opadyIntensywnosc);
            edytorUstawien.putBoolean("opadySzansa",opadySzansa);
            edytorUstawien.putBoolean("wilgotnosc",wilgotnosc);
            edytorUstawien.putBoolean("wiatrSr",wiatrSr);
            edytorUstawien.putBoolean("wiatrWPorywach",wiatrWPorywach);
            edytorUstawien.putBoolean("wiatrKierunek",wiatrKierunek);
            edytorUstawien.putBoolean("cisnienie",cisnienie);
            edytorUstawien.putBoolean("zachmurzenie",zachmurzenie);

            edytorUstawien.apply();

            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        });
    }

    private void przywrocenieUstawien(){
        przelacznikTrybuCiemnego.setChecked(trybCiemnyAktywny);

        if(jednostkiOpady!= null) {
            if (jednostkiOpady.contentEquals("l/m2h"))
                wyborJednostkiOpady.setSelection(0);
            else if (jednostkiOpady.contentEquals("mm/h"))
                wyborJednostkiOpady.setSelection(1);
        }

        if(jednostkiWiatr!=null) {
            if (jednostkiWiatr.contentEquals("km/h"))
                wyborJednostkiWiatr.setSelection(0);
            else if (jednostkiWiatr.contentEquals("mph"))
                wyborJednostkiWiatr.setSelection(1);
            else if (jednostkiWiatr.contentEquals("m/s"))
                wyborJednostkiWiatr.setSelection(2);
        }

        if(jednostkiTemperatura!=null) {
            if (jednostkiTemperatura.contentEquals("C"))
                wyborJednostkiTemperatura.setSelection(0);
            else if (jednostkiTemperatura.contentEquals("F"))
                wyborJednostkiTemperatura.setSelection(1);
        }

        if(jednostkiCisnienie!=null) {
            if (jednostkiCisnienie.contentEquals("hPa"))
                wyborJednostkiCisnienia.setSelection(0);
            else if (jednostkiCisnienie.contentEquals("Atm"))
                wyborJednostkiCisnienia.setSelection(1);
            else if (jednostkiCisnienie.contentEquals("bar"))
                wyborJednostkiCisnienia.setSelection(2);
            else if (jednostkiCisnienie.contentEquals("psi"))
                wyborJednostkiCisnienia.setSelection(3);
        }

        przelacznikTemp.setChecked(temp);
        przelacznikTempOdczuwalna.setChecked(tempOdczuwalna);
        przelacznikOpadyIntensywnosc.setChecked(opadyIntensywnosc);
        przelacznikOpadySzansa.setChecked(opadySzansa);
        przelacznikWilgotnosc.setChecked(wilgotnosc);
        przelacznikWiatrSr.setChecked(wiatrSr);
        przelacznikWiatrWPorywach.setChecked(wiatrWPorywach);
        przelacznikWiatrKierunek.setChecked(wiatrKierunek);
        przelacznikCisnienie.setChecked(cisnienie);
        przelacznikZachmurzenie.setChecked(zachmurzenie);
    }

    public static void wczytajZPamieci(Map<String, ?> ustawienia){
        trybCiemnyAktywny=((Map<String,Boolean>) ustawienia).getOrDefault("trybCiemny",false);
        jednostkiWiatr=((Map<String,String>) ustawienia).getOrDefault("jednostkiWiatr","km/h");
        jednostkiOpady=((Map<String,String>) ustawienia).getOrDefault("jednostkiOpady","mm/h");
        jednostkiTemperatura=((Map<String,String>) ustawienia).getOrDefault("jednostkiTemperatura","C");
        jednostkiCisnienie=((Map<String,String>) ustawienia).getOrDefault("jednostkiCisnienie","hPa");
        temp=((Map<String,Boolean>) ustawienia).getOrDefault("temp",false);
        tempOdczuwalna=((Map<String,Boolean>) ustawienia).getOrDefault("tempOdczuwalna",true);
        opadyIntensywnosc=((Map<String,Boolean>) ustawienia).getOrDefault("opadyIntensywnosc",false);
        opadySzansa=((Map<String,Boolean>) ustawienia).getOrDefault("opadySzansa",true);
        wilgotnosc=((Map<String,Boolean>) ustawienia).getOrDefault("wilgotnosc",false);
        wiatrSr=((Map<String,Boolean>) ustawienia).getOrDefault("wiatrSr",false);
        wiatrWPorywach=((Map<String,Boolean>) ustawienia).getOrDefault("wiatrWPorywach",true);
        wiatrKierunek=((Map<String,Boolean>) ustawienia).getOrDefault("wiatrKierunek",false);
        cisnienie=((Map<String,Boolean>) ustawienia).getOrDefault("cisnienie",false);
        zachmurzenie=((Map<String,Boolean>) ustawienia).getOrDefault("zachmurzenie",false);
    }

}
