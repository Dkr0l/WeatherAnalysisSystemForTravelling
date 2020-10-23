package krolkozak.project.app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Trasa {
    // rozmiar ramki wokół kadru widoku
    private final int ROZMIAR_RAMKI = 200;
    // widok mapy
    public MapView mapa;
    // koordynaty geograficzne
    public double szerGeog1, dlugGeog1;
    public double szerGeog2, dlugGeog2;
    // lista punktów geograficznych
    private ArrayList<GeoPoint> punkty = new ArrayList<>();
    // pomocnicza nazwa aplikacji do debuggowania
    private final String nazwaApki = "TRAVEL_APP";
    public OffsetDateTime czasWyjazdu=OffsetDateTime.now();
    //warunki pogodowe na trasie
    Pogoda pogoda=new Pogoda();

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
    public void odswiezMape(Context kontekst) {
        // -------------- CZYSZCZENIE MAPY --------------
        // wywołanie metody czyszcząca mapę (punkty i trasa)
        wyczyscMape();

        // -------------- POBRANIE DANYCH TRASY --------------
        JSONObject trasaJSON = new JSONObject();
        OffsetDateTime czasDotarcia= OffsetDateTime.now();
        try {
            // sformatowanie koordynatów, aby pasowały do adresu zapytania API
            String koordynaty1 = szerGeog1 + "," + dlugGeog1;
            String koordynaty2 = szerGeog2 + "," + dlugGeog2;

            // -------------- ZAPYTANIE O TRASĘ (mapquestapi.com) --------------
            // złączenie adresu url w jedną zmienną, która zawiera podstawową domenę zapytań API,
            // klucz, jednostki w kilometrach oraz koordynaty początkowe i końcowe trasy
            String url = "http://www.mapquestapi.com/directions/v2/route?key=ElrQRaDB6PgzWPc9z2n3LXGuZ8KfjFfi&unit=k&from=" + koordynaty1 + "&to=" + koordynaty2;
            Log.i(nazwaApki, "mapa url: " + url);
            // zapisanie wyniku zapytania do ciągu buforu
            StringBuffer odpowiedz = InterfejsAPI.pobierzOdpowiedzAPI(url);

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
            czasDotarcia = czasWyjazdu.plusSeconds(Long.parseLong(trasaJSON.getJSONArray("legs").getJSONObject(0).getString("time")));
        } catch (IOException | JSONException e) {
            // jeśli nie udało się pobrać trasy - aplikacja wyrzyci wyjątek
            Log.i(nazwaApki, "Blad trasy: " + e.getMessage());
        }

        //czas do dotarcia na miejsce w sekundach
        OffsetDateTime teraz=OffsetDateTime.now();
        pogoda.czasDoPrzyjazdu= ((java.time.Period.between(LocalDate.now(), czasDotarcia.toLocalDate()).getDays()*24+czasDotarcia.getHour()-teraz.getHour())*60+czasDotarcia.getMinute()-teraz.getMinute())*60;

        Log.i(nazwaApki, "Arrival Date: " + czasDotarcia);

        // -------------- DODANIE ZNACZNIKOW NA MAPIE --------------
        try {
            // dodanie punktów geograficznych do listy
            punkty.add(new GeoPoint(szerGeog1, dlugGeog1));
            punkty.add(new GeoPoint(szerGeog2, dlugGeog2));

            // -------------- KADROWANIE EKRANU --------------
            // skadrowanie widoku mapy na podstawie listy punktów
            mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(punkty), true, ROZMIAR_RAMKI);

            // wywołanie metody, która doda znacznik na mapie z informacją o miejscu, czasu i pogodzie
            // w danym punkcie na mapie
            dodajPunktPogodowy(szerGeog1, dlugGeog1, czasWyjazdu, kontekst);
            dodajPunktPogodowy(szerGeog2, dlugGeog2, czasDotarcia, kontekst);
        } catch (IOException e) {
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
        Road trasa = zarzadcaTrasy.getRoad(punkty);
        // utworzenie warstwy trasy i dodanie jej do mapy
        Polyline warstwaTrasy = RoadManager.buildRoadOverlay(trasa);
        mapa.getOverlays().add(warstwaTrasy);
    }

    // -------------- DODANIE PUNKTÓW POGODY NA TRASIE Z ODSTĘPEM CZASU --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda, która doda punkty pośrednie na trasie z obliczonym odstępem
    public void dodajPunktyPosrednie(JSONObject trasa, int odstep, Context kontekst) {
        int sumaCzasu = 0;
        long sekundy = 0;

        // -------------- PRZEJŚCIE PO CAŁEJ TRASIE I DODANIE PUNKTÓW POGODOWYCH Z OBLICZONYM ODSTĘPEM --------------
        try {
            // pobranie punktów manewrowych
            JSONArray manewryJSON = trasa.getJSONArray("legs").getJSONObject(0).getJSONArray("maneuvers");
            JSONObject manewr=new JSONObject();
            JSONObject koordynatyJSON=new JSONObject();
            JSONObject koordynatyPoprzednieJSON=new JSONObject();
            int dodanePunkty=0;

            // pętla przechodząca po wszystkich punktach manewrowych trasy, dodająca czas pomiędzy nimi do sumy
            for (int i = 0; i < manewryJSON.length(); i++) {
                // pobranie czasu z punktu manewrowego
                manewr = (JSONObject) manewryJSON.get(i);
                sumaCzasu += manewr.getInt("time");

                // jesli sumowany czas przekroczy obliczony odstęp - zostanie dodany punkt pogodowy na trasie w danym punkcie
                if (sumaCzasu > odstep) {
                    if(dodanePunkty>0) koordynatyPoprzednieJSON=koordynatyJSON;
                    koordynatyJSON = manewr.getJSONObject("startPoint");
                    if(sumaCzasu>=1.5*odstep && dodanePunkty>0){
                        dodajPunktyWLiniProstej(koordynatyPoprzednieJSON.getDouble("lat"), koordynatyJSON.getDouble("lat"), koordynatyPoprzednieJSON.getDouble("lng"), koordynatyJSON.getDouble("lng"), odstep, sumaCzasu, sekundy, kontekst);
                    }
                    sekundy += sumaCzasu;
                    sumaCzasu = 0;
                    dodanePunkty++;
                    dodajPunktPogodowy(koordynatyJSON.getDouble("lat"), koordynatyJSON.getDouble("lng"), czasWyjazdu.plusSeconds(sekundy), kontekst);
                }
            }
            JSONObject cel = trasa.getJSONObject("boundingBox").getJSONObject("ul");
        } catch (JSONException | IOException e) {
            // jeśli nie uda siędodać punktu pogodowego lub pobrać trasy - zostanie wyrzucony wyjątek
            Log.i(nazwaApki, "Blad punktow posrednich: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dodajPunktyWLiniProstej(double szerokosc1, double szerokosc2, double dlugosc1, double dlugosc2, int odstep, int sumaCzasu, long sekundy, Context kontekst) throws IOException {
        int doUzupelnienia= (int) Math.floor(sumaCzasu/odstep);
        double wektorLat=(szerokosc2-szerokosc1)/(doUzupelnienia+1);
        double wektorLng=(dlugosc2-dlugosc1)/(doUzupelnienia+1);
        double latPosrednie=szerokosc1;
        double lngPosrednie=dlugosc1;
        for(int k=1; k<=doUzupelnienia; k++){
            latPosrednie+=wektorLat;
            lngPosrednie+=wektorLng;
            dodajPunktPogodowy(latPosrednie, lngPosrednie,czasWyjazdu.plusSeconds(sekundy+(int)(k*odstep*0.9)), kontekst);
        }
    }

    // -------------- DODANIE PUNKTU Z POGODA NA MAPIE --------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    // metoda dodająca znacznik na mapie z informacją o pogodzie, miejscu i czasie, na podstawie
    // podanych koordynatów geograficznych oraz daty przybycia na miejsce
    public void dodajPunktPogodowy(Double szerGeog, Double dlugGeog, OffsetDateTime data, Context kontekst) throws IOException {
        // -------------- POBRANIE DANYCH O POGODZIE W DANYM CZASIE --------------
        List danePogodowe = pogoda.pobierzPogode(szerGeog, dlugGeog, data.toString().substring(0, 16) + "Z");

        // -------------- USTAWIENIE ZNACZNIKA --------------
        // ustawienie parametrów znacznika (pozycji, miejsca zakotwiczenia, ikony, obrazka)
        Marker znacznik = new Marker(mapa);
        znacznik.setPosition(new GeoPoint(szerGeog, dlugGeog));
        znacznik.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        znacznik.setIcon(kontekst.getApplicationContext().getResources().getDrawable(R.drawable.weather_marker));
        znacznik.setImage(kontekst.getApplicationContext().getResources().getDrawable(R.drawable.weather_icon));

        // -------------- SFORMATOWANIE DANYCH O POGODZIE I WYSWIETLENIE --------------
        // sformatowanie parametrów pogody oraz wyświetlenie ich jako tytuł
        String tytulZnacznika = "Wystąpił błąd.";
        if (danePogodowe != null) {
            String temperatureText = "Temperatura: " + danePogodowe.get(0) + pogoda.jednostkaTemp;
            String precipitationText = "Opady: " + danePogodowe.get(1) + pogoda.jednostkaOpad;
            tytulZnacznika = temperatureText + "\n" + precipitationText;
        }
        znacznik.setTitle(tytulZnacznika);

        // -------------- SFORMATOWANIE ADRESU ORAZ DATY I WYSWIETLENIE --------------
        // pobranie adresu na podstawie koordynatów geograficznych
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

        // -------------- DODANIE ZNACZNIKA NA MAPE --------------
        mapa.getOverlays().add(znacznik);

        // -------------- ODSWIEZENIE MAPY --------------
        mapa.invalidate();
    }

    public void zatrzymaj(){
        mapa.onPause();
    }
    public void wznow(){
        mapa.onResume();
    }
}
