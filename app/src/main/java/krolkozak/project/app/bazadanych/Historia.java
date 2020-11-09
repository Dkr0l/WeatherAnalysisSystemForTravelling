package krolkozak.project.app.bazadanych;

import org.json.JSONArray;


public class Historia {
    private String id_uzytkownika;
    private JSONArray miejscaPogodowe;

    public Historia() {
    }

    public Historia(String id_uzytkownika, JSONArray miejscaPogodowe) {
        this.id_uzytkownika = id_uzytkownika;
        this.miejscaPogodowe = miejscaPogodowe;
    }

    public String pobierzObiekt() {
        return "Historia: Id: " + id_uzytkownika + ", miejsca pogodowe: " + miejscaPogodowe.toString();
    }

    public String getId_uzytkownika() {
        return id_uzytkownika;
    }

    public void setId_uzytkownika(String id_uzytkownika) {
        this.id_uzytkownika = id_uzytkownika;
    }

    public JSONArray getMiejscaPogodowe() {
        return miejscaPogodowe;
    }

    public void setMiejscaPogodowe(JSONArray miejscaPogodowe) {
        this.miejscaPogodowe = miejscaPogodowe;
    }
}

// private JSONArray miejscaPogodowe = new JSONArray();

// Historia historia = new Historia(FirebaseAuth.getInstance().getCurrentUser().getUid().toString(), miejscaPogodowe);
// Log.i(nazwaApki, "Lista obiektów pogodowych: " + historia.pobierzObiekt());

//JSONObject obiektPogodowy = new JSONObject();
//JSONObject warunkiPogodowe = new JSONObject();

// warunkiPogodowe.put("temperatura", temperaturaTekst);
// warunkiPogodowe.put("opady", opadyTekst);
// warunkiPogodowe.put("porywy_wiatru", porywyWiatryTekst);

// obiektPogodowy.put("lokalizacja", nazwaLokacji);
// obiektPogodowy.put("czas", pelnaData);

// Log.i(nazwaApki, "Obiekt pogodowy: " + obiektPogodowy.toString());
// miejscaPogodowe.put(obiektPogodowy);