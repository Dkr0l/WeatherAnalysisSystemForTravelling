package krolkozak.project.app.bazadanych;

import java.io.Serializable;

public class Historia implements Serializable {
    private String id_uzytkownika;
    private String lokalizacja_poczatkowa;
    private String lokalizacja_koncowa;
    private String czas_wyjazdu;
    private String miejsca_pogodowe;
    private String typ_trasy;
    private String punkty_trasy;

    public Historia() {
    }

    public Historia(String id_uzytkownika, String lokalizacja_poczatkowa, String lokalizacja_koncowa, String czas_wyjazdu, String miejsca_pogodowe, String typ_trasy, String punkty_trasy) {
        this.id_uzytkownika = id_uzytkownika;
        this.lokalizacja_poczatkowa = lokalizacja_poczatkowa;
        this.lokalizacja_koncowa = lokalizacja_koncowa;
        this.czas_wyjazdu = czas_wyjazdu;
        this.miejsca_pogodowe = miejsca_pogodowe;
        this.typ_trasy = typ_trasy;
        this.punkty_trasy = punkty_trasy;
    }

    public String pobierzObiekt() {
        return "id_uzytkownika: " + id_uzytkownika + ", lokalizacja_poczatkowa: " + lokalizacja_poczatkowa + ", lokalizacja_koncowa: " + lokalizacja_koncowa
                + ", czas_wyjazdu: " + czas_wyjazdu + ", miejsca_pogodowe: " + miejsca_pogodowe + ", typ_trasy: " + typ_trasy + ", punkty_trasy: " + punkty_trasy;
    }

    public String getId_uzytkownika() {
        return id_uzytkownika;
    }

    public void setId_uzytkownika(String id_uzytkownika) {
        this.id_uzytkownika = id_uzytkownika;
    }

    public String getLokalizacja_poczatkowa() {
        return lokalizacja_poczatkowa;
    }

    public void setLokalizacja_poczatkowa(String lokalizacja_poczatkowa) {
        this.lokalizacja_poczatkowa = lokalizacja_poczatkowa;
    }

    public String getLokalizacja_koncowa() {
        return lokalizacja_koncowa;
    }

    public void setLokalizacja_koncowa(String lokalizacja_koncowa) {
        this.lokalizacja_koncowa = lokalizacja_koncowa;
    }

    public String getCzas_wyjazdu() {
        return czas_wyjazdu;
    }

    public void setCzas_wyjazdu(String czas_wyjazdu) {
        this.czas_wyjazdu = czas_wyjazdu;
    }

    public String getMiejsca_pogodowe() {
        return miejsca_pogodowe;
    }

    public void setMiejsca_pogodowe(String miejsca_pogodowe) {
        this.miejsca_pogodowe = miejsca_pogodowe;
    }

    public String getTyp_trasy() {
        return typ_trasy;
    }

    public void setTyp_trasy(String typ_trasy) {
        this.typ_trasy = typ_trasy;
    }

    public String getPunkty_trasy() {
        return punkty_trasy;
    }

    public void setPunkty_trasy(String punkty_trasy) {
        this.punkty_trasy = punkty_trasy;
    }
}