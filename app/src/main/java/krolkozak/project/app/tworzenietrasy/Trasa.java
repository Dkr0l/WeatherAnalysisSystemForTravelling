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
import krolkozak.project.app.bazadanych.Historia;

import static krolkozak.project.app.pomocnicze.WyswietlanieMapy.pobierzTytulZnacznika;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Trasa {
    // rozmiar ramki wokół kadru widoku
    private final int ROZMIAR_RAMKI = 200;
    // widok mapy
    public MapView mapa;
    // koordynaty geograficzne
    public double szerGeog1, dlugGeog1;
    public double szerGeog2, dlugGeog2;
    public ArrayList<PunktPostoju> przystanki = new ArrayList<>();
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
    protected static int postep=1;
    protected static CharSequence opis="";

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
        punkty.add(new GeoPoint(51.49, -0.14)); // LONDYN
        punkty.add(new GeoPoint(41.01, 28.97)); // STAMBUŁ

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

            //domyślny środek transportu (jeżeli żaden nie został wybrany)
            if (srodek_transportu == null)
                srodek_transportu = "\uD83D\uDE97 samochód: najszybsza trasa";

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
                    PunktPostoju przystanek = przystanki.get(numPrzystanku);
                    czasPostojuMinutySuma += przystanek.getCzasPostojuMinuty();
                    url.append("\"").append(przystanek.getSzerGeog()).append(",").append(przystanek.getDlugGeog()).append("\",");
                }
            }
            url.append("\"").append(koordynaty2).append("\"], options:{routeType:").append(transport_doURL).append(",unit:k}}");
            Log.i(nazwaApki, "mapa url: " + url);

            opis="Pobieranie danych trasy";
            postep=10;

            // zapisanie wyniku zapytania do ciągu buforu

            StringBuffer odpowiedz = InterfejsAPI.pobierzOdpowiedzAPI(url.toString());
            //pobranie trasy z zapytania
            trasaJSON = (JSONObject) (new JSONObject(String.valueOf(odpowiedz))).get("route");

            //pobranie kodu błędu
            String kodBledu = ((JSONObject) trasaJSON.get("routeError")).getString("errorCode");
            // jeśli kodu błędu jest inny niż (-400) - nie znaleziono trasy -  wyświetl komunikat i przerwij metodę
            if (!kodBledu.equals("-400")) {
                Mapa.handler.post(()->Toast.makeText(kontekst, "Nie udało się znaleźć trasy!", Toast.LENGTH_LONG).show());
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

        opis="Dodawanie punktów pogodowych początkowego i końcowego";
        postep=15;

        // -------------- DODANIE ZNACZNIKOW NA MAPIE --------------
        try {
            // wywołanie metody, która doda znacznik na mapie z informacją o miejscu, czasu i pogodzie
            // w danym punkcie na mapie
            dodajPunktPogodowy(szerGeog1, dlugGeog1, czasWyjazdu, kontekst, 1);
            dodajPunktPogodowy(szerGeog2, dlugGeog2, czasDotarcia, kontekst, 2);
        } catch (IOException | JSONException e) {
            // jeśli nie udało się dodać punktów pogodowych - aplikacja wyrzuci wyjatek
            e.printStackTrace();
        }

        opis="Dodawanie punktów pogodowych na trasie 0%";
        postep=20;

        // wywołanie metody, która obliczy minimalny odstęp czasu pomiędzy punktami na trasie
        int odstep = pogoda.wyznaczDlugoscOdstepu();
        // wywołanie metody, która doda punkty pośrednie na trasie z obliczonym odstępem
        dodajPunktyPosrednie(trasaJSON, odstep, kontekst);


        // -------------- KADROWANIE EKRANU --------------
        // skadrowanie widoku mapy na podstawie listy punktów
        Mapa.handler.post(() -> mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(punkty), true, ROZMIAR_RAMKI));

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
        postep=100;
    }

    // -------------- DODANIE PUNKTÓW POGODY NA TRASIE Z ODSTĘPEM CZASU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda, która doda punkty pośrednie na trasie z obliczonym odstępem
    public void dodajPunktyPosrednie(JSONObject trasa, int odstep, Context kontekst) throws JSONException {
        postep=20;
        // -------------- PRZEJŚCIE PO CAŁEJ TRASIE I DODANIE PUNKTÓW POGODOWYCH Z OBLICZONYM ODSTĘPEM --------------
        try {
            int sumaCzasu = 0;
            long sekundy = 0;
            // pobranie punktów manewrowych
            JSONArray obiektLegsJSON = trasa.getJSONArray("legs");
            JSONObject manewr;
            JSONObject koordynatyJSON = new JSONObject();
            JSONObject koordynatyPoprzednieJSON = new JSONObject();
            punkty.add(new GeoPoint(szerGeog1, dlugGeog1));
            for (int numOdcinka = 0; numOdcinka < obiektLegsJSON.length(); numOdcinka++) {
                if(obiektLegsJSON.length()>1) {
                    opis="Dodawanie punktów pogodowych na trasie " + (int) (80*numOdcinka / obiektLegsJSON.length()) + "%";
                    postep=(int) (20+80*numOdcinka / obiektLegsJSON.length());
                }
                PunktPostoju obecnyPrzystanek;
                if (numOdcinka < przystanki.size()) {
                    obecnyPrzystanek = przystanki.get(numOdcinka);
                    punkty.add(new GeoPoint(obecnyPrzystanek.getSzerGeog(), obecnyPrzystanek.getDlugGeog()));
                }
                JSONArray manewryJSON = obiektLegsJSON.getJSONObject(numOdcinka).getJSONArray("maneuvers");
                int dodanePunkty = 0;
                // pętla przechodząca po wszystkich punktach manewrowych trasy, dodająca czas pomiędzy nimi do sumy
                for (int i = 0; i < manewryJSON.length(); i++) {
                    if(obiektLegsJSON.length()==1) {
                        opis="Dodawanie punktów pogodowych na trasie " + (int) (80*i / manewryJSON.length()) + "%";
                        postep=(int) (20+80*i / manewryJSON.length());
                    }
                    manewr = (JSONObject) manewryJSON.get(i);

                    // jesli sumowany czas przekroczy obliczony odstęp - zostanie dodany punkt pogodowy na trasie w danym punkcie
                    if (sumaCzasu > (odstep * 0.95)) {
                        if (dodanePunkty > 0) {
                            koordynatyPoprzednieJSON = koordynatyJSON;
                        }
                        koordynatyJSON = manewr.getJSONObject("startPoint");
                        punkty.add(new GeoPoint(koordynatyJSON.getDouble("lat"), koordynatyJSON.getDouble("lng")));
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
            punkty.add(new GeoPoint(szerGeog2, dlugGeog2));

            JSONArray punktyTrasy = new JSONArray();

            JSONObject punkt_pocz = new JSONObject();
            punkt_pocz.put("nazwa", lokalizacja_poczatkowa);
            punkt_pocz.put("szer_geog", punkty.get(0).getLatitude());
            punkt_pocz.put("dlug_geog", punkty.get(0).getLongitude());
            punkt_pocz.put("indeks", "POCZATEK");
            punkt_pocz.put("czas_postoju", 0);
            punktyTrasy.put(punkt_pocz);

            for (PunktPostoju przystanek : przystanki) {
                JSONObject punkt = new JSONObject();
                punkt.put("nazwa", przystanek.getNazwa());
                punkt.put("szer_geog", przystanek.getSzerGeog());
                punkt.put("dlug_geog", przystanek.getDlugGeog());
                punkt.put("indeks", "PRZYSTANEK");
                punkt.put("czas_postoju", przystanek.getCzasPostojuMinuty());
                punktyTrasy.put(punkt);
            }

            JSONObject punkt_konc = new JSONObject();
            punkt_konc.put("nazwa", lokalizacja_koncowa);
            punkt_konc.put("szer_geog", punkty.get(punkty.size() - 1).getLatitude());
            punkt_konc.put("dlug_geog", punkty.get(punkty.size() - 1).getLongitude());
            punkt_konc.put("indeks", "KONIEC");
            punkt_konc.put("czas_postoju", 0);
            punktyTrasy.put(punkt_konc);

            Mapa.obecnePunktyTrasy=punktyTrasy;

            Historia historia = new Historia(FirebaseAuth.getInstance().getCurrentUser().getUid(), lokalizacja_poczatkowa, lokalizacja_koncowa, czasWyjazdu.toString(), miejscaPogodowe.toString(), transport_doURL, punktyTrasy.toString());
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

    // -------------- DODANIE PUNKTU Z POGODA NA MAPIE --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda dodająca znacznik na mapie z informacją o pogodzie, miejscu i czasie, na podstawie
    // podanych koordynatów geograficznych oraz daty przybycia na miejsce
    public void dodajPunktPogodowy(Double szerGeog, Double dlugGeog, OffsetDateTime data, Context kontekst, int oznaczenieKolejnosciPunktu) throws IOException, JSONException {
        JSONObject obiektPogodowy = new JSONObject();

        // -------------- POBRANIE DANYCH O POGODZIE W DANYM CZASIE --------------
        List danePogodowe = pogoda.pobierzPogode(szerGeog, dlugGeog, data.toString().substring(0, 16) + "Z");

        // -------------- USTAWIENIE ZNACZNIKA --------------
        // ustawienie parametrów znacznika (pozycji, miejsca zakotwiczenia, ikony, obrazka)
        Marker znacznik = new Marker(mapa);
        znacznik.setPosition(new GeoPoint(szerGeog, dlugGeog));
        znacznik.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // -------------- SFORMATOWANIE DANYCH O POGODZIE I WYSWIETLENIE --------------
        // sformatowanie parametrów pogody oraz wyświetlenie ich jako tytuł

        int indeksObrazka = R.drawable.error;
        if (danePogodowe != null) {
            indeksObrazka = pobierzIndeksObrazka((String) danePogodowe.get(0));
        }

        String tytulZnacznika = pobierzTytulZnacznika(danePogodowe, Pogoda.typPrognozy);

        znacznik.setIcon(kontekst.getApplicationContext().getDrawable(indeksObrazka));
        znacznik.setImage(kontekst.getApplicationContext().getDrawable(indeksObrazka));
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
            obiektPogodowy.put("pogoda_odpowiedz_api", Pogoda.pogodaOdpowiedzApiTekst);
            obiektPogodowy.put("typ_prognozy", Pogoda.typPrognozy);
            obiektPogodowy.put("indeks_obrazka", indeksObrazka);

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

    public int pobierzIndeksObrazka(String kodPogodowy) {
        switch (kodPogodowy) {
            case "freezing_rain_heavy":
            case "freezing_rain":
            case "freezing_rain_light":
            case "freezing_drizzle":
            case "ice_pellets_heavy":
            case "ice_pellets":
            case "ice_pellets_light":
                return R.drawable.blizzard;
            case "snow_heavy":
            case "snow":
            case "snow_light":
                return R.drawable.snow;
            case "flurries":
                return R.drawable.winter;
            case "tstorm":
                return R.drawable.storm2;
            case "rain_heavy":
                return R.drawable.rain2;
            case "rain":
            case "rain_light":
            case "drizzle":
                return R.drawable.rain3;
            case "fog_light":
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
                return R.drawable.error;
        }
    }

    public void zatrzymaj() {
        mapa.onPause();
    }

    public void wznow() {
        mapa.onResume();
    }
}
