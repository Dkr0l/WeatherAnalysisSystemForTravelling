package krolkozak.project.app;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import krolkozak.project.app.bazadanych.Uzytkownik;

public class DaneAplikacji extends ViewModel {

    public final MutableLiveData<Uzytkownik> uzytkownik = new MutableLiveData<>(null);

}