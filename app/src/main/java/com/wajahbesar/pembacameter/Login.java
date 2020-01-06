package com.wajahbesar.pembacameter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TableCatatan;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.util.List;
import java.util.Objects;

public class Login extends AppCompatActivity {

    private DatabaseHandler databaseHandler;
    private TextView txtConnected;
    private ProgressDialog progressDialog;
    private Double vLat, vLng;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TRANSPARENT NOTIFBAR
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);

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

        txtConnected = findViewById(R.id.txtConnected);

        databaseHandler = new DatabaseHandler(this);

        progressDialog = new ProgressDialog(Login.this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        // Baca database inisial (login)
        if((ambilAPISetting().equals("")) || (ambilAPISetting() == null)){
            txtConnected.setText("< Disconnected >");
        } else {
            // Check connection
            startAPIConnection();
        }

        // Klik login
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                EditText edtLogin = findViewById(R.id.edtLogin);

                if (edtLogin.getText().length() == 3) {
                    String connStat = txtConnected.getText().toString();

                    if (connStat.equals("< Connected >")) {

                        progressDialog.show();

                        @SuppressLint("HardwareIds") final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                        final String strToken = ((GlobalVars) getApplicationContext()).getApiToken();
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url = ambilAPISetting()
                                + "/?action=login&init=" + edtLogin.getText().toString()
                                + "&logid=" + androidId
                                + "&lat=" + vLat
                                + "&lng=" + vLng
                                + "&token=" + strToken;

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("LOGIN", response);

                                if(response.length() > 3){
                                    if(response.equals("already-login")){
                                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Login gagal, hanya diperbolehkan login pada satu device!", "", 5000);
                                    }else if(response.substring(0, 2).equals("ok")) {
                                        databaseHandler.emptyPetugas();
                                        // "ok,JOK,JOKO HARDIANTO,29,/avatar/,2019-10-30 06.02.51 Rabu"
                                        String[] arrResponse = response.split(",");
                                        databaseHandler.addPetugas(new TablePetugas(arrResponse[1], arrResponse[2], arrResponse[3], arrResponse[4], androidId, arrResponse[5]));

                                        // download catatan dari server ke db
                                        RequestQueue strQueue = Volley.newRequestQueue(getApplicationContext());
                                        String urlReq = ambilAPISetting() + "/?action=notes&token=" + strToken + "";
                                        StringRequest strReq = new StringRequest(Request.Method.GET, urlReq, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (response.length() > 50) {
                                                    String[] arrCat = response.split(";");
                                                    databaseHandler.emptyCatatan();
                                                    for (String rowCat: arrCat){
                                                        String[] field = rowCat.split(",");
                                                        databaseHandler.addCatatan(new TableCatatan(field[0], field[1]));
                                                    }
                                                } else {
                                                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Error download Catatan", "", Snackbar.LENGTH_SHORT);
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Error", error.getMessage(), Snackbar.LENGTH_SHORT);
                                            }
                                        });

                                        strQueue.add(strReq);

                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    }else{
                                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Login gagal, Inisial tidak ada!", "", Snackbar.LENGTH_SHORT);
                                    }
                                } else{
                                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Login gagal, silahkan ulangi lagi!", "", Snackbar.LENGTH_SHORT);
                                }
                                progressDialog.dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("LOGIN", error.toString());
                                progressDialog.dismiss();
                                new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Error", error.getMessage(), Snackbar.LENGTH_SHORT);
                            }
                        });

                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    } else {
                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Tidak tersambung dengan server, silahkan setting alamat server", "", 3000);
                        //txtConnected.callOnClick();
                    }
                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Inisial petugas mohon diisi! ", "[3 huruf]", 3000);
                }
            }
        });

        // Klik setting
        txtConnected.setText("< Disconnected >");
        txtConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Login.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("Setting API Server");
                alertDialogBuilder.setIcon(R.drawable.setting);
                alertDialogBuilder.setCancelable(true);

                // Set the inflated layout view object to the AlertDialog builder.
                LayoutInflater layoutInflater = LayoutInflater.from(Login.this);
                @SuppressLint("InflateParams")
                final View popup_setting_view = layoutInflater.inflate(R.layout.popup_setting, null);
                Button btnSaveURL = popup_setting_view.findViewById(R.id.btnSaveURL);
                final EditText txtURL = popup_setting_view.findViewById(R.id.txtURL);

                // isi textConfig
                txtURL.setText(ambilAPISetting());

                alertDialogBuilder.setView(popup_setting_view);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                btnSaveURL.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {
                        new Functions(getApplicationContext()).Getar();
                        try {
                            // Save setting
                            databaseHandler.emptySetting();
                            databaseHandler.addSetting(new TableSetting(String.valueOf(txtURL.getText())));
                            //new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Setting URL API berhasil disimpan!", "", 5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), e.getMessage(),"", 5000);
                        }

                        alertDialog.dismiss();
                        startAPIConnection();
                    }
                });
            }
        });
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void startAPIConnection() {
        String api_token = ((GlobalVars) this.getApplication()).getApiToken();
        String serverRequest = ambilAPISetting() + "/?action=ping&token=" + api_token;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                if(response.equals("pong")) {
                    txtConnected.setText("< Connected >");
                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.root_login), "Request time out", response, 5000);
                }
            }
        }, new Response.ErrorListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                txtConnected.setText("< Disconnected >");
                Log.e("CONNECT", Objects.requireNonNull(error.getMessage()));
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String ambilAPISetting() {
        String tempApiSetting = "";
        List<TableSetting> tableSettingList = databaseHandler.bukaSetting();
        if(tableSettingList.size() > 0) {
            for (TableSetting tableSetting : tableSettingList) {
                tempApiSetting = tableSetting.getURLAPI();
            }
        }
        return tempApiSetting;
    }
}
