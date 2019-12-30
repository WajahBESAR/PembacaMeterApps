package com.wajahbesar.pembacameter.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.appcompat.widget.FitWindowsViewGroup;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "db_meterreading.db";
    private static final int DATABASE_VERSION = 1;

    // Deklarasi table setting
    private static final String TABLE_SETTING = "t_setting";
    private static final String FIELD_SETTING_URLAPI = "f_setting_urlapi";
    // Query create table setting
    private static final String CREATE_TABLE_SETTING = "CREATE TABLE IF NOT EXISTS " + TABLE_SETTING + "(" + FIELD_SETTING_URLAPI + " TEXT);";

    // Deklarai table petugas
    private static final String TABLE_PETUGAS = "t_petugas";
    private static final String FIELD_PETUGAS_INISIAL = "f_petugas_inisial";
    private static final String FIELD_PETUGAS_NAMA = "f_petugas_nama";
    private static final String FIELD_PETUGAS_AVATAR = "f_petugas_avatar";
    private static final String FIELD_PETUGAS_HARIBACA = "f_petugas_haribaca";
    private static final String FIELD_PETUGAS_LOGINID = "f_petugas_loginid";
    private static final String FIELD_PETUGAS_TANGGAL = "f_petugas_tanggal";
    // Query create table petugas
    private static final String CREATE_TABLE_PETUGAS = "CREATE TABLE IF NOT EXISTS " + TABLE_PETUGAS + "(" + FIELD_PETUGAS_INISIAL + " TEXT, " + FIELD_PETUGAS_NAMA + " TEXT, "
            + FIELD_PETUGAS_HARIBACA + " TEXT, " + FIELD_PETUGAS_AVATAR + " TEXT, " + FIELD_PETUGAS_LOGINID + " TEXT, " + FIELD_PETUGAS_TANGGAL + " TEXT);";

    // Deklarasi table pelanggan
    private static final String TABLE_PELANGGAN = "t_pelanggan";
    private static final String FIELD_PELANGGAN_NOPEL = "f_pelanggan_nopel";
    private static final String FIELD_PELANGGAN_NAMA = "f_pelanggan_nama";
    private static final String FIELD_PELANGGAN_ALAMAT = "f_pelanggan_alamat";
    private static final String FIELD_PELANGGAN_GOLTAR = "f_pelanggan_goltar";
    private static final String FIELD_PELANGGAN_TELEPON = "f_pelanggan_telepon";
    private static final String FIELD_PELANGGAN_METNUM = "f_pelanggan_metnum";
    private static final String FIELD_PELANGGAN_LATITUDE = "f_pelanggan_latitude";
    private static final String FIELD_PELANGGAN_LONGITUDE = "f_pelanggan_longitude";
    private static final String FIELD_PELANGGAN_DIBACA = "f_pelanggan_dibaca";
    // Query create table pelanggan
    private static final String CREATE_TABLE_PELANGGAN = "CREATE TABLE IF NOT EXISTS " + TABLE_PELANGGAN + "(" + FIELD_PELANGGAN_NOPEL + " TEXT, " + FIELD_PELANGGAN_NAMA + " TEXT, "
            + FIELD_PELANGGAN_ALAMAT + " TEXT, " + FIELD_PELANGGAN_GOLTAR + " TEXT, " + FIELD_PELANGGAN_TELEPON + " TEXT, " + FIELD_PELANGGAN_METNUM + " TEXT, "
            + FIELD_PELANGGAN_LATITUDE + " TEXT, " + FIELD_PELANGGAN_LONGITUDE + " TEXT, " + FIELD_PELANGGAN_DIBACA + " TEXT);";

    // Deklarasi table bacaan
    private static final String TABLE_BACAAN = "t_bacaan";
    private static final String FIELD_BACAAN_NOPEL = "f_bacaan_nopel";
    private static final String FIELD_BACAAN_STAND = "f_bacaan_stand";
    private static final String FIELD_BACAAN_CATATAN = "f_bacaan_catatan";
    private static final String FIELD_BACAAN_KETERANGAN = "f_bacaan_keterangan";
    private static final String FIELD_BACAAN_TANGGAL = "f_bacaan_tanggal";
    // Query create table bacaan
    private static final String CREATE_TABLE_BACAAN = "CREATE TABLE IF NOT EXISTS " + TABLE_BACAAN + "(" + FIELD_BACAAN_NOPEL + " TEXT, " + FIELD_BACAAN_STAND + " TEXT, "
            + FIELD_BACAAN_CATATAN + " TEXT, " + FIELD_BACAAN_KETERANGAN + " TEXT, " + FIELD_BACAAN_TANGGAL + " TEXT);";

    // Deklarasi table catatan
    private static final String TABLE_CATATAN = "t_catatan";
    private static final String FIELD_CATATAN_KODE = "f_catatan_kode";
    private static final String FIELD_CATATAN_KETERANGAN = "f_catatan_keterangan";
    // Query create table catatan
    private static final String CREATE_TABLE_CATATAN = "CREATE TABLE IF NOT EXISTS " + TABLE_CATATAN + " (" + FIELD_CATATAN_KODE + " TEXT, " + FIELD_CATATAN_KETERANGAN + " TEXT);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create table petugas
        database.execSQL(CREATE_TABLE_PETUGAS);
        // Create table setting
        database.execSQL(CREATE_TABLE_SETTING);
        // Create table pelanggan
        database.execSQL(CREATE_TABLE_PELANGGAN);
        // Create table bacaan
        database.execSQL(CREATE_TABLE_BACAAN);
        // Create table catatan
        database.execSQL(CREATE_TABLE_CATATAN);

        Log.i("TABLE", "onCREATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop table petugas
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PETUGAS);
        // Drop table setting
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTING);
        // Drop table pelanggan
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PELANGGAN);
        // Drop table bacaan
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_BACAAN);
        // Drop table catatan
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CATATAN);

        // Execute Drop
        onCreate(database);
    }

    // Add setting
    public void addSetting(TableSetting tableSetting) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_SETTING_URLAPI, tableSetting.getURLAPI());

        database.insert(TABLE_SETTING, null, contentValues);
        database.close();
    }

    // Baca setting
    public List<TableSetting> bukaSetting() {
        List<TableSetting> tableSettingList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_SETTING;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                TableSetting setting = new TableSetting();
                setting.setURLAPI(cursor.getString(0));
                tableSettingList.add(setting);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return tableSettingList;
    }

    // Empty setting
    public void emptySetting() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_SETTING);
        database.close();
    }

    // Add petugas
    public void addPetugas(TablePetugas tablePetugas) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_PETUGAS_INISIAL, tablePetugas.getInisial());
        contentValues.put(FIELD_PETUGAS_NAMA, tablePetugas.getNama());
        contentValues.put(FIELD_PETUGAS_HARIBACA, tablePetugas.getHaribaca());
        contentValues.put(FIELD_PETUGAS_AVATAR, tablePetugas.getAvatar());
        contentValues.put(FIELD_PETUGAS_LOGINID, tablePetugas.getLoginID());
        contentValues.put(FIELD_PETUGAS_TANGGAL, tablePetugas.getTanggal());

        database.insert(TABLE_PETUGAS, null, contentValues);
        database.close();
    }

    // Cari petugas (inisial) -> BELUM KEPAKE
