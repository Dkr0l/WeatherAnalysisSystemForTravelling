package krolkozak.project.app.tworzenietrasy;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import krolkozak.project.app.InterfejsAPI;
import krolkozak.project.app.R;
import krolkozak.project.app.Ustawienia;
import krolkozak.project.app.bazadanych.Historia;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Trasa {
    // rozmiar ramki wokół kadru widoku
    private final int ROZMIAR_RAMKI = 200;
    // widok mapy
    public MapView mapa;
    // koordynaty geograficzne
    public double szerGeog1, dlugGeog1;
    public double szerGeog2, dlugGeog2;
    public ArrayList<PunkPostoju> przystanki = new ArrayList<>();
    //środek transportu
    public String srodek_transportu;
    private String transport_doURL = "";
    // lista punktów geograficznych
    public ArrayList<GeoPoint> punkty = new ArrayList<>();
    // pomocnicza nazwa aplikacji do debuggowania
    private final String nazwaApki = "TRAVEL_APP";
    public OffsetDateTime czasWyjazdu = OffsetDateTime.now();
    //warunki pogodowe na trasie
    private Pogoda pogoda = new Pogoda();
    private JSONArray miejscaPogodowe = new JSONArray();
    private String lokalizacja_poczatkowa = "";
    private String lokalizacja_koncowa = "";

    // metoda czyszcząca mapę (punkty i trasa)
    public void wyczyscMape() {
        // usunięcie punktów z listy
        punkty = new ArrayList<>();
        List<Overlay> mapOverlays = mapa.getOverlays();

        // usunięcie wszystkich rzeczy z mapy
        for (int i = mapOverlays.size() - 1; i >= 0; i--) {
            mapOverlays.remove(mapOverlays.get(i));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda, która ustawia początkowe parametry mapy oraz wyświetla na ekranie
    public void zainicjujMape() {
        // -------------- WYSWIETLANIE MAPY --------------
        // ustawienie przycisków przybliżania i oddalania widoku mapy na stale widoczne
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        // włączenie obsługi wielokrotnego dotyku (mutli-touch)
        mapa.setMultiTouchControls(true);
        // -------------- CZYSZCZENIE MAPY --------------
        // wywołanie metody czyszcząca mapę (punkty i trasa)
        wyczyscMape();

        // Punkty wokół których zostanie wykadrowana mapa na start aplikacji
        punkty.add(new GeoPoint(40.730610, -73.935242)); // NOWY JORK
        punkty.add(new GeoPoint(41.015137, 28.979530)); // STAMBUŁ

        // -------------- DOSTOSOWANIE PRZYBLIZENIA --------------
        // ustawienie minimalnego i maksymalnego możliwego przybliżenia
        // minimalne i maksymalne przybliżenie mapy
        double MIN_PRZYBLIZENIE = 3;
        mapa.setMinZoomLevel(MIN_PRZYBLIZENIE);
        double MAX_PRZYBLIZENIE = 20;
        mapa.setMaxZoomLevel(MAX_PRZYBLIZENIE);
        // po uruchomieniu aplikacji mapa ustawi kadr widoku na dane punkty
        mapa.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            BoundingBox obszarDoWyswietlenia = BoundingBox.fromGeoPoints(punkty);
            mapa.zoomToBoundingBox(obszarDoWyswietlenia, true, ROZMIAR_RAMKI);
            mapa.invalidate();
        });

        // -------------- ODSWIEZENIE MAPY --------------
        mapa.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda, która obliczy i wyświetli trasę wraz z punktami pogodowymi na mapie
    public void odswiezMape(Context kontekst) throws JSONException {
        // -------------- CZYSZCZENIE MAPY --------------
        // wywołanie metody czyszcząca mapę (punkty i trasa)
        wyczyscMape();

        // -------------- POBRANIE DANYCH TRASY --------------
        JSONObject trasaJSON = new JSONObject();
        OffsetDateTime czasDotarcia = OffsetDateTime.now();
        try {
            // sformatowanie koordynatów, aby pasowały do adresu zapytania API
            String koordynaty1 = szerGeog1 + "," + dlugGeog1;
            String koordynaty2 = szerGeog2 + "," + dlugGeog2;
            switch (srodek_transportu) {
                case "\uD83D\uDE97 samochód: najszybsza trasa":
                    transport_doURL = "fastest";
                    break;
                case "\uD83D\uDE97 samochód: najkrótsza trasa":
                    transport_doURL = "shortest";
                    break;
                case "\uD83D\uDEB2 rower":
                    transport_doURL = "bicycle";
                    break;
                case "\uD83D\uDEB6 pieszo":
                    transport_doURL = "pedestrian";
                    break;
            }

            // -------------- ZAPYTANIE O TRASĘ (mapquestapi.com) --------------
            // złączenie adresu url w jedną zmienną, która zawiera podstawową domenę zapytań API,
            // klucz, jednostki w kilometrach oraz koordynaty początkowe i końcowe trasy
            StringBuilder url = new StringBuilder("http://www.mapquestapi.com/directions/v2/optimizedroute?key=ElrQRaDB6PgzWPc9z2n3LXGuZ8KfjFfi&json={\"locations\":[\"" + koordynaty1 + "\",");
            int czasPostojuMinutySuma = 0;
            if (przystanki.size() > 0) {
                for (int numPrzystanku = 0; numPrzystanku < przystanki.size(); numPrzystanku++) {
                    PunkPostoju przystanek = przystanki.get(numPrzystanku);
                    czasPostojuMinutySuma += przystanek.getCzasPostojuMinuty();
                    url.append("\"").append(przystanek.getSzerGeog()).append(",").append(przystanek.getDlugGeog()).append("\",");
                }
            }
            url.append("\"").append(koordynaty2).append("\"], options:{routeType:").append(transport_doURL).append(",unit:k}}");
            Log.i(nazwaApki, "mapa url: " + url);
            // zapisanie wyniku zapytania do ciągu buforu
            StringBuffer odpowiedz = InterfejsAPI.pobierzOdpowiedzAPI(url.toString());

            //pobranie trasy z zapytania
            trasaJSON = (JSONObject) (new JSONObject(String.valueOf(odpowiedz))).get("route");

            //pobranie kodu błędu
            String kodBledu = ((JSONObject) trasaJSON.get("routeError")).getString("errorCode");
            // jeśli kodu błędu jest inny niż (-400) - nie znaleziono trasy -  wyświetl komunikat i przerwij metodę
            if (!kodBledu.equals("-400")) {
                Toast.makeText(kontekst, "Nie udało się znaleźć trasy!", Toast.LENGTH_LONG).show();
                Log.i(nazwaApki, "Kod błędu: " + kodBledu);
                return;
            }

            // -------------- SFORMATOWANY CZAS TRASY --------------
            // pobranie sformatowanego czasu trasy z zapytania
            String formatowanyCzas = trasaJSON.getString("formattedTime");
            Log.i(nazwaApki, "FormattedTime: " + formatowanyCzas);

            // -------------- DYSTANS TRASY [KM] --------------
            // pobranie dystansu trasy
            String odleglosc = trasaJSON.getString("distance");
            Log.i(nazwaApki, "Distance: " + odleglosc);

            // -------------- SFORMATOWANY CZAS PRZYJAZDU NA MIEJSCE --------------
            // sformatowanie czasu przyjazdu na miejsce (obecny czas dodać pobrany czas trasy w sekundach)
            // czas dotarcia do celu
            String[] wartosciCzasu = trasaJSON.getString("formattedTime").split(":");
            int sekundyDoCelu = Integer.parseInt(wartosciCzasu[0]) * 3600 + Integer.parseInt(wartosciCzasu[1]) * 60 + Integer.parseInt(wartosciCzasu[0]);

            czasDotarcia = czasWyjazdu.plusSeconds(sekundyDoCelu + (czasPostojuMinutySuma * 60));
        } catch (IOException | JSONException e) {
            // jeśli nie udało się pobrać trasy - aplikacja wyrzyci wyjątek
            Log.i(nazwaApki, "Blad trasy: " + e.getMessage());
        }

        //czas do dotarcia na miejsce w sekundach
        OffsetDateTime teraz = OffsetDateTime.now();
        pogoda.czasDoPrzyjazdu = ((java.time.Period.between(LocalDate.now(), czasDotarcia.toLocalDate()).getDays() * 24 + czasDotarcia.getHour() - teraz.getHour()) * 60 + czasDotarcia.getMinute() - teraz.getMinute()) * 60;

        Log.i(nazwaApki, "Arrival Date: " + czasDotarcia);

        // -------------- DODANIE ZNACZNIKOW NA MAPIE --------------
        try {
            // dodanie punktów geograficznych do listy
            punkty.add(new GeoPoint(szerGeog1, dlugGeog1));
            if (przystanki.size() > 0) {
                for (int numPrzystanku = 0; numPrzystanku < przystanki.size(); numPrzystanku++) {
                    PunkPostoju przystanek = przystanki.get(numPrzystanku);
                    punkty.add(new GeoPoint(przystanek.getSzerGeog(), przystanek.getDlugGeog()));
                }
            }
            punkty.add(new GeoPoint(szerGeog2, dlugGeog2));

            // -------------- KADROWANIE EKRANU --------------
            // skadrowanie widoku mapy na podstawie listy punktów
            mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(punkty), true, ROZMIAR_RAMKI);

            // wywołanie metody, która doda znacznik na mapie z informacją o miejscu, czasu i pogodzie
            // w danym punkcie na mapie
            dodajPunktPogodowy(szerGeog1, dlugGeog1, czasWyjazdu, kontekst, 1);
            dodajPunktPogodowy(szerGeog2, dlugGeog2, czasDotarcia, kontekst, 2);
        } catch (IOException | JSONException e) {
            // jeśli nie udało się dodać punktów pogodowych - aplikacja wyrzuci wyjatek
            e.printStackTrace();
        }

        // wywołanie metody, która obliczy minimalny odstęp czasu pomiędzy punktami na trasie
        int odstep = pogoda.wyznaczDlugoscOdstepu();
        // wywołanie metody, która doda punkty pośrednie na trasie z obliczonym odstępem
        dodajPunktyPosrednie(trasaJSON, odstep, kontekst);

        // -------------- TWORZENIE TRASY POMIEDZY DWOMA PUNTKAMI NA MAPIE --------------
        // utworzenie zarzadcy trasy z podanym kluczem API
        RoadManager zarzadcaTrasy = new MapQuestRoadManager("ElrQRaDB6PgzWPc9z2n3LXGuZ8KfjFfi");
        // utworzenie trasy na podstawie listy punktów
        zarzadcaTrasy.addRequestOption("routeType=" + transport_doURL);
        Road trasa = new Road();
        try {
            trasa = zarzadcaTrasy.getRoad(punkty);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // utworzenie warstwy trasy i dodanie jej do mapy
        Polyline warstwaTrasy = RoadManager.buildRoadOverlay(trasa);
        mapa.getOverlays().add(warstwaTrasy);
    }

    // -------------- DODANIE PUNKTÓW POGODY NA TRASIE Z ODSTĘPEM CZASU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda, która doda punkty pośrednie na trasie z obliczonym odstępem
    public void dodajPunktyPosrednie(JSONObject trasa, int odstep, Context kontekst) throws JSONException {
        // -------------- PRZEJŚCIE PO CAŁEJ TRASIE I DODANIE PUNKTÓW POGODOWYCH Z OBLICZONYM ODSTĘPEM --------------
        try {
            int sumaCzasu = 0;
            long sekundy = 0;
            // pobranie punktów manewrowych
            JSONArray obiektLegsJSON = trasa.getJSONArray("legs");
            JSONObject manewr;
            JSONObject koordynatyJSON = new JSONObject();
            JSONObject koordynatyPoprzednieJSON = new JSONObject();
            for (int numOdcinka = 0; numOdcinka < obiektLegsJSON.length(); numOdcinka++) {
                JSONArray manewryJSON = obiektLegsJSON.getJSONObject(numOdcinka).getJSONArray("maneuvers");
                int dodanePunkty = 0;
                // pętla przechodząca po wszystkich punktach manewrowych trasy, dodająca czas pomiędzy nimi do sumy
                for (int i = 0; i < manewryJSON.length(); i++) {
                    manewr = (JSONObject) manewryJSON.get(i);

                    // jesli sumowany czas przekroczy obliczony odstęp - zostanie dodany punkt pogodowy na trasie w danym punkcie
                    if (sumaCzasu > (odstep * 0.95)) {
                        if (dodanePunkty > 0) {
                            koordynatyPoprzednieJSON = koordynatyJSON;
                        }
                        koordynatyJSON = manewr.getJSONObject("startPoint");
                        if (sumaCzasu > odstep && dodanePunkty > 0) {
                            double latSr = (koordynatyPoprzednieJSON.getDouble("lat") + koordynatyJSON.getDouble("lat")) / 2;
                            double lngSr = (koordynatyPoprzednieJSON.getDouble("lng") + koordynatyJSON.getDouble("lng")) / 2;
                            dodajPunktPogodowy(latSr, lngSr, czasWyjazdu.plusSeconds(sekundy + (int) (0.5 * sumaCzasu)), kontekst, 0);
                        } else if (sumaCzasu > (2 * odstep) && dodanePunkty > 0) {
                            dodajPunktyWLiniProstej(koordynatyPoprzednieJSON.getDouble("lat"), koordynatyJSON.getDouble("lat"), koordynatyPoprzednieJSON.getDouble("lng"), koordynatyJSON.getDouble("lng"), odstep, sumaCzasu, sekundy, kontekst);
                        }
                        sekundy += sumaCzasu;
                        sumaCzasu = 0;
                        dodanePunkty++;
                        dodajPunktPogodowy(koordynatyJSON.getDouble("lat"), koordynatyJSON.getDouble("lng"), czasWyjazdu.plusSeconds(sekundy), kontekst, 0);
                    }
                    // pobranie czasu z punktu manewrowego
                    sumaCzasu += manewr.getInt("time");
                }
                Log.i(nazwaApki, " numer odcinka: " + numOdcinka + " na " + (obiektLegsJSON.length() - 1));
                if (numOdcinka != obiektLegsJSON.length() - 1) {
                    sekundy += (przystanki.get(numOdcinka).getCzasPostojuMinuty()) * 60;
                }
            }

            Historia historia = new Historia(FirebaseAuth.getInstance().getCurrentUser().getUid(), lokalizacja_poczatkowa, lokalizacja_koncowa, czasWyjazdu.toString(), miejscaPogodowe.toString(), transport_doURL);
            Log.i(nazwaApki, "Historia: " + historia.pobierzObiekt());

            dodajDokumentHistoriiDoBazy(historia);
        } catch (JSONException | IOException e) {
            // jeśli nie uda siędodać punktu pogodowego lub pobrać trasy - zostanie wyrzucony wyjątek
            Log.i(nazwaApki, "Blad punktow posrednich: " + e.getMessage());
        }
    }

    private void dodajDokumentHistoriiDoBazy(Historia historia) {
        Log.i(nazwaApki, "Dodawanie dokumentu historii do bazy: " + historia.pobierzObiekt());

        FirebaseFirestore.getInstance().collection("historia").add(historia).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i(nazwaApki, "Pomyślnie dodano nowy dokument historii o ID: " + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(nazwaApki, "Błąd dodoawania nowego dokumentu historii: " + e.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dodajPunktyWLiniProstej(double szerokosc1, double szerokosc2, double dlugosc1, double dlugosc2, int odstep, int sumaCzasu, long sekundy, Context kontekst) throws IOException, JSONException {
        int doUzupelnienia = sumaCzasu / odstep;
        double wektorLat = (szerokosc2 - szerokosc1) / (doUzupelnienia + 1);
        double wektorLng = (dlugosc2 - dlugosc1) / (doUzupelnienia + 1);
        double latPosrednie = szerokosc1;
        double lngPosrednie = dlugosc1;
        for (int k = 1; k <= doUzupelnienia; k++) {
            latPosrednie += wektorLat;
            lngPosrednie += wektorLng;
            dodajPunktPogodowy(latPosrednie, lngPosrednie, czasWyjazdu.plusSeconds(sekundy + (k * sumaCzasu / (doUzupelnienia + 1))), kontekst, 0);
        }
    }

    private void dodajIkony(Marker znacznik, Double wiatr, Double opady, String kodPogodowy, Context kontekst) {
        switch (kodPogodowy) {
            case "freezing_rain_heavy":
            case "freezing_rain":
            case "freezing_rain_light":
            case "freezing_drizzle":
            case "ice_pellets_heavy":
            case "ice_pellets":
            case "ice_pellets_light":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.blizzard));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.blizzard));
                break;
            case "snow_heavy":
            case "snow":
            case "snow_light":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.snow));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.snow));
                break;
            case "flurries":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.winter));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.winter));
                break;
            case "tstorm":
                if (wiatr <= 10 && opady <= 1) {
                    znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.thunderstorm));
                    znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.thunderstorm));
                } else if (opady > 1) {
                    znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.storm2));
                    znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.storm2));
                } else if (wiatr > 10) {
                    znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.storm));
                    znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.storm));
                }
                break;
            case "rain_heavy":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.rain2));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.rain2));
                break;
            case "rain":
            case "rain_light":
            case "drizzle":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.rain3));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.rain3));
                break;
            case "fog_light":
            case "fog":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.fog));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.fog));
                break;
            case "cloudy":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.clouds_heavy));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.clouds_heavy));
                break;
            case "mostly_cloudy":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.mostly_cloudy));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.mostly_cloudy));
                break;
            case "partly_cloudy":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.cloudy));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.cloudy));
                break;
            case "mostly_clear":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.cloud));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.cloud));
                break;
            case "clear":
                znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.sun));
                znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.sun));
                break;
        }
    }

    // -------------- DODANIE PUNKTU Z POGODA NA MAPIE --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda dodająca znacznik na mapie z informacją o pogodzie, miejscu i czasie, na podstawie
    // podanych koordynatów geograficznych oraz daty przybycia na miejsce
    public void dodajPunktPogodowy(Double szerGeog, Double dlugGeog, OffsetDateTime data, Context kontekst, int oznaczenieKolejnosciPunktu) throws IOException, JSONException {
        JSONObject obiektPogodowy = new JSONObject();
        JSONObject warunkiPogodowe = new JSONObject();

        // -------------- POBRANIE DANYCH O POGODZIE W DANYM CZASIE --------------
        List danePogodowe = pogoda.pobierzPogode(szerGeog, dlugGeog, data.toString().substring(0, 16) + "Z");

        // -------------- USTAWIENIE ZNACZNIKA --------------
        // ustawienie parametrów znacznika (pozycji, miejsca zakotwiczenia, ikony, obrazka)
        Marker znacznik = new Marker(mapa);
        znacznik.setPosition(new GeoPoint(szerGeog, dlugGeog));
        znacznik.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // -------------- SFORMATOWANIE DANYCH O POGODZIE I WYSWIETLENIE --------------
        // sformatowanie parametrów pogody oraz wyświetlenie ich jako tytuł
        String tytulZnacznika = "Wystąpił błąd.";
        if (danePogodowe != null) {
            final int indeksObrazka = pobierzIndeksObrazka(Double.parseDouble((String) danePogodowe.get(2)), Double.parseDouble((String) danePogodowe.get(1)), (String) danePogodowe.get(3));
            znacznik.setIcon(kontekst.getApplicationContext().getDrawable(indeksObrazka));
            znacznik.setImage(kontekst.getApplicationContext().getDrawable(indeksObrazka));

            double wartoscTemperatury = Double.parseDouble((String) danePogodowe.get(0));
            if (Ustawienia.jednostkaTemperatury().equals("F"))
                wartoscTemperatury = wartoscTemperatury * 1.8 + 32;
            String temperaturaTekst = "Temperatura: " + wartoscTemperatury + Ustawienia.jednostkaTemperatury();

            String opadyTekst = "Opady: " + danePogodowe.get(1) + Ustawienia.jednostkaOpadow();

            double wartoscWiatru = Double.parseDouble((String) danePogodowe.get(2));
            if (Ustawienia.jednostkaWiatru().equals("km/h")) wartoscWiatru *= 3.6;
            else if (Ustawienia.jednostkaWiatru().equals("mph")) wartoscWiatru *= 2.23693629;
            String porywyWiatryTekst = "Porywy wiatru: " + wartoscWiatru + Ustawienia.jednostkaWiatru();
            tytulZnacznika = temperaturaTekst + "\n" + opadyTekst + "\n" + porywyWiatryTekst;

            warunkiPogodowe.put("temperatura", temperaturaTekst);
            warunkiPogodowe.put("opady", opadyTekst);
            warunkiPogodowe.put("porywy_wiatru", porywyWiatryTekst);
            warunkiPogodowe.put("indeks_obrazka", indeksObrazka);
        }
        znacznik.setTitle(tytulZnacznika);

        // -------------- SFORMATOWANIE ADRESU ORAZ DATY I WYSWIETLENIE --------------
        // pobranie adresu na podstawie koordynatów geograficznych
        try {
            Address adres = new Geocoder(kontekst).getFromLocation(szerGeog, dlugGeog, 1).get(0);
            Log.i(nazwaApki, "Address: " + adres.toString());
            // pobranie nazwy lokalizacji z adresu
            String nazwaLokacji = adres.getAdminArea() + ", " + adres.getCountryName();
            Log.i(nazwaApki, "Nazwa Lokacji: " + adres.getLocality());
            if (adres.getLocality() != null) {
                nazwaLokacji = adres.getLocality() + ", " + nazwaLokacji;
            } else {
                nazwaLokacji = adres.getSubAdminArea() + ", " + nazwaLokacji;
            }
            Log.i(nazwaApki, "Location: " + nazwaLokacji);
            // sformatowanie pełnej daty
            String pelnaData = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(data);
            // ustawienie opisu znacznika - lokalizacja i data
            znacznik.setSubDescription(nazwaLokacji + "<br>" + pelnaData);

            JSONObject koordynaty = new JSONObject();
            koordynaty.put("szer_geog", szerGeog);
            koordynaty.put("dlug_geog", dlugGeog);

            obiektPogodowy.put("koordynaty", koordynaty);
            obiektPogodowy.put("lokalizacja", nazwaLokacji);
            obiektPogodowy.put("czas", pelnaData);
            obiektPogodowy.put("warunki", warunkiPogodowe);

            if (oznaczenieKolejnosciPunktu == 1) {
                lokalizacja_poczatkowa = nazwaLokacji;
            } else if (oznaczenieKolejnosciPunktu == 2) {
                lokalizacja_koncowa = nazwaLokacji;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.i(nazwaApki, "Błędny podpis punktu pogodowego: " + e.getMessage());
        }

        Log.i(nazwaApki, "Obiekt pogodowy: " + obiektPogodowy.toString());
        miejscaPogodowe.put(obiektPogodowy);

        // -------------- DODANIE ZNACZNIKA NA MAPE --------------
        mapa.getOverlays().add(znacznik);

        // -------------- ODSWIEZENIE MAPY --------------
        mapa.invalidate();
    }

    public int pobierzIndeksObrazka(Double wiatr, Double opady, String kodPogodowy) {
        switch (kodPogodowy) {
//            case "freezing_rain_heavy":
//            case "freezing_rain":
//            case "freezing_rain_light":
//            case "freezing_drizzle":
//            case "ice_pellets_heavy":
//            case "ice_pellets":
            case "ice_pellets_light":
                return R.drawable.blizzard;
//            case "snow_heavy":
//            case "snow":
            case "snow_light":
                return R.drawable.snow;
            case "flurries":
                return R.drawable.winter;
            case "tstorm":
                if (wiatr <= 10 && opady <= 1) {
                    return R.drawable.thunderstorm;
                } else if (opady > 1) {
                    return R.drawable.storm2;
                } else if (wiatr > 10) {
                    return R.drawable.storm;
                }
            case "rain_heavy":
                return R.drawable.rain2;
//            case "rain":
//            case "rain_light":
            case "drizzle":
                return R.drawable.rain3;
//            case "fog_light":
            case "fog":
                return R.drawable.fog;
            case "cloudy":
                return R.drawable.clouds_heavy;
            case "mostly_cloudy":
                return R.drawable.mostly_cloudy;
            case "partly_cloudy":
                return R.drawable.cloudy;
            case "mostly_clear":
                return R.drawable.cloud;
            case "clear":
                return R.drawable.sun;
            default:
                return R.drawable.marker_default;
        }
    }

    public void zatrzymaj() {
        mapa.onPause();
    }

    public void wznow() {
        mapa.onResume();
    }
}
