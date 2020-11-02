package krolkozak.project.app;

public class Uzytkownik {
    String id, login, haslo, email, data_utworzenia;

    public Uzytkownik() {
    }

    public Uzytkownik(String id, String login, String haslo, String email, String data_utworzenia) {
        this.id = id;
        this.login = login;
        this.haslo = haslo;
        this.email = email;
        this.data_utworzenia = data_utworzenia;
    }

    public String pobierzPelneDane() {
        return this.id + ", " + this.login + ", " + this.haslo + ", " + this.email + ", " + this.data_utworzenia;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
