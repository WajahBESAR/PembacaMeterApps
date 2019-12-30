package com.wajahbesar.pembacameter.Database;

public class TablePetugas {
    private String Inisial;
    private String Nama;
    private String Haribaca;
    private String Avatar;
    private String LoginID;
    private String Tanggal;

    TablePetugas() {}

    public TablePetugas(String Inisial, String Nama, String Haribaca, String Avatar, String LoginID, String Tanggal) {
        this.Inisial = Inisial;
        this.Nama = Nama;
        this.Haribaca = Haribaca;
        this.Avatar = Avatar;
        this.LoginID = LoginID;
        this.Tanggal = Tanggal;
    }

    public String getInisial() {
        return Inisial;
    }

    void setInisial(String Inisial) {
        this.Inisial = Inisial;
    }

    public String getNama() {
        return Nama;
    }

    void setNama(String Nama) {
        this.Nama = Nama;
    }

    public String getHaribaca() {
        return Haribaca;
    }

    void setHaribaca(String Haribaca) {
        this.Haribaca = Haribaca;
    }

    public String getAvatar() {
        return Avatar;
    }

    void setAvatar(String Avatar) {
        this.Avatar = Avatar;
    }

    public String getLoginID() {
        return LoginID;
    }

    void setLoginID(String LoginID) {
        this.LoginID = LoginID;
    }

    public String getTanggal() {
        return Tanggal;
    }

    void setTanggal(String Tanggal) {
        this.Tanggal = Tanggal;
    }
}
