package krolkozak.project.app.pomocnicze;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import krolkozak.project.app.Ustawienia;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PobieranieDanychPogody {

    // -------------- PRZETWORZENIE DANYCH O POGODZIE --------------
    // metoda, która formatuje wynik zapytania w listę, którą będzie łatwo wyświetlić
    public static List danePogodowe(StringBuffer odpowiedz, String typPrognozy) {
        String opady = null;
        String opadySzansa = null;
        String temperatura = null;
        String temperaturaOdczuwalna = null;
        String wiatrSr = null;
        String kodPogodowy;
        String cisnienie = null;
        String kierunekWiatru = null;
        String wilgotnosc = null;
        String wiatrWPorywach = null;
        String zachmurzenie = null;

        Object temperaturaObjekt;
        Object temperaturaOdczuwlanaObjekt;
        Object cisnienieObjekt;
        Object opadyObjekt;
        Object opadySzansaObjekt;
        Object kierunekWiatruObjekt;
        Object wiatrWPorywachObiekt;
        Object wiatrSredniObjekt;
        Object wilgotnoscObjekt;
        Object zachmurzenieObjekt;

        List dane = new LinkedList();
        // -------------- POBRANIE PARAMETRÓW POGODY --------------
        try {
            JSONObject odpowiedzJSON = new JSONArray(String.valueOf(odpowiedz)).getJSONObject(0);

            if (Ustawienia.wyswietlicTemp()) {
                // pobranie wartości temperatury
                if (odpowiedzJSON.getJSONObject("temp").isNull("value")) temperaturaObjekt = "0";
                else temperaturaObjekt = odpowiedzJSON.getJSONObject("temp").get("value");
                temperatura = String.valueOf(new BigDecimal(temperaturaObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());

                double wartoscTemperatury = Double.parseDouble(temperatura);
                if (Ustawienia.jednostkaTemperatury().equals("F"))
                    wartoscTemperatury = wartoscTemperatury * 1.8 + 32;
                temperatura = "Temperatura: " + wartoscTemperatury + Ustawienia.jednostkaTemperatury();
            }
            if (Ustawienia.wyswietlicTempOdczuwalna()) {
                // pobranie wartości temperatury odczuwalnej
                if (odpowiedzJSON.getJSONObject("feels_like").isNull("value"))
                    temperaturaOdczuwlanaObjekt = "0";
                else
                    temperaturaOdczuwlanaObjekt = odpowiedzJSON.getJSONObject("feels_like").get("value");
                temperaturaOdczuwalna = String.valueOf(new BigDecimal(temperaturaOdczuwlanaObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());

                double wartoscTemperatury = Double.parseDouble(temperaturaOdczuwalna);
                if (Ustawienia.jednostkaTemperatury().equals("F"))
                    wartoscTemperatury = wartoscTemperatury * 1.8 + 32;
                temperaturaOdczuwalna = "Temperatura odczuwalna: " + wartoscTemperatury + Ustawienia.jednostkaTemperatury();
            }
            if (Ustawienia.wyswietlicCisnienie()) {
                // pobranie wartości ciśnienia atmosferycznego
                if (odpowiedzJSON.getJSONObject("baro_pressure").isNull("value"))
                    cisnienieObjekt = "0";
                else cisnienieObjekt = odpowiedzJSON.getJSONObject("baro_pressure").get("value");
                cisnienie = String.valueOf(new BigDecimal(cisnienieObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());

                double wartoscCisnienia = Double.parseDouble(cisnienie);
                if (Ustawienia.jednostkaCisnienia().equals("Atm")) {
                    wartoscCisnienia /= 1013.2501;
                } else if (Ustawienia.jednostkaCisnienia().equals("bar")) {
                    wartoscCisnienia /= 1000;
                } else if (Ustawienia.jednostkaCisnienia().equals("psi")) {
                    wartoscCisnienia /= 68.9475729;
                }
                cisnienie = "Ciśnienie atmosferyczne: " + wartoscCisnienia + Ustawienia.jednostkaCisnienia();
            }
            if (Ustawienia.wyswietlicOpadyIntensywnosc() || Ustawienia.wyswietlicOpadySzansa()) {
                // pobranie wartości opadów
                if (odpowiedzJSON.getJSONObject("precipitation").isNull("value")) opadyObjekt = "0";
                else opadyObjekt = odpowiedzJSON.getJSONObject("precipitation").get("value");
                opady = "Opady: " + new BigDecimal(opadyObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue() + Ustawienia.jednostkaOpadow();
            }
            if (Ustawienia.wyswietlicOpadySzansa() && !typPrognozy.contentEquals("nowcast")) {
                // pobranie szansy opadów
                if (odpowiedzJSON.getJSONObject("precipitation_probability").isNull("value"))
                    opadySzansaObjekt = "0";
                else
                    opadySzansaObjekt = odpowiedzJSON.getJSONObject("precipitation_probability").get("value");
                opadySzansa = "Szansa opadów: " + new BigDecimal(opadySzansaObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue() + "%";
            }
            if (Ustawienia.wyswietlicWiatrKierunek()) {
                // pobranie kierunku wiatru
                if (odpowiedzJSON.getJSONObject("wind_direction").isNull("value"))
                    kierunekWiatruObjekt = "0";
                else
                    kierunekWiatruObjekt = odpowiedzJSON.getJSONObject("wind_direction").get("value");
                kierunekWiatru = String.valueOf(new BigDecimal(kierunekWiatruObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());
                double kierunekWiatruWartosc = Double.parseDouble(kierunekWiatru);
                if (338 <= kierunekWiatruWartosc || kierunekWiatruWartosc < 23)
                    kierunekWiatru = "Wiatr północny";
                if (23 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 68)
                    kierunekWiatru = "Wiatr północno-wschodni";
                if (68 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 113)
                    kierunekWiatru = "Wiatr wschodni";
                if (113 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 158)
                    kierunekWiatru = "Wiatr południowo-wschodni";
                if (158 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 203)
                    kierunekWiatru = "Wiatr południowy";
                if (203 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 248)
                    kierunekWiatru = "Wiatr południowo-zachodni";
                if (248 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 293)
                    kierunekWiatru = "Wiatr zachodni";
                if (293 <= kierunekWiatruWartosc && kierunekWiatruWartosc < 338)
                    kierunekWiatru = "Wiatr północno-zachodni";
            }
            if (Ustawienia.wyswietlicWiatrSr()) {
                // pobranie prędkości wiatru
                if (odpowiedzJSON.getJSONObject("wind_speed").isNull("value"))
                    wiatrSredniObjekt = "0";
                else wiatrSredniObjekt = odpowiedzJSON.getJSONObject("wind_speed").get("value");
                wiatrSr = String.valueOf(new BigDecimal(wiatrSredniObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());

                double wartoscWiatru = Double.parseDouble(wiatrSr);
                if (Ustawienia.jednostkaWiatru().equals("km/h")) wartoscWiatru *= 3.6;
                else if (Ustawienia.jednostkaWiatru().equals("mph")) wartoscWiatru *= 2.23693629;
                wiatrSr = "Średnia prędkość wiatru: " + BigDecimal.valueOf(wartoscWiatru).setScale(1, RoundingMode.HALF_UP).doubleValue() + Ustawienia.jednostkaWiatru();
            }
            if (Ustawienia.wyswietlicWilgotnosc()) {
                // pobranie wilgotności
                if (odpowiedzJSON.getJSONObject("humidity").isNull("value")) wilgotnoscObjekt = "0";
                else wilgotnoscObjekt = odpowiedzJSON.getJSONObject("humidity").get("value");
                wilgotnosc = "Wilgotność: " + new BigDecimal(wilgotnoscObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue() + "%";
            }
            if (!typPrognozy.contentEquals("daily")) {
                if (Ustawienia.wyswietlicWiatrWPorywach()) {
                    //pobranie wartości wiatru
                    if (odpowiedzJSON.getJSONObject("wind_gust").isNull("value"))
                        wiatrWPorywachObiekt = "0";
                    else
                        wiatrWPorywachObiekt = odpowiedzJSON.getJSONObject("wind_gust").get("value");
                    wiatrWPorywach = String.valueOf(new BigDecimal(wiatrWPorywachObiekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue());

                    double wartoscWiatru = Double.parseDouble(wiatrWPorywach);
                    if (Ustawienia.jednostkaWiatru().equals("km/h")) wartoscWiatru *= 3.6;
                    else if (Ustawienia.jednostkaWiatru().equals("mph"))
                        wartoscWiatru *= 2.23693629;
                    wiatrWPorywach = "Porywy wiatru: " + wartoscWiatru + Ustawienia.jednostkaWiatru();
                }
                if (Ustawienia.wyswietlicZachmurzenie()) {
                    //pobranie wartości zachmurzenia
                    if (odpowiedzJSON.getJSONObject("cloud_cover").isNull("value"))
                        zachmurzenieObjekt = "0";
                    else
                        zachmurzenieObjekt = odpowiedzJSON.getJSONObject("cloud_cover").get("value");
                    zachmurzenie = "Zachmurzenie: " + new BigDecimal(zachmurzenieObjekt.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue() + "%";
                }
            }

            //pobranie kodu pogodowego
            JSONObject kodJSON = odpowiedzJSON.getJSONObject("weather_code");
            kodPogodowy = kodJSON.getString("value");
            dane.add(kodPogodowy);
            dane.add(opady);
            dane.add(opadySzansa);
            dane.add(temperatura);
            dane.add(temperaturaOdczuwalna);
            dane.add(wiatrSr);
            dane.add(cisnienie);
            dane.add(kierunekWiatru);
            dane.add(wilgotnosc);
            dane.add(wiatrWPorywach);
            dane.add(zachmurzenie);
        } catch (JSONException | NumberFormatException e) {
            // jeśli nie uda się pobrać parametrów pogody - aplikacja wyrzuci wyjątek
            Log.i(nazwaApki, "Bląd danych pogodowych: " + e.getMessage());
            return null;
        }

        return dane;
    }

}
