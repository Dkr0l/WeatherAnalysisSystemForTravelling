package krolkozak.project.app.pomocnicze;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;

import krolkozak.project.app.Ustawienia;

import static krolkozak.project.app.tworzenietrasy.Mapa.nazwaApki;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WyswietlanieMapy {

    public static void wyswietlWidokMapy(MapView mapa) {
        mapa.invalidate();

        mapa.setTileSource(TileSourceFactory.MAPNIK);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && Ustawienia.trybCiemnyAktywny()) {
            wlaczCiemnyTrybMapy(mapa);
        }

        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapa.setMultiTouchControls(true);

        double MIN_PRZYBLIZENIE = 3;
        mapa.setMinZoomLevel(MIN_PRZYBLIZENIE);
        double MAX_PRZYBLIZENIE = 20;
        mapa.setMaxZoomLevel(MAX_PRZYBLIZENIE);

        IMapController kontrolerMapy = mapa.getController();
        kontrolerMapy.setZoom(9);

        GeoPoint punktPoczatkowyMapy = new GeoPoint(52.229676, 21.012229);
        kontrolerMapy.setCenter(punktPoczatkowyMapy);
    }

    public static void wyswietlZnacznikNaMapie(Context kontekst, MapView mapa, GeoPoint punktGeog, String tytulZnacznika, String opisZnacznika, int indeksObrazka) {
        Marker znacznik = new Marker(mapa);

        znacznik.setPosition(punktGeog);
        znacznik.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        znacznik.setIcon(kontekst.getApplicationContext().getDrawable(indeksObrazka));
        znacznik.setImage(kontekst.getApplicationContext().getDrawable(indeksObrazka));

        znacznik.setTitle(tytulZnacznika);
        znacznik.setSubDescription(opisZnacznika);

        mapa.getOverlays().add(znacznik);

        mapa.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void wlaczCiemnyTrybMapy(MapView mapa) {
        TilesOverlay plytkiMapy = mapa.getOverlayManager().getTilesOverlay();

        /*  4x5 matrix for transforming the color and alpha components of a Bitmap. The matrix can be passed as single array, and is treated as follows:
          [ a, b, c, d, e,
            f, g, h, i, j,
            k, l, m, n, o,
            p, q, r, s, t ]
        When applied to a color [R, G, B, A], the resulting color is computed as:
           R’ = a*R + b*G + c*B + d*A + e;
           G’ = f*R + g*G + h*B + i*A + j;
           B’ = k*R + l*G + m*B + n*A + o;
           A’ = p*R + q*G + r*B + s*A + t;      */

        float[] matrycaKolorow = {
                0, -1, -1, 0, 460, // Czerwony
                -1, 0, -1, 0, 460, // Zielony
                -1, -1, 0, 0, 460, // Niebieski
                0, 0, 0, 1, 0      //alpha (nie tykać!!)
        };

        plytkiMapy.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(matrycaKolorow)));
    }

    public static ArrayList<GeoPoint> wyswietlTraseNaMapie(MapView mapa, String punktyTrasy, String typTrasy) throws JSONException {
        RoadManager zarzadcaTrasy = new MapQuestRoadManager("ElrQRaDB6PgzWPc9z2n3LXGuZ8KfjFfi");
        zarzadcaTrasy.addRequestOption("routeType=" + typTrasy);

        ArrayList<GeoPoint> punkty = new ArrayList<>();
        JSONArray punktyTrasyJSON = new JSONArray(punktyTrasy);

        for (int i = 0; i < punktyTrasyJSON.length(); i++) {
            JSONObject koordynaty = (JSONObject) punktyTrasyJSON.getJSONObject(i);

            double szerGeog = koordynaty.getDouble("szer_geog");
            double dlugGeog = koordynaty.getDouble("dlug_geog");

            GeoPoint punktGeog = new GeoPoint(szerGeog, dlugGeog);
            punkty.add(punktGeog);
        }

        Road trasa = new Road();

        try {
            trasa = zarzadcaTrasy.getRoad(punkty);
        } catch (Exception e) {
            Log.i(nazwaApki, "Nie udało się wyświetlić trasy na mapie: " + e.getMessage());
        }

        Polyline warstwaTrasy = RoadManager.buildRoadOverlay(trasa);
        mapa.getOverlays().add(warstwaTrasy);
        mapa.invalidate();

        return punkty;
    }

}
