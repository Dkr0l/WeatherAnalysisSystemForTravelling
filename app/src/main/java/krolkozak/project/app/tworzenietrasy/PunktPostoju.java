package krolkozak.project.app.tworzenietrasy;

public class PunktPostoju {
    private double szerGeog, dlugGeog;
    private String nazwa;
    private int czasPostojuMinuty;

    public PunktPostoju(double szerGeog, double dlugGeog, String nazwa, int czasPostojuMinuty) {
        this.szerGeog = szerGeog;
        this.dlugGeog = dlugGeog;
        this.nazwa = nazwa;
        this.czasPostojuMinuty = czasPostojuMinuty;
    }

    public double getSzerGeog() {
        return szerGeog;
    }

    public double getDlugGeog() {
        return dlugGeog;
    }

    public String getNazwa() {
        return nazwa;
    }

    public int getCzasPostojuMinuty() {
        return czasPostojuMinuty;
    }
}
