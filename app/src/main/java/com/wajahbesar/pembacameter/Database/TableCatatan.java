package com.wajahbesar.pembacameter.Database;

public class TableCatatan {
    private String Kode;
    private String Keterangan;

    TableCatatan() {}

    public TableCatatan(String Kode, String Keterangan) {
        this.Kode = Kode;
        this.Keterangan = Keterangan;
    }

    public String getKode() {
        return Kode;
    }

    public void setKode(String Kode) {
        this.Kode = Kode;
    }

    public String getKeterangan() {
        return Keterangan;
    }

    public void setKeterangan(String Keterangan) {
        this.Keterangan = Keterangan;
    }
}
