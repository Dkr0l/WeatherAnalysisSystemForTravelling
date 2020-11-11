package krolkozak.project.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import krolkozak.project.app.ekrany.Menu;
import krolkozak.project.app.tworzenietrasy.Mapa;

public class Ustawienia extends Activity {

    private Switch przelacznikTrybuCiemnego;

    private static boolean trybCiemnyAktywny=false;

    public static boolean trybCiemnyAktywny() {
        return trybCiemnyAktywny;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(trybCiemnyAktywny()) {
            setContentView(R.layout.trybciemnyustawienia);
        }else{
            setContentView(R.layout.ustawienia);
        }
        przelacznikTrybuCiemnego=findViewById(R.id.ustawieniaTrybCiemny);

        //wyświetlenie zapisanych ustawień w interfejsie
        przywrocenieUstawien();

        // zatwierdzenie ustawień
        findViewById(R.id.zatwierdzUstawienia).setOnClickListener(v -> {
            trybCiemnyAktywny=przelacznikTrybuCiemnego.isChecked();
            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        });
    }

    private void przywrocenieUstawien(){
        przelacznikTrybuCiemnego.setChecked(trybCiemnyAktywny());
    }

}
