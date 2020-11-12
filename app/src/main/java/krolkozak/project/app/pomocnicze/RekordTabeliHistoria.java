package krolkozak.project.app.pomocnicze;

public class RekordTabeliHistoria {
    private String start, koniec, data;

    public RekordTabeliHistoria() {
    }

    public RekordTabeliHistoria(String start, String koniec, String data) {
        this.start = start;
        this.koniec = koniec;
        this.data = data;
    }

    public String pobierzObiekt() {
        return "start: " + start + ", koniec: " + koniec + ", data: " + data;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getKoniec() {
        return koniec;
    }

    public void setKoniec(String koniec) {
        this.koniec = koniec;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
