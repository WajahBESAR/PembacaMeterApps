package com.wajahbesar.pembacameter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.squareup.picasso.Picasso;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private DatabaseHandler databaseHandler;
    private Double vLat, vLng;
    private TextView txtTanggal, txtNama, txtIni, txtHari, txtJumlah, txtDibaca, txtSisa;
    private ImageView imgBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // URGENT ONLY
        //getApplicationContext().deleteDatabase("db_meterreading.db"); // urgent only (reinstall, debug, etc)
        //finishAndRemoveTask();
        // REMARK AFTER

        databaseHandler = new DatabaseHandler(this);

        // Inisialisasi
        txtTanggal = findViewById(R.id.txtTanggal);
        txtNama = findViewById(R.id.txtNama);
        txtIni = findViewById(R.id.txtIni);
        txtHari = findViewById(R.id.txtHari);
        txtJumlah = findViewById(R.id.txtJumlah);
        txtDibaca = findViewById(R.id.txtDibaca);
        txtSisa = findViewById(R.id.txtSisa);
        imgBackground = findViewById(R.id.imgBackground);

        // Deklarasi progressdialog
        progressDialog = new ProgressDialog(MainActivity.this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        // SEMUA PERmission DIMINTA DISINI, setelah itu -> starttheactivity
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
        String rationale = "Ijinkan untuk menyimpan data ke memory, Ijinkan untuk mengetahui lokasi Anda, Ijinkan menggunakan Kamera?";
        Permissions.Options options = new Permissions.Options().setRationaleDialogTitle("Penting").setSettingsDialogTitle("Info");

        Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                StartTheActivity();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                finish();
            }
        });

        // TOMBOL SYNC
        findViewById(R.id.imgSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                int sisa = Integer.parseInt(txtSisa.getText().toString());
                if (sisa > 0) {
                    // Create a AlertDialog Builder.
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    // Set title, icon, can not cancel properties.
                    alertDialogBuilder.setTitle("Reset data");
                    alertDialogBuilder.setIcon(R.drawable.logopdam);
                    alertDialogBuilder.setCancelable(true);

                    // Set the inflated layout view object to the AlertDialog builder.
                    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                    @SuppressLint("InflateParams") View popup_sync_view = layoutInflater.inflate(R.layout.popup_sync, null);
                    Button btnReset = popup_sync_view.findViewById(R.id.btnReset);
                    alertDialogBuilder.setView(popup_sync_view);

                    // Create AlertDialog and show.
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    btnReset.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Functions(getApplicationContext()).Getar();

                            alertDialog.dismiss();

                            downloadData();
                        }
                    });
                } else {
                    downloadData();
                }
            }
        });

        // KLIK MENU NOMOR PELANGGAN
        findViewById(R.id.cardNomor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                startActivity(new Intent(MainActivity.this, ByNopel.class));
            }
        });

        // KLIK MENU LIST PELANGGAN
        findViewById(R.id.cardList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                new Functions(getApplicationContext()).showMessage(view, "Anda memilih menu ", "List Pelanggan", 5000);
                // ...
            }
        });

        // KLIK MENU PETA PELANGGAN
        findViewById(R.id.cardPeta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                new Functions(getApplicationContext()).showMessage(view, "Anda memilih menu ", "Peta Pelanggan", 5000);
                // ...
            }
        });

        // KLIK MENU UPLOAD PELANGGAN
        findViewById(R.id.cardUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                new Functions(getApplicationContext()).showMessage(view, "Anda memilih menu ", "Upload Pelanggan", 5000);
                // ...
            }
        });

        // Logout
        findViewById(R.id.imgLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("Logout?");
                alertDialogBuilder.setIcon(R.drawable.logopdam);
                alertDialogBuilder.setCancelable(true);

                // Set the inflated layout view object to the AlertDialog builder.
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                @SuppressLint("InflateParams") View popup_setting_view = layoutInflater.inflate(R.layout.popup_logout, null);
                Button btnLogout = popup_setting_view.findViewById(R.id.btnLogout);
                alertDialogBuilder.setView(popup_setting_view);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Functions(getApplicationContext()).Getar();

                        alertDialog.dismiss();

                        databaseHandler.emptyPetugas();

                        progressDialog.show();

                        String strToken = ((GlobalVars) getApplicationContext()).getApiToken();
                        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url = ambilAPISetting()
                                + "/?action=kick&init=" + txtIni.getText()
                                + "&token=" + strToken;

                        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("LOGOUT", response);

                                progressDialog.dismiss();

                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("LOGOUT", error.toString());

                                progressDialog.dismiss();

                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();
                            }
                        });

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void StartTheActivity() {
        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    Log.d("My Current location", "Lat : " + location.getLatitude() + " Long : " + location.getLongitude());
                    vLat = location.getLatitude();
                    vLng = location.getLongitude();
                }
            }
        });
        // --------------------

        // BUAT FOLDER KALO BELUM ADA
        createFolders();
        // --------------------------

        // CEK LOGIN
        // cek inisial petugas di database
        List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
        String currentActivePetugas_Inisial = "";
        for(TablePetugas tablePetugas: tablePetugasList) {
            currentActivePetugas_Inisial = tablePetugas.getInisial();
        }

        // if not logged in
        if(currentActivePetugas_Inisial.length() != 3){
            // ke halaman Login
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        } else {
            if(ambilAPISetting().length() < 15){
                new Functions(this).showMessage(findViewById(R.id.main_root), "Terjadi kesalahan setting server, silahkan setting ulang!", "", 3000);
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            } else {
                validLogin();
            }
        }
    }

    private void bacaDataBase() {
        // jumlah all pelanggan
        int pelJumlah = databaseHandler.countPelanggan("0");
        txtJumlah.setText(String.valueOf(pelJumlah));
        int pelDibaca = databaseHandler.countPelanggan("1");
        txtDibaca.setText(String.valueOf(pelDibaca));
        txtSisa.setText(String.valueOf(pelJumlah - pelDibaca));
    }

    private void downloadData() {
        // perlu dipertimbangkan, ketika download data apakah existing data (kalo ada termasuk data expired) perlu di selamatkan? pake dialog box
        // kalo ada yg belum upload, minta diselesaikan upload. atau tetap di reset diganti data baru.
        // ada toleransi 1 hari. kalau setelah login ternyata ada data kemarin, bisa saja di baca dulu sampe habis baru sync untuk dapat data baru
        // dialog box akan muncul hanya kalo ada data yg sudah dibaca (belum upload). kalo dibaca = 0, maka babat habis aja. ganti baru.

        progressDialog.show();

        // kosongkan isi 2 tables ini
        databaseHandler.emptyPelanggan();
        databaseHandler.emptyBacaan();

        bacaDataBase();

        String api_token = ((GlobalVars) getApplication()).getApiToken();
        @SuppressLint("HardwareIds") final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String str_ini = txtIni.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final String url = ambilAPISetting()
                + "/?action=download&init=" + str_ini
                + "&logid=" + androidId
                + "&token=" + api_token + "";

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //1278-1001,AHMAD SAPAR,KB KOPI 38 RT/RW 1/6,R6,,14028478,-6.59116288,106.78451363 ;
                if (response.length() > 20) {
                    String[] arrData = response.split(";");
                    for (String line: arrData){
                        String[] field = line.split(",");
                        String strNopel = field[0].trim().replace("-", "");
                        String strNama = field[1].trim();
                        String strAlamat = field[2].trim();
                        String strGoltar = field[3].trim();
                        String strTelp = field[4].trim();
                        String strMetnum = field[5].trim();
                        String strLat = field[6].trim();
                        String strLon = field[7].trim();
                        try {
                            databaseHandler.addPelanggan(new TablePelanggan(strNopel, strNama, strAlamat, strGoltar, strTelp, strMetnum, strLat, strLon, "0"));
                            bacaDataBase();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Save Pelanggan: ", strNopel);
                        }
                    }
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("syncData", error.toString());
                progressDialog.dismiss();
                Log.e("URL", url);
                Log.e("AndroidID", androidId);
                new Functions(getApplicationContext()).showMessage(findViewById(R.id.main_root), "Koneksi dengan server terputus, coba lagi nanti!", error.toString() + "_" + androidId, 5000);
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String ambilAPISetting() {
        List<TableSetting> tableSettingList = databaseHandler.bukaSetting();
        String currentAPISetting = "";
        for(TableSetting tableSetting: tableSettingList) {
            currentAPISetting = tableSetting.getURLAPI();
        }

        return currentAPISetting;
    }

    private void validLogin(){
        final String api_token = ((GlobalVars) getApplication()).getApiToken();
        final String url_saved = ambilAPISetting();

        // cek koneksi
        String serverRequest = ambilAPISetting() + "/?action=ping&token=" + api_token;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                if(response.equals("pong")) {

                    // LOGIN
                    @SuppressLint("HardwareIds") final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    String init = "";
                    List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
                    for(TablePetugas tablePetugas: tablePetugasList) {
                        init = tablePetugas.getInisial();
                    }

                    final String serverRequest2 = url_saved
                            + "/?action=login&init=" + init
                            + "&logid=" + androidId
                            + "&lat=" + vLat
                            + "&lng=" + vLng
                            + "&token=" + api_token;

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest2, new Response.Listener<String>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(String response) {
                            if(response.substring(0, 2).equals("ok")) {
                                // ok,JOK,JOKO HARDIANTO,29,/avatar/,2019-10-30 02.43.02 Rabu
                                String[] dtSource;
                                String dtSourceSplitted, hari, tanggal, bln, tahun;
                                dtSource = response.split(",");

                                dtSourceSplitted = dtSource[5];
                                hari = dtSourceSplitted.substring(20);
                                tanggal = dtSourceSplitted.substring(8, 10);
                                bln = dtSourceSplitted.substring(5,7);
                                tahun = dtSourceSplitted.substring(0,4);
                                String[] bulan = new String[12];
                                bulan[0] = "Januari";
                                bulan[1] = "Februari";
                                bulan[2] = "Maret";
                                bulan[3] = "April";
                                bulan[4] = "Mei";
                                bulan[5] = "Juni";
                                bulan[6] = "Juli";
                                bulan[7] = "Agustus";
                                bulan[8] = "September";
                                bulan[9] = "Oktober";
                                bulan[10] = "Nopember";
                                bulan[11] = "Desember";
                                txtTanggal.setText(hari + ", " + tanggal + " " + bulan[Integer.parseInt(bln) - 1] + " " + tahun);
                                txtNama.setText(dtSource[2]);
                                txtIni.setText(dtSource[1]);
                                txtHari.setText(dtSource[3]);

                                databaseHandler.updatePetugas(dtSource[2], dtSource[3], dtSource[4], dtSource[1], dtSource[5]);
                                Picasso.get().load(ambilAPISetting() + dtSource[4]).error(R.drawable.watermeter).into(imgBackground);

                                bacaDataBase();

                                // tracker
                                goTrakcMe(dtSource[1]);
                            } else {
                                new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Anda telah keluar dari login , silahkan login ulang!", response, 5000);
                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Tidak terhubung dengan server", error.getMessage(), 5000);
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                    // ---==

                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Tidak terhubung dengan server", response, 5000);
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }

                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Tidak terhubung dengan server", error.getMessage(), 5000);
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void createFolders() {
        File MeterReadingDir = new File(Environment.getExternalStorageDirectory(), "PembacaMeter");
        if (!MeterReadingDir.exists()) {
            if (!MeterReadingDir.mkdirs()) {
                Log.i("FOLDER", "Gagal bikin folder PembacaMeter");
            }
        }
        File PhotoDir = new File(Environment.getExternalStorageDirectory(), "PembacaMeter/Photo");
        if (!PhotoDir.exists()) {
            if (!PhotoDir.mkdirs()) {
                Log.i("FOLDER", "Gagal bikin folder PembacaMeter/Photo");
            }
        }
    }

    private void goTrakcMe(String init) {
        String api_token = ((GlobalVars) this.getApplication()).getApiToken();
        String serverRequest = ambilAPISetting() + "/?action=logger&init=" + init + "&lat=" + vLat + "&lon=" + vLng + "&act=LOGIN&token=" + api_token;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onErrorResponse(VolleyError error) {}
        });
        queue.add(stringRequest);
    }
}
