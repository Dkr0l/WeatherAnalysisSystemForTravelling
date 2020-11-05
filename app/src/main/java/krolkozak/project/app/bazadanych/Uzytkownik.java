package krolkozak.project.app.bazadanych;

public class Uzytkownik {
    String id, login, email, data_utworzenia;

    public Uzytkownik() {
    }

    public Uzytkownik(String id, String login, String email, String data_utworzenia) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.data_utworzenia = data_utworzenia;
    }

    public String pobierzPelneDane() {
        return this.id + ", " + this.login + ", " + this.email + ", " + this.data_utworzenia;
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
