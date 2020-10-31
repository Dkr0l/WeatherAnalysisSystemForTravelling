package krolkozak.project.app;

public class Uzytkownik {
    String login, haslo, email, data_utworzenia;

    public Uzytkownik() {
    }

    public Uzytkownik(String login, String haslo, String email, String data_utworzenia) {
        this.login = login;
        this.haslo = haslo;
        this.email = email;
        this.data_utworzenia = data_utworzenia;
    }

    public String pobierzPelneDane() {
        return this.login + ", " + this.haslo + ", " + this.email + ", " + this.data_utworzenia;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHaslo() {
        return haslo;
    }

    public void setHaslo(String haslo) {
        this.haslo = haslo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getData_utworzenia() {
        return data_utworzenia;
    }

    public void setData_utworzenia(String data_utworzenia) {
        this.data_utworzenia = data_utworzenia;
    }

}
