package krolkozak.project.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

public class Autouzupelnianie {
    // koordynaty geograficzne pomocnicze
    private double pomocSzerGeog1 = 200.0, pomocDlugGeog1 = 200.0;
    private double pomocSzerGeog2 = 200.0, pomocDlugGeog2 = 200.0;
    // kontekst aplikacji
    private Context kontekst;
    // pomocnicza nazwa aplikacji do debuggowania
    private final String nazwaApki = "TRAVEL_APP";
    // zmienna logiczna sprawdzająca czy pobrano lokalizację z GPS
    private boolean pobranoZgps = false;
    // maksymalna ilość podpowiedzi w polach z autouzupełnianiem
    private final int iloscPodpowiedzi = 5;
    // podpowiedzi do pól z autouzupełnianiem w formacie objektu JSON
    JSONObject poczatekPodpowiedziJSON;
    JSONObject koniecPodpowiedziJSON;
    // podpowiedzi do pól z autouzupełnianiem w formacie tablicy JSON
    JSONArray miejsca;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Autouzupelnianie(Context kontekst, Trasa trasa) {
        this.kontekst = kontekst;

        // wywołanie metody która pobierze lokalizację GPS i uzupełni pierwsze pole tekstowe
        this.pobierzLokalizacjeGPS();

        // wyłączenie przycisku wyszukiwania trasy
        wylaczPrzycisk();

        // dodanie nasłuchiwacza zmiany tekstu to pierwszego pola tekstowego (początek trasy)
        TworzenieTrasy.poczatekAutouzupelnianie.addTextChangedListener(new TextWatcher() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            // metoda wywoływana po zmianie tekstu w polu
            public void afterTextChanged(Editable s) {
                Log.i(nazwaApki, "Zmodyfikowano tekst wyszukiwarki!");

                if (pobranoZgps) {
                    pobranoZgps = false;
                    return;
                }

                pomocSzerGeog1 = 200.0;
                pomocDlugGeog1 = 200.0;
                wylaczPrzycisk();

                // tablica, która będzie przechowywała aktualną listę podpowiedzi
                String[] podpowiedzi;

                // jeśli tekst w pierwszym polu tekstowym ma 2 znaki lub więcej - wykonaj instrukcje, jeśli nie - nie rób nic
                if (s.length() >= 2) {
                    // zmienna klasy synchronizowanej ciagów buforu
                    StringBuffer podpowiedziOdpowiedzAPI = new StringBuffer();

                    // -------------- ZAPYTANIE O PODPOWIEDZI (geonames.org) --------------
                    try {
                        // złączenie adresu url w jedną zmienną, która zawiera podstawową domenę zapytań API, tekst z pierwszego pola tekstowego,
                        // maksymalną ilość podpowiedzi, nazwę użytkownika podaną przy rejestracji oraz język, w którym chcemy otrzymać podpowiedzi
                        String podpowiedziURL = "http://api.geonames.org/searchJSON?q=" + (String) URLEncoder.encode(s.toString(), "UTF-8") + "&maxRows=" + iloscPodpowiedzi + "&username=Dakr0&lang=pl";
                        Log.i(nazwaApki, "Podpowiedzi URL: " + podpowiedziURL);
                        podpowiedziOdpowiedzAPI = InterfejsAPI.pobierzOdpowiedzAPI(podpowiedziURL);
                    } catch (IOException e) {
                        Log.i(nazwaApki, "Blad podpowiedzi: " + e.getMessage());
                    }

                    try {
                        // zmienne do przechowywania otrzymanych z zapytania podpowiedzi
                        JSONObject podpowiedziJSON[] = new JSONObject[iloscPodpowiedzi];
                        poczatekPodpowiedziJSON = new JSONObject(String.valueOf(podpowiedziOdpowiedzAPI));
                        podpowiedzi = new String[iloscPodpowiedzi];

                        // jeśli otrzymano podpowiedzi - pobierz ze wszystkich nazwę miejscowości, panśtwa i zapisz do tablicy
                        miejsca = poczatekPodpowiedziJSON.optJSONArray("geonames");
                        if (miejsca != null) {
                            for (int i = 0; i < iloscPodpowiedzi; i++) {
                                podpowiedziJSON[i] = miejsca.optJSONObject(i);

                                if (podpowiedziJSON[i] != null) {
                                    String podpowiedz = podpowiedziJSON[i].getString("name") + ", " + podpowiedziJSON[i].getString("adminName1") + ", " + podpowiedziJSON[i].getString("countryName");
                                    podpowiedzi[i] = podpowiedz;
                                    Log.i(nazwaApki, podpowiedz);
                                } else {
                                    podpowiedzi[i] = "";
                                }
                            }
                        }

                        // przefiltrowanie tablicy podpowiedzi w celu usunięcia pustych wartości
                        podpowiedzi = Arrays.stream(podpowiedzi).filter(wartosc -> !wartosc.equals("")).toArray(rozmiar -> new String[rozmiar]);
                        Log.i(nazwaApki, "HINTS: " + Arrays.toString(podpowiedzi));

                        // utworzenie listy podpowiedzi do wyświetlenia i przypisanie do pierwszego pola tektsowego
                        ArrayAdapter adapterListy = new ArrayAdapter<String>(kontekst, android.R.layout.simple_dropdown_item_1line, podpowiedzi);
                        TworzenieTrasy.poczatekAutouzupelnianie.setAdapter(adapterListy);
                        adapterListy.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.i(nazwaApki, "Blad podpowiedzi: " + e.getMessage());
                    }
                }
            }

            // inne metody nasłuchiwacza zmiany tekstu
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        // dodanie nasłuchiwacza kliknięcia w podpowiedź w pierwszym polu tekstowym
        TworzenieTrasy.poczatekAutouzupelnianie.setOnItemClickListener((parent, arg1, pos, id) -> {
            JSONObject podpowiedziJSON;

            try {
                // pobierz podpowiedź o indeksie zgodnym z pozycją klikniętą na liście podpowiedzi
                podpowiedziJSON = poczatekPodpowiedziJSON.getJSONArray("geonames").getJSONObject(pos);
                pomocSzerGeog1 = Double.parseDouble(podpowiedziJSON.getString("lat"));
                pomocDlugGeog1 = Double.parseDouble(podpowiedziJSON.getString("lng"));

                Log.i(nazwaApki, String.valueOf(pomocSzerGeog1));
                Log.i(nazwaApki, String.valueOf(pomocDlugGeog1));

                //ukryj klawiaturę
                TworzenieTrasy.inputMethodManager.hideSoftInputFromWindow(parent.getApplicationWindowToken(), 0);

                wlaczPrzycisk();
            } catch (JSONException e) {
                // jeśli nie udało się pobrać podpowiedzi aplikacji wyrzuci wyjątek
                e.printStackTrace();
            }
        });

        // dodanie nasłuchiwacza kliknięcia w przycisk "WYCZYŚĆ" - wyczyszczenie pierwszego pola tekstowego
        TworzenieTrasy.wyczyscPoczatekPrzycisk.setOnClickListener(v -> {
            Log.i(nazwaApki, "Wyczyszczono pierwsze pole tekstowe!");

            // ustawia zawartość pierwszego pola tekstowego na pustą
            TworzenieTrasy.poczatekAutouzupelnianie.setText("");
        });

        // dodanie nasłuchiwacza zmiany tekstu to drugiego pola tekstowego (koniec trasy)
        TworzenieTrasy.koniecAutouzupelnianie.addTextChangedListener(new TextWatcher() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            // metoda wywoływana po zmianie tekstu w polu
            public void afterTextChanged(Editable s) {
                Log.i(nazwaApki, "Zmodyfikowano tekst wyszukiwarki!");
                pomocSzerGeog2 = 200.0;
                pomocDlugGeog2 = 200.0;
                wylaczPrzycisk();

                // tablica, która będzie przechowywała aktualną listę podpowiedzi
                String[] podpowiedzi;

                // jeśli tekst w drugim polu tekstowym ma 2 znaki lub więcej - wykonaj instrukcje, jeśli nie - nie rób nic
                if (s.length() >= 2) {
                    // zmienna klasy synchronizowanej ciagów buforu
                    StringBuffer podpowiedziOdpowiedzAPI = new StringBuffer();

                    // -------------- ZAPYTANIE O PODPOWIEDZI (geonames.org) --------------
                    try {
                        // złączenie adresu url w jedną zmienną, która zawiera podstawową domenę zapytań API, tekst z drugiego pola tekstowego,
                        // maksymalną ilość podpowiedzi, nazwę użytkownika podaną przy rejestracji oraz język, w którym chcemy otrzymać podpowiedzi
                        String podpowiedziURL = "http://api.geonames.org/searchJSON?q=" + (String) URLEncoder.encode(s.toString(), "UTF-8") + "&maxRows=" + iloscPodpowiedzi + "&username=Dakr0&lang=pl";
                        Log.i(nazwaApki, "Podpowiedzi URL: " + podpowiedziURL);
                        podpowiedziOdpowiedzAPI = InterfejsAPI.pobierzOdpowiedzAPI(podpowiedziURL);
                    } catch (IOException e) {
                        Log.i(nazwaApki, "Blad podpowiedzi: " + e.getMessage());
                    }

                    try {
                        // zmienne do przechowywania otrzymanych z zapytania podpowiedzi
                        JSONObject podpowiedziJSON[] = new JSONObject[iloscPodpowiedzi];
                        koniecPodpowiedziJSON = new JSONObject(String.valueOf(podpowiedziOdpowiedzAPI));
                        podpowiedzi = new String[iloscPodpowiedzi];

                        // jeśli otrzymano podpowiedzi - pobierz ze wszystkich nazwę miejscowości, panśtwa i zapisz do tablicy
                        miejsca = koniecPodpowiedziJSON.optJSONArray("geonames");
                        if (miejsca != null) {
                            for (int i = 0; i < iloscPodpowiedzi; i++) {
                                podpowiedziJSON[i] = miejsca.optJSONObject(i);

                                if (podpowiedziJSON[i] != null) {
                                    String podpowiedz = podpowiedziJSON[i].getString("name") + ", " + podpowiedziJSON[i].getString("adminName1") + ", " + podpowiedziJSON[i].getString("countryName");
                                    podpowiedzi[i] = podpowiedz;
                                    Log.i(nazwaApki, podpowiedz);
                                } else {
                                    podpowiedzi[i] = "";
                                }
                            }
                        }

                        // przefiltrowanie tablicy podpowiedzi w celu usunięcia pustych wartości
                        podpowiedzi = Arrays.stream(podpowiedzi).filter(wartosc -> !wartosc.equals("")).toArray(rozmiar -> new String[rozmiar]);
                        Log.i(nazwaApki, "HINTS: " + Arrays.toString(podpowiedzi));

                        // utworzenie listy podpowiedzi do wyświetlenia i przypisanie do drugiego pola tektsowego
                        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(kontekst, android.R.layout.simple_dropdown_item_1line, podpowiedzi);
                        TworzenieTrasy.koniecAutouzupelnianie.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                        TworzenieTrasy.koniecAutouzupelnianie.showDropDown();
                    } catch (JSONException e) {
                        Log.i(nazwaApki, "Blad podpowiedzi: " + e.getMessage());
                    }
                }
            }

            // inne metody nasłuchiwacza zmiany tekstu
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        // dodanie nasłuchiwacza kliknięcia w podpowiedź w drugim polu tekstowym
        TworzenieTrasy.koniecAutouzupelnianie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                JSONObject podpowiedziJSON;

                try {
                    // pobierz podpowiedź o indeksie zgodnym z pozycją klikniętą na liście podpowiedzi
                    podpowiedziJSON = koniecPodpowiedziJSON.getJSONArray("geonames").getJSONObject(pos);
                    pomocSzerGeog2 = Double.parseDouble(podpowiedziJSON.getString("lat"));
                    pomocDlugGeog2 = Double.parseDouble(podpowiedziJSON.getString("lng"));

                    Log.i(nazwaApki, String.valueOf(pomocSzerGeog2));
                    Log.i(nazwaApki, String.valueOf(pomocDlugGeog2));

                    //ukryj klawiaturę
                    TworzenieTrasy.inputMethodManager.hideSoftInputFromWindow(parent.getApplicationWindowToken(), 0);

                    wlaczPrzycisk();
                } catch (JSONException e) {
                    // jeśli nie udało się pobrać podpowiedzi aplikacji wyrzuci wyjątek
                    e.printStackTrace();
                }
            }
        });