//    public TablePetugas searchPetugas(String inisial) {
//        SQLiteDatabase database = this.getReadableDatabase();
//        Cursor cursor = database.query(
//                TABLE_PETUGAS,
//                new String[]{FIELD_PETUGAS_ID, FIELD_PETUGAS_INISIAL, FIELD_PETUGAS_NAMA, FIELD_PETUGAS_HARIBACA, FIELD_PETUGAS_AVATAR, FIELD_PETUGAS_LOGINID},
//                FIELD_PETUGAS_INISIAL + "=?",
//                new String[]{String.valueOf(inisial)},
//                null,
//                null,
//                null
//        );
//
//        if(cursor != null){
//            cursor.moveToFirst();
//        }
//
//        return new TablePetugas(Integer.parseInt(Objects.requireNonNull(cursor).getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
//    }

//    public String getInitial() {
//        String strResult = "";
//        SQLiteDatabase database = this.getReadableDatabase();
//        Cursor cursor = database.rawQuery("SELECT " + FIELD_PETUGAS_INISIAL + " FROM " + TABLE_PETUGAS, null, null);
//        if(cursor != null){
//            cursor.moveToFirst();
//            strResult = cursor.getString(0);
//        }
//        if(cursor != null){
//            cursor.close();
//        }
//
//        return strResult;
//    }

    // List petugas
    public List<TablePetugas> selectPetugas(){
        List<TablePetugas> tablePetugasList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PETUGAS;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                TablePetugas petugas = new TablePetugas();
                petugas.setInisial(cursor.getString(0));
                petugas.setNama(cursor.getString(1));
                petugas.setHaribaca(cursor.getString(2));
                petugas.setAvatar(cursor.getString(3));
                petugas.setLoginID(cursor.getString(4));
                petugas.setTanggal(cursor.getString(5));
                tablePetugasList.add(petugas);
            }while(cursor.moveToNext());
        }
        cursor.close();

        return tablePetugasList;
    }

    // Update petugas
    public void updatePetugas(String nama, String hari, String avatar, String ini, String tgl){
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_PETUGAS_NAMA, nama);
        contentValues.put(FIELD_PETUGAS_HARIBACA, hari);
        contentValues.put(FIELD_PETUGAS_AVATAR, avatar);
        contentValues.put(FIELD_PETUGAS_TANGGAL, tgl);

        database.update(TABLE_PETUGAS, contentValues, FIELD_PETUGAS_INISIAL + "=?", new String[]{ini});

        database.close();
    }

    // Delete petugas
    public void deletePetugas(TablePetugas petugas){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_PETUGAS, FIELD_PETUGAS_INISIAL + "=?", new String[]{String.valueOf(petugas.getInisial())});
        database.close();
    }

    // Empty petugas
    public void emptyPetugas() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_PETUGAS);
        database.close();
    }

    // Add pelanggan
    public void addPelanggan(TablePelanggan tablePelanggan){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_PELANGGAN_NOPEL, tablePelanggan.getNopel());
        contentValues.put(FIELD_PELANGGAN_NAMA, tablePelanggan.getNama());
        contentValues.put(FIELD_PELANGGAN_ALAMAT, tablePelanggan.getAlamat());
        contentValues.put(FIELD_PELANGGAN_GOLTAR, tablePelanggan.getGoltar());
        contentValues.put(FIELD_PELANGGAN_TELEPON, tablePelanggan.getTelepon());
        contentValues.put(FIELD_PELANGGAN_METNUM, tablePelanggan.getMetnum());
        contentValues.put(FIELD_PELANGGAN_LATITUDE, tablePelanggan.getLatitude());
        contentValues.put(FIELD_PELANGGAN_LONGITUDE, tablePelanggan.getLongitude());
        contentValues.put(FIELD_PELANGGAN_DIBACA, tablePelanggan.getDibaca());

        database.insert(TABLE_PELANGGAN, null, contentValues);
        database.close();
    }

    // List pelanggan
    public List<TablePelanggan> selectPelanggan() {
        List<TablePelanggan> tablePelangganList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PELANGGAN;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do{
                TablePelanggan pelanggan = new TablePelanggan();
                pelanggan.setNopel(cursor.getString(0));
                pelanggan.setNama(cursor.getString(1));
                pelanggan.setAlamat(cursor.getString(2));
                pelanggan.setGoltar(cursor.getString(3));
                pelanggan.setTelepon(cursor.getString(4));
                pelanggan.setMetnum(cursor.getString(5));
                pelanggan.setLatitude(cursor.getString(6));
                pelanggan.setLongitude(cursor.getString(7));
                pelanggan.setDibaca(cursor.getString(8));
                tablePelangganList.add(pelanggan);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return tablePelangganList;
    }

    // Search pelanggan
    public List<TablePelanggan> searchPelanggan(String field, String data) {
        String fieldName = (field.equals("nopel")) ? FIELD_PELANGGAN_NOPEL : FIELD_PELANGGAN_METNUM;

        List<TablePelanggan> tablePelangganList = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery( "SELECT * FROM " + TABLE_PELANGGAN + " WHERE " + fieldName + "=?", new String[]{data});

        if(cursor.moveToFirst()) {
            do{
                TablePelanggan pelanggan = new TablePelanggan();
                pelanggan.setNopel(cursor.getString(0));
                pelanggan.setNama(cursor.getString(1));
                pelanggan.setAlamat(cursor.getString(2));
                pelanggan.setGoltar(cursor.getString(3));
                pelanggan.setTelepon(cursor.getString(4));
                pelanggan.setMetnum(cursor.getString(5));
                pelanggan.setLatitude(cursor.getString(6));
                pelanggan.setLongitude(cursor.getString(7));
                pelanggan.setDibaca(cursor.getString(8));
                tablePelangganList.add(pelanggan);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return tablePelangganList;
    }

    // Update pelanggan
    public void updatePelanggan(String dibaca, String nopel) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_PELANGGAN_DIBACA, dibaca);

        database.update(TABLE_PELANGGAN, contentValues, FIELD_PELANGGAN_NOPEL + "=?", new String[]{nopel});

        database.close();
    }

    // Empty pelanggan
    public void emptyPelanggan() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_PELANGGAN);
        database.close();
    }

    // Hitung pelanggan
    public int countPelanggan(String dibaca) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(" + FIELD_PELANGGAN_NOPEL + ") FROM " + TABLE_PELANGGAN + " WHERE " + FIELD_PELANGGAN_DIBACA + "=?", new String[]{dibaca});

        int count = 0;
        if (null != cursor) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        database.close();
        return count;
    }

    // Add bacaan
    public void addBacaan(TableBacaan tableBacaan) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_BACAAN_NOPEL, tableBacaan.getNopel());
        contentValues.put(FIELD_BACAAN_STAND, tableBacaan.getStand());
        contentValues.put(FIELD_BACAAN_CATATAN, tableBacaan.getCatatan());
        contentValues.put(FIELD_BACAAN_KETERANGAN, tableBacaan.getKeterangan());
        contentValues.put(FIELD_BACAAN_TANGGAL, tableBacaan.getTanggal());

        database.insert(TABLE_BACAAN, null, contentValues);
        database.close();
    }

    // Empty bacaan
    public void emptyBacaan() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_BACAAN);
        database.close();
    }

    // Add catatan
    public void addCatatan(TableCatatan tableCatatan) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_CATATAN_KODE, tableCatatan.getKode());
        contentValues.put(FIELD_CATATAN_KETERANGAN, tableCatatan.getKeterangan());

        database.insert(TABLE_CATATAN, null, contentValues);
        database.close();
    }

    // empty catatan
    public void emptyCatatan() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_CATATAN);
        database.close();
    }

    // List catatan
    public List<TableCatatan> selectCatatan() {
        List<TableCatatan> tableCatatanList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CATATAN;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do{
                TableCatatan catatan = new TableCatatan();
                catatan.setKode(cursor.getString(0));
                catatan.setKeterangan(cursor.getString(1));
                tableCatatanList.add(catatan);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return tableCatatanList;
    }
}
