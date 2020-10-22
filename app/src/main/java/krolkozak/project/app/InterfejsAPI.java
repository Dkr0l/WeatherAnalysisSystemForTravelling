package krolkozak.project.app;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InterfejsAPI {

    // -------------- WYKONANIE ZAPYTANIA API --------------
    // metoda wykonująca zapytanie API i zwracająca jego wynik
    public static StringBuffer pobierzOdpowiedzAPI(String url) throws IOException {
        // utworzenie adresu URL, ustawienie połączenia i metody żądania na GET (pobranie)
        URL adresDoWyslania = new URL(url);
        String linijka;
        HttpURLConnection polaczenie = (HttpURLConnection) adresDoWyslania.openConnection();
        polaczenie.setRequestMethod("GET");

        int kodOdpowiedzi = polaczenie.getResponseCode();
        if (kodOdpowiedzi == HttpURLConnection.HTTP_OK) {
            // jeśli zapytanie się udało - przypisz wynik zapytania do ciągu buforu
            BufferedReader bufor = new BufferedReader(new InputStreamReader(polaczenie.getInputStream()));
            StringBuffer odpowiedz = new StringBuffer();

            // odczytywanie buforu linijka po linijce
            while ((linijka = bufor.readLine()) != null) {
                odpowiedz.append(linijka);
            }
            bufor.close();

            // zwrócenie buforu
            return odpowiedz;

        } else {
            // jeśli zapytanie się nie udało - zwróć null (nic)
            // pomocnicza nazwa aplikacji do debuggowania
            String nazwaApki = "TRAVEL_APP";
            Log.i(nazwaApki, "Cos poszlo nie tak...");
            return null;
        }
    }

}
