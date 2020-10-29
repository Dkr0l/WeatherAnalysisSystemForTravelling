package krolkozak.project.app;

public class PunkPostoju {
    private double szerGeog, dlugGeog;
    private String nazwa;
    private int czasPostojuMinuty;

    public PunkPostoju(double szerGeog, double dlugGeog, String nazwa, int czasPostojuMinuty) {
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
