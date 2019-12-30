package com.wajahbesar.pembacameter.Database;

public class TablePelanggan {
    private String Nopel;
    private String Nama;
    private String Alamat;
    private String Goltar;
    private String Telepon;
    private String Metnum;
    private String Latitude;
    private String Longitude;
    private String Dibaca;

    TablePelanggan() {}

    public TablePelanggan(String Nopel, String Nama, String Alamat, String Goltar, String Telepon, String Metnum, String Latitude, String Longitude, String Dibaca) {
        this.Nopel = Nopel;
        this.Nama = Nama;
        this.Alamat = Alamat;
        this.Goltar = Goltar;
        this.Telepon = Telepon;
        this.Metnum = Metnum;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Dibaca = Dibaca;
    }

    public String getNopel() {
        return Nopel;
    }

    public void setNopel(String Nopel) {
        this.Nopel = Nopel;
    }

    public String getNama() {
        return Nama;
    }

    public void setNama(String Nama) {
        this.Nama = Nama;
    }

    public String getAlamat() {
        return Alamat;
    }

    public void setAlamat(String Alamat) {
        this.Alamat= Alamat;
    }

    public String getGoltar() {
        return Goltar;
    }

    public void setGoltar(String Goltar) {
        this.Goltar = Goltar;
    }

    public String getTelepon() {
        return Telepon;
    }

    public void setTelepon(String Telepon) {
        this.Telepon = Telepon;
    }

    public String getMetnum() {
        return Metnum;
    }

    public void setMetnum(String Metnum) {
        this.Metnum = Metnum;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String Latitude) {
        this.Latitude = Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String Longitude) {
        this.Longitude = Longitude;
    }

    public String getDibaca() {
        return Dibaca;
    }

    public void setDibaca(String Dibaca) {
        this.Dibaca = Dibaca;
    }
}
