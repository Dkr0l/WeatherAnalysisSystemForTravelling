package krolkozak.project.app.pomocnicze;

public class AktualnaTrasaTekst {
    private String początekTrasyTekst = "";
    private String czasWyjazduTekst = "";
    private String przystankiTekst = "";
    private String koniecTrasyTekst = "";

    public String pobierzPelnyTekstTrasy() {
        String tytul = "Aktualna trasa:";
        String poczatekTekst = początekTrasyTekst == "" ? "" : "\nPoczątek trasy: " + this.początekTrasyTekst;
        String czasWyjazduTekst = this.czasWyjazduTekst == "" ? "" : " (" + this.czasWyjazduTekst + ")";
        String przystankiTekst = this.przystankiTekst == "" ? "" : "\nPrzystanki: " + this.przystankiTekst;
        String koniecTekst = koniecTrasyTekst == "" ? "" : "\nKoniec trasy: " + this.koniecTrasyTekst;

        if (poczatekTekst == "" && czasWyjazduTekst == "" && przystankiTekst == "" && koniecTekst == "") {
            return "";
        } else {
            return tytul + poczatekTekst + (poczatekTekst == "" ? "" : czasWyjazduTekst) + przystankiTekst + koniecTekst;
        }
    }


    public String getPoczątekTrasyTekst() {
        return początekTrasyTekst;
    }

    public void setPoczątekTrasyTekst(String początekTrasyTekst) {
        this.początekTrasyTekst = początekTrasyTekst;
    }

    public String getCzasWyjazduTekst() {
        return czasWyjazduTekst;
    }

    public void setCzasWyjazduTekst(String czasWyjazduTekst) {
        this.czasWyjazduTekst = czasWyjazduTekst;
    }

    public String getPrzystankiTekst() {
        return przystankiTekst;
    }

    public void setPrzystankiTekst(String przystankiTekst) {
        this.przystankiTekst = przystankiTekst;
    }

    public String getKoniecTrasyTekst() {
        return koniecTrasyTekst;
    }

    public void setKoniecTrasyTekst(String koniecTrasyTekst) {
        this.koniecTrasyTekst = koniecTrasyTekst;
    }
}