        // dodanie nasłuchiwacza kliknięcia w przycisk "WYCZYŚĆ" - wyczyszczenie drugiego pola tekstowego
        TworzenieTrasy.wyczyscKoniecPrzycisk.setOnClickListener(v -> {
            Log.i(nazwaApki, "Wyczyszczono drugie pole tekstowe!");

            // ustawia zawartość drugiego pola tekstowego na pustą
            TworzenieTrasy.koniecAutouzupelnianie.setText("");
        });

        // dodanie nasłuchiwacza kliknięcia w przycisk "ZNAJDŹ TRASĘ"
        GlownaAktywnosc.znajdzTrasePrzycisk.setOnClickListener(v -> {
            Log.i(nazwaApki, "Nacisnieto przycisk!");

            // przypisz pobrane szerkości i długości geograficzne z pierwszego i drugiego pola wyboru trasy
            // do zmiennych, które zostaną użyte przy tworzeniu trasy na mapie
            trasa.szerGeog1 = pomocSzerGeog1;
            trasa.dlugGeog1 = pomocDlugGeog1;
            trasa.szerGeog2 = pomocSzerGeog2;
            trasa.dlugGeog2 = pomocDlugGeog2;

            // wywołanie metody, która obliczy i wyświetli trasę wraz z punktami pogodowymi na mapie
            trasa.odswiezMape(kontekst);
        });
    }

    // -------------- WYŁĄCZENIE PRZYCISKU WYSZUKANIA TRASY --------------
    // metoda, która uniemożliwi wyszukanie trasy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void wylaczPrzycisk() {
        Log.i(nazwaApki, "WYŁĄCZONO PRZYCISK");

        TworzenieTrasy.zatwierdzTrasePrzycisk.setText("WYBIERZ PUNKTY");
        TworzenieTrasy.zatwierdzTrasePrzycisk.setEnabled(false);

        GlownaAktywnosc.znajdzTrasePrzycisk.setText("STWÓRZ TRASĘ");
        GlownaAktywnosc.znajdzTrasePrzycisk.setEnabled(false);
    }

    // -------------- WŁĄCZENIE PRZYCISKU WYSZUKANIA TRASY --------------
    // metoda, która włączy przycisk do wyszykania trasy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void wlaczPrzycisk() {
        Log.i(nazwaApki, "Koordynaty pomocnicze: " + pomocSzerGeog1 + " " + pomocDlugGeog1 + " " + pomocSzerGeog2 + " " + pomocDlugGeog2 + " ");

        if (pomocSzerGeog1 != 200 && pomocDlugGeog1 != 200 && pomocSzerGeog2 != 200 && pomocDlugGeog2 != 200) {
            Log.i(nazwaApki, "WŁĄCZONO PRZYCISK");
            TworzenieTrasy.zatwierdzTrasePrzycisk.setText("ZATWIERDŹ TRASĘ");
            TworzenieTrasy.zatwierdzTrasePrzycisk.setEnabled(true);

            GlownaAktywnosc.znajdzTrasePrzycisk.setText("ZNAJDŹ TRASĘ");
            GlownaAktywnosc.znajdzTrasePrzycisk.setEnabled(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void pobierzLokalizacjeGPS() {
        try {
            // pobranie aktualnej lokalizacji urządzenia i utworzenie nasłuchiwacza zmiany lokalizacji
            LocationListener nasluchiwaczLokalizacji = new LocationListener() {
                @Override
                // jeśli lokalizacja się zmieni to zostanie wywołana ta metoda
                public void onLocationChanged(Location location) {
                    // pobranie długosci i szerokości geograficznych urządzenia
                    pomocDlugGeog1 = location.getLongitude();
                    pomocSzerGeog1 = location.getLatitude();

                    try {
                        // ustalenie adresu urządzenia na podstawie wcześniej pobranych długosci i szerokości geograficznych
                        Address adres = new Geocoder(kontekst).getFromLocation(pomocSzerGeog1, pomocDlugGeog1, 1).get(0);
                        Log.i(nazwaApki, "Adres: " + adres);
                        String nazwaLokacji = adres.getAdminArea() + ", " + adres.getCountryName();
                        Log.i(nazwaApki, "Nazwa Lokacji: " + adres.getLocality());
                        if (adres.getLocality() != null) {
                            nazwaLokacji = adres.getLocality() + ", " + nazwaLokacji;
                        } else {
                            nazwaLokacji = adres.getSubAdminArea() + ", " + nazwaLokacji;
                        }
                        Log.i(nazwaApki, "Lokalizacja użytkownika: " + nazwaLokacji + " " + location.getLatitude() + " Lat, " + location.getLongitude() + " Lon");
                        pobranoZgps = true;
                        // wyświetlenie adresu urządzenia w pierwszym polu tekstowym (początek trasy)
                        TworzenieTrasy.poczatekAutouzupelnianie.setText(nazwaLokacji, false);
                    } catch (IOException e) {
                        // jeśli się nie udało pobrać adresu - aplikacja wyrzuci wyjątek
                        Log.i(nazwaApki, "Location Error: " + e.getMessage());
                    }
                }

                // inne metody nasłuchiwacza lokalizacji
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            // jeśli użytkownik zezwolił na pobieranie lokalizacji przez aplikację - wymuś pojedyńczą aktualizację lokalizacji
            // jeśli nie zezwolił - użytkownik będzie musiał sam wpisać początkową lokalizację
            if (!(ActivityCompat.checkSelfPermission(kontekst, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(kontekst, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                TworzenieTrasy.manadzerLokalizacji.requestSingleUpdate(LocationManager.GPS_PROVIDER, nasluchiwaczLokalizacji, null);
            }
        } catch (Exception e) {
            // jeśli się nie udało pobrać lokalizacji urządzenia - aplikacja wyrzuci wyjątek
            Log.i(nazwaApki, "Location Error: " + e.getMessage());
        }
    }
}
