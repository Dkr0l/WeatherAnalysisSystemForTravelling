package krolkozak.project.app.tworzenietrasy;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import krolkozak.project.app.InterfejsAPI;

public class Pogoda {
    // czas do przyjazdu do celu podróży
    protected int czasDoPrzyjazdu;
    // pomocnicza nazwa aplikacji do debuggowania
    private final String nazwaApki = "TRAVEL_APP";

    // -------------- POBRANIE POGODY W DANYM PUNKCIE --------------
    // metoda pobierająca informacje o pogodzie dla danego punktu geograficznego w określonym czasie
    public List pobierzPogode(double szerGeog, double dlugGeog, String dataISO) {
        StringBuffer pogodaOdpowiedzAPI = new StringBuffer();

        // -------------- JESLI CZAS PODROZY JEST MNIEJSZY NIZ 2H POGODA BEDZIE POKAZYWANA CO 20 MINUT OKOLO --------------
        if (czasDoPrzyjazdu < 7200) { // 21600
            try {
                // URL rozdzielony na dwie zmienne dla wygody programisty, która składa się z podstawowej domeny zapytań API,
                // koordynatów, systemu jednostek SI, odstepu czasu 20 minut oraz czasu startowego,
                // parametrów pogodowych, które chcemy uzyskać (opady i temperatura) oraz klucza API
                String pogodaURL_1 = "https://api.climacell.co/v3/weather/nowcast?lat=" + szerGeog + "&lon=" + dlugGeog + "&unit_system=si&timestep=20&start_time=" + dataISO;
                String pogodaURL_2 = "&end_time=" + dataISO + "&fields=precipitation%2Ctemp%2Cwind_gust%2Cweather_code&apikey=IkbL8JOHgt5iGVzsjCWtAMcwgUs4KGoM";
                Log.i(nazwaApki, "climacell url: " + pogodaURL_1 + pogodaURL_2);

                // przypisanie wyniku zapytania do zmiennej
                pogodaOdpowiedzAPI = InterfejsAPI.pobierzOdpowiedzAPI(pogodaURL_1 + pogodaURL_2);
            } catch (IOException e) {
                Log.i(nazwaApki, "Blad pogody: " + e.getMessage());
            }

            // -------------- JESLI CZAS PODROZY JEST MNIEJSZY NIZ 96H POGODA BEDZIE POKAZYWANA CO GODZINE OKOLO --------------
        } else if (czasDoPrzyjazdu < 345600) {
            try {
                // URL rozdzielony na dwie zmienne dla wygody programisty, która składa się z podstawowej domeny zapytań API,
                // koordynatów, systemu jednostek SI, odstepu czasu 1 godziny oraz czasu startowego,
                // parametrów pogodowych, które chcemy uzyskać (opady i temperatura) oraz klucza API
                String pogodaURL_1 = "https://api.climacell.co/v3/weather/forecast/hourly?lat=" + szerGeog + "&lon=" + dlugGeog + "&unit_system=si&start_time=" + dataISO;
                String pogodaURL_2 = "&end_time=" + dataISO + "&fields=precipitation%2Ctemp%2Cwind_gust%2Cweather_code&apikey=IkbL8JOHgt5iGVzsjCWtAMcwgUs4KGoM";
                Log.i(nazwaApki, "climacell url: " + pogodaURL_1 + pogodaURL_2);

                // przypisanie wyniku zapytania do zmiennej
                pogodaOdpowiedzAPI = InterfejsAPI.pobierzOdpowiedzAPI(pogodaURL_1 + pogodaURL_2);
            } catch (IOException e) {
                // jeśli nie uda się wykonać zapytania - aplikacja wyrzuci wyjatek
                Log.i(nazwaApki, "Blad pogody: " + e.getMessage());
            }
        }

        // zwrócenie metody, która sformatuje wynik zapytania w listę, którą będzie łatwo wyświetlić
        return danePogodowe(pogodaOdpowiedzAPI);
    }

    // -------------- PRZETWORZENIE DANYCH O POGODZIE --------------
    // metoda, która formatuje wynik zapytania w listę, którą będzie łatwo wyświetlić
    public List danePogodowe(StringBuffer odpowiedz) {
        String opady;
        String temperatura;
        String wiatr;
        String kodPogodowy;

        // -------------- POBRANIE PARAMETRÓW POGODY --------------
        try {
            JSONObject odpowiedzJSON = new JSONArray(String.valueOf(odpowiedz)).getJSONObject(0);

            // pobranie wartości temperatury
            JSONObject temperaturaJSON = odpowiedzJSON.getJSONObject("temp");
            temperatura = String.valueOf(new BigDecimal(temperaturaJSON.getString("value")).setScale(2, RoundingMode.HALF_UP).doubleValue());

            // pobranie wartości opadów
            JSONObject opadyJSON = odpowiedzJSON.getJSONObject("precipitation");
            opady = String.valueOf(new BigDecimal(opadyJSON.getString("value")).setScale(2, RoundingMode.HALF_UP).doubleValue());

            //pobranie wartości wiatru
            JSONObject wiatrJSON = odpowiedzJSON.getJSONObject("wind_gust");
            wiatr = String.valueOf(new BigDecimal(wiatrJSON.getString("value")).setScale(2, RoundingMode.HALF_UP).doubleValue());

            //pobranie kodu pogodowego
            JSONObject kodJSON=odpowiedzJSON.getJSONObject("weather_code");
            kodPogodowy=kodJSON.getString("value");
        } catch (JSONException | NumberFormatException e) {
            // jeśli nie uda się pobrać parametrów pogody - aplikacja wyrzuci wyjątek
            Log.i(nazwaApki, "Bląd danych pogodowych: " + e.getMessage());
            return null;
        }

        // dodanie pobranych parametrów do listy i zwrócenie jej
        List dane = new LinkedList();
        dane.add(temperatura);
        dane.add(opady);
        dane.add(wiatr);
        dane.add(kodPogodowy);
        return dane;
    }

    // -------------- CZAS PO KTORYM POBIERANA JEST POGODA --------------
    // metoda, która wyznacza długość odstępu pomiędzy kolejnymi punktami na trasie
    public int wyznaczDlugoscOdstepu() {
        // jesli czas do przyjazdu jest mniejszy niż 2 godziny, pobieranie pogody będzie możliwe mniejszych odstępach
        // jesli czas do przyjazdu jest mniejszy niż 96 godziny, pobieranie pogody będzie możliwe co godzinę
        // jest to ograniczenie nałożone przez API pogody (climacell.co)
        if (czasDoPrzyjazdu <= 21600) {
            return 1200;
        }
        return 3600;
    }

}
