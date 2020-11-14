package krolkozak.project.app.pomocnicze;

import android.content.Context;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import krolkozak.project.app.R;

public class WyswietlanieMapy {

    public static void wyswietlWidokMapy(MapView mapa) {
        mapa.invalidate();

        mapa.setTileSource(TileSourceFactory.MAPNIK);

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

    public static void wyswietlZnacznikNaMapie(Context kontekst, MapView mapa, GeoPoint punktGeog, String tytulZnacznika, String opisZnacznika) {
        Marker znacznik = new Marker(mapa);

        znacznik.setPosition(punktGeog);
        znacznik.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // --------------------
        znacznik.setIcon(kontekst.getApplicationContext().getDrawable(R.drawable.sun));
        znacznik.setImage(kontekst.getApplicationContext().getDrawable(R.drawable.sun));
        // --------------------

        znacznik.setTitle(tytulZnacznika);
        znacznik.setSubDescription(opisZnacznika);

        mapa.getOverlays().add(znacznik);

        mapa.invalidate();
    }

}
