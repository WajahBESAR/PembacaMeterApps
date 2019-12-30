package com.wajahbesar.pembacameter.Database;

import java.util.Calendar;
import java.util.Objects;

public class TableBacaan {
    private String Nopel;
    private String Stand;
    private String Catatan;
    private String Keterangan;
    private String Tanggal;

    TableBacaan() {}

    public TableBacaan(String Nopel, String Stand, String Catatan, String Keterangan, String Tanggal) {
        this.Nopel = Nopel;
        this.Stand = Stand;
        this.Catatan = Catatan;
        this.Keterangan = Keterangan;
        this.Tanggal = Tanggal;
    }

    public String getNopel() {
        return Nopel;
    }

    public void setNopel(String Nopel) {
        this.Nopel = Nopel;
    }

    public String getStand() {
        return Stand;
    }

    public void setStand(String Stand) {
        this.Stand = Stand;
    }

    public String getCatatan() {
        return Catatan;
    }

    public void setCatatan(String Catatan) {
        this.Catatan = Catatan;
    }

    public String getKeterangan() {
        return Keterangan;
    }

    public void setKeterangan(String Keterangan) {
        this.Keterangan = Keterangan;
    }

    public String getTanggal() {
        return Tanggal;
    }

    public void setTanggal(String Tanggal) {
        this.Tanggal = Tanggal;
    }
}
