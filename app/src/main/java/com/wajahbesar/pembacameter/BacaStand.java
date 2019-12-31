package com.wajahbesar.pembacameter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TableBacaan;
import com.wajahbesar.pembacameter.Database.TableCatatan;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Utilities.Functions;
import com.wajahbesar.pembacameter.Utilities.VolleyMultipartRequest;
import com.wajahbesar.pembacameter.Utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.wajahbesar.pembacameter.Login.setWindowFlag;

public class BacaStand extends AppCompatActivity {

    private DatabaseHandler databaseHandler;
    private Animator currentAnimator;
    private int shortAnimationDuration;

    private ProgressDialog progressDialog;
    private String extNopel = "";
    private String extFilename = "";
    private String extStand = "";
    private Double vLat, vLng;

    private String parent;

    EditText edtBacaStandStand;
    EditText edtBacaStandCatatan;
    EditText edtBacaStandKeterangan;
    Button btnBacaStandSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_stand);

        // TRANSPARENT NOTIFBAR
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        progressDialog = new ProgressDialog(this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    vLat = location.getLatitude();
                    vLng = location.getLongitude();
                }
            }
        });
        // --------------------

        TextView txtHistoryBacaan = findViewById(R.id.txtHistoryBacaan);
        txtHistoryBacaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                String api_token = ((GlobalVars) getApplication()).getApiToken();
                String serverRequest = ambilAPISetting() + "/?action=history&nopel=" + extNopel + "&token=" + api_token;
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                progressDialog.show();

                StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(BacaStand.this);
                        builder.setTitle("History pembacaan");
                        builder.setIcon(R.drawable.logopdam);
                        builder.setMessage(response);
                        builder.show();
                    }
                }, new Response.ErrorListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(BacaStand.this);
                        builder.setTitle("History pembacaan");
                        builder.setIcon(R.drawable.logopdam);
                        builder.setMessage(error.getMessage());
                        builder.show();
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);

            }
        });

        edtBacaStandStand = findViewById(R.id.edtBacaStandStand);
        edtBacaStandCatatan = findViewById(R.id.edtBacaStandCatatan);
        edtBacaStandCatatan.setText("0");
        edtBacaStandCatatan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    showSelectCatatan();
                }
            }
        });
        edtBacaStandCatatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                //tampilkan select catatan
                showSelectCatatan();
            }
        });
        edtBacaStandKeterangan = findViewById(R.id.edtBacaStandKeterangan);

        ImageView imgBacaStandGantiPhotoMeter = findViewById(R.id.imgBacaStandGantiPhotoMeter);
        imgBacaStandGantiPhotoMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                Intent intent = new Intent(BacaStand.this, BacaCamera.class);
                intent.putExtra("extNopel", extNopel);
                intent.putExtra("parent", parent);
                startActivity(intent);

                finish();
            }
        });

        ImageView imgBacaStandBack = findViewById(R.id.imgBacaStandBack);
        imgBacaStandBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                finish();
            }
        });

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final ImageView imgBacaStandMeter = findViewById(R.id.imgBacaStandMeter);
        imgBacaStandMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageFromThumb(view, ((BitmapDrawable) imgBacaStandMeter.getDrawable()).getBitmap());
            }
        });

        // Tangkap data dari activity seblumnya
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            extNopel = extras.getString("extNopel");
            extFilename = extras.getString("extFilename");
            extStand = extras.getString("extStand");
            parent = extras.getString("parent");
        }
        if (extNopel != null && !extNopel.equals("")) {
            // panggil database
            databaseHandler = new DatabaseHandler(this);

            if (extStand != null && !extStand.equals("")) {
                edtBacaStandStand.setText(extStand.replaceAll("[^0-9.]", ""));
            }
            edtBacaStandStand.setSelectAllOnFocus(true);

            // tmapilkan foto meter
//            String imageFileName = extNopel.substring(0, 4) + "-" + extNopel.substring(4, 8) + "_" + bln + "_" + thn + ".JPG"; // DEFAULT FILE NAME PHOTO METERAN
            File direktori = new File(Environment.getExternalStorageDirectory().getPath() + "/PembacaMeter/Photo/");
            File photometer = new File(direktori, extFilename);

            if (photometer.exists()) {
                imgBacaStandMeter.setImageBitmap(BitmapFactory.decodeFile(photometer.getAbsolutePath()));
            } else {
                new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Photo meter tidak ditemukan!", "", Snackbar.LENGTH_SHORT);
            }
        }

        ImageView imgSelectCatatan = findViewById(R.id.imgSelectCatatan);
        imgSelectCatatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                // tampilkan option catatan
                showSelectCatatan();
            }
        });

        btnBacaStandSimpan = findViewById(R.id.btnBacaStandSimpan);
        btnBacaStandSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                btnBacaStandSimpan.setEnabled(false);
                if (edtBacaStandStand.getText().length() > 0) {
                    if (edtBacaStandCatatan.getText().toString().length() > 0) {

                        // PROSES SAVE & UPLOAD
                        simpanBacaan();

                        // proses tracking
                        goTrakcMe();

//                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Selesai", "", 2000);
                    } else {
                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Data belum lengkap!", "", Snackbar.LENGTH_SHORT);
                    }
                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Data belum lengkap!", "", Snackbar.LENGTH_SHORT);
                }
                btnBacaStandSimpan.setEnabled(true);
            }
        });
    }

    private void simpanBacaan() {
        try {
            List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
            String datetime_full = "";
            String init_petugas = "";
            String hari_baca = "";
            for(TablePetugas tablePetugas: tablePetugasList) {
                datetime_full = tablePetugas.getTanggal(); // 2019-09-24 xxxxxx
                init_petugas = tablePetugas.getInisial();
                hari_baca = tablePetugas.getHaribaca();
            }

            //Log.e("FULLDATE", datetime_full);

            String tahun, bulan, tanggal, jam, menit, detik;
            if (datetime_full.equals("")){
                switch (Calendar.getInstance().get(Calendar.MONTH)){
                    case Calendar.JANUARY: bulan = "01"; break;
                    case Calendar.FEBRUARY: bulan = "02"; break;
                    case Calendar.MARCH: bulan = "03"; break;
                    case Calendar.APRIL: bulan = "04"; break;
                    case Calendar.MAY: bulan = "05"; break;
                    case Calendar.JUNE: bulan = "06"; break;
                    case Calendar.JULY: bulan = "07"; break;
                    case Calendar.AUGUST: bulan = "08"; break;
                    case Calendar.SEPTEMBER: bulan = "09"; break;
                    case Calendar.OCTOBER: bulan = "10"; break;
                    case Calendar.NOVEMBER: bulan = "11"; break;
                    default: bulan = "12"; break;
                }
                if (Calendar.getInstance().get(Calendar.DATE) < 10 ) {
                    tanggal = "0" + Calendar.getInstance().get(Calendar.DATE);
                } else {
                    tanggal = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
                }
                tahun = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            } else {
                tahun = datetime_full.substring(0, 4);
                bulan = datetime_full.substring(5, 7);
                tanggal = datetime_full.substring(8, 10);
            }
            if (Calendar.getInstance().get(Calendar.HOUR) < 10 ) {
                jam = "0" + Calendar.getInstance().get(Calendar.HOUR);
            } else {
                jam = String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
            }
            if (Calendar.getInstance().get(Calendar.MINUTE) < 10 ) {
                menit = "0" + Calendar.getInstance().get(Calendar.MINUTE);
            } else {
                menit = String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
            }
            if (Calendar.getInstance().get(Calendar.SECOND) < 10 ) {
                detik = "0" + Calendar.getInstance().get(Calendar.SECOND);
            } else {
                detik = String.valueOf(Calendar.getInstance().get(Calendar.SECOND));
            }

            String bacaNopel = extNopel;
            String bacaStand = edtBacaStandStand.getText().toString();
            String bacaCatat = edtBacaStandCatatan.getText().toString();
            String bacaKeter = edtBacaStandKeterangan.getText().toString();
            String bacaTangg = tahun + "-" + bulan + "-" + tanggal + " " + jam + ":" + menit + ":" + detik;

            // add ke table
            databaseHandler.addBacaan(new TableBacaan(bacaNopel, bacaStand, bacaCatat, bacaKeter, bacaTangg));

            // update status ke table pelanggan
            databaseHandler.updatePelanggan("1", extNopel);

            // Upload ke server
            uploadBacaan(init_petugas, hari_baca, tahun, bulan, tanggal, bacaNopel, bacaStand, bacaCatat, bacaKeter, bacaTangg);

        } catch (Exception e) {
            e.printStackTrace();
            new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Gagal menyimpan data bacaan! ", e.toString(), Snackbar.LENGTH_SHORT);
        }
    }

    private void uploadBacaan(final String Initial, final String Haribaca, final String Tahun, final String Bulan, final String Tanggal, final String Nopel, final String Stand, final String Catatan, final String Keterangan, final String FullTanggal) {
        final String api_token = ((GlobalVars) getApplication()).getApiToken();

        progressDialog.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, ambilAPISetting(), new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                progressDialog.dismiss();

                //outputResponse(response);
                byte[] bytes = response.data;
                String str = new String(bytes, StandardCharsets.UTF_8);

                if (str.equals("ok")){
                    // kasih popup kalo udah selesai, atau bikin delay lalu next action di Snackbar nya
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Data berhasil disimpan dan diupload ke server!", "", 3000);
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Intent intent;
                                    if (parent.equals("1")) {
                                        intent = new Intent(BacaStand.this, ByNopel.class);
                                    } else {
                                        intent = new Intent(BacaStand.this, MainActivity.class);
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 3000);
                } else {
                    // Create a AlertDialog Builder.
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BacaStand.this);
                    // Set title, icon, can not cancel properties.
                    alertDialogBuilder.setTitle("Upload gagal!");
                    alertDialogBuilder.setIcon(R.drawable.logopdam);
                    alertDialogBuilder.setCancelable(false);

                    // Set the inflated layout view object to the AlertDialog builder.
                    LayoutInflater layoutInflater = LayoutInflater.from(BacaStand.this);
                    @SuppressLint("InflateParams") View popup_view_reupload = layoutInflater.inflate(R.layout.popup_reupload, null);
                    Button btnCobaLagi = popup_view_reupload.findViewById(R.id.btnCobaLagi);
                    Button btnNantiSaja = popup_view_reupload.findViewById(R.id.btnNantiSaja);
                    alertDialogBuilder.setView(popup_view_reupload);

                    // Create AlertDialog and show.
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    btnCobaLagi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Functions(getApplicationContext()).Getar();
                            alertDialog.dismiss();

                            uploadBacaan(Initial, Haribaca, Tahun, Bulan, Tanggal, Nopel, Stand, Catatan, Keterangan, FullTanggal);
                        }
                    });

                    btnNantiSaja.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Functions(getApplicationContext()).Getar();
                            alertDialog.dismiss();

                            Intent intent;
                            if (parent.equals("1")) {
                                intent = new Intent(BacaStand.this, ByNopel.class);
                            } else {
                                intent = new Intent(BacaStand.this, MainActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorExtractionForSaveProfileAccount(error);
                error.printStackTrace();

                progressDialog.dismiss();
                btnBacaStandSimpan.setEnabled(true);

            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "uploadx");
                params.put("init", Initial);
                params.put("readday", Haribaca);
                params.put("custid", Nopel);
                params.put("year", Tahun);
                params.put("month", Bulan);
                params.put("date", Tanggal);
                params.put("stand", Stand);
                params.put("notes", Catatan);
                params.put("desc", Keterangan);
                params.put("fulldate", FullTanggal);
                params.put("token", api_token);
                System.out.println(Collections.singletonList(params));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    File direktori = new File(Environment.getExternalStorageDirectory().getPath() + "/PembacaMeter/Photo/");
                    File photometer = new File (direktori, extFilename);

                    InputStream iStream = getContentResolver().openInputStream(Uri.fromFile(photometer));
                    byte[] inputData = new byte[0];
                    if (iStream != null) {
                        inputData = getBytes(iStream);
                    }
                    params.put("photometer", new DataPart(extFilename, inputData, "image/jpeg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }

    private void errorExtractionForSaveProfileAccount(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");
                String message = response.getString("message");

                Log.e("Error Status", status);
                Log.e("Error Message", message);

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    errorMessage = message + " Please login again";
                } else if (networkResponse.statusCode == 400) {
                    errorMessage = message + " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + " Something is getting wrong";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e("Error", errorMessage);
        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacaStand), "Upload gagal! \n" + errorMessage, "\nData sudah tersimpan, Upload dapat dilakukan nanti melalui menu Upload", Snackbar.LENGTH_LONG);

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showSelectCatatan() {
        final ArrayAdapter<String> items = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        List<TableCatatan> tableCatatanList = databaseHandler.selectCatatan();
        if (tableCatatanList.size() > 0) {
           for (int i = 0; i < tableCatatanList.size(); i ++) {
               items.add(tableCatatanList.get(i).getKode() + ". " + tableCatatanList.get(i).getKeterangan());
           }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih catatan bacaan");
        builder.setIcon(R.drawable.logopdam);
        builder.setAdapter(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Functions(getApplicationContext()).Getar();

                edtBacaStandCatatan.setText(Objects.requireNonNull(items.getItem(i)).replace(".", "").trim());
            }
        });
        builder.show();
    }

    private void zoomImageFromThumb(final View thumbView, Bitmap imageBitmap) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        final ImageView expandedImageView = findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(imageBitmap);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.rootBacaStand).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(expandedImageView, View.X,startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,startBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
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

    private void goTrakcMe() {
        String init = "";
        List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
        if (tablePetugasList.size() > 0) {
            for (TablePetugas tablePetugas: tablePetugasList) {
                init = tablePetugas.getInisial();
            }
        }

        String api_token = ((GlobalVars) this.getApplication()).getApiToken();
        String serverRequest = ambilAPISetting() + "/?action=logger&init=" + init + "&lat=" + vLat + "&lon=" + vLng + "&act=BACA -> "+ extNopel +"&token=" + api_token;

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
