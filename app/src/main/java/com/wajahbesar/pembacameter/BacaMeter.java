package com.wajahbesar.pembacameter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Database.TableUpdate;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.util.List;
import java.util.Objects;

public class BacaMeter extends AppCompatActivity implements OnMapReadyCallback{

    private String curNopel;
    private ImageView imgRumah;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Animator currentAnimator;
    private int shortAnimationDuration;
    private String parent;
    private DatabaseHandler databaseHandler;

    ProgressDialog progressDialog;
    CardView bacameterCarMap;
    MapView bacametermapView;
    LatLng newLatLng;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_meter);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        final EditText edtNopel = findViewById(R.id.edtBacaMeterNopel);
        EditText edtNomet = findViewById(R.id.edtBacaMeterNomet);
        final EditText edtNama = findViewById(R.id.edtBacaMeterNama);
        final EditText edtAlamat = findViewById(R.id.edtBacaMeterAlamat);
        final EditText edtTelp = findViewById(R.id.edtBacaMeterTelp);
        EditText edtGoltar = findViewById(R.id.edtBacaMeterGoltar);

        disableEditText(edtNopel);
        disableEditText(edtNomet);
        disableEditText(edtNomet);
        disableEditText(edtNama);
        disableEditText(edtAlamat);
        disableEditText(edtTelp);
        disableEditText(edtGoltar);

        ImageView imgBacaMeterGantiPhotoRumah = findViewById(R.id.imgBacaMeterGantiPhotoRumah);
        imgBacaMeterGantiPhotoRumah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BacaMeter.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("Photo Rumah Pelanggan");
                alertDialogBuilder.setIcon(R.drawable.logopdam);
                alertDialogBuilder.setCancelable(true);

                // Set the inflated layout view object to the AlertDialog builder.
                LayoutInflater layoutInflater = LayoutInflater.from(BacaMeter.this);
                @SuppressLint("InflateParams") final View popup_gantiphotorumah_view = layoutInflater.inflate(R.layout.popup_gantiphotorumah, null);

                Button btnGantiPhotoRumah = popup_gantiphotorumah_view.findViewById(R.id.btnGantiPhotoRumah);
                alertDialogBuilder.setView(popup_gantiphotorumah_view);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                btnGantiPhotoRumah.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {
                        new Functions(getApplicationContext()).Getar();

                        alertDialog.dismiss();
                        dispatchTakePictureIntent();
                    }
                });
            }
        });

        ImageView imgBacaMeterBack = findViewById(R.id.imgBacaMeterBack);
        imgBacaMeterBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                finish();
            }
        });

        imgRumah = findViewById(R.id.imgRumah);
        imgRumah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new Functions(getApplicationContext()).Getar();
                zoomImageFromThumb(view, ((BitmapDrawable) imgRumah.getDrawable()).getBitmap());
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curNopel = extras.getString("extNopel");

            edtNopel.setText(extras.getString("extNopel"));
            edtNama.setText(extras.getString("extNama"));
            edtAlamat.setText(extras.getString("extAlamat"));
            edtGoltar.setText(extras.getString("extGoltar"));
            edtTelp.setText(extras.getString("extTelp"));
            edtNomet.setText(extras.getString("extMetnum"));
            parent = extras.getString("parent");

            getHousePic();
        }

        ImageView imgAlamatDetail = findViewById(R.id.imgAlamatDetail);
        imgAlamatDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                AlertDialog.Builder builder = new AlertDialog.Builder(BacaMeter.this);
                builder.setMessage(edtAlamat.getText());
                builder.show();
            }
        });

        Button btnBacaMeterBaca = findViewById(R.id.btnBacaMeterBaca);
        btnBacaMeterBaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                Intent intent = new Intent(BacaMeter.this, BacaCamera.class);
                intent.putExtra("extNopel", curNopel);
                intent.putExtra("parent", parent);

                startActivity(intent);
            }
        });

        ImageView imgBacaMeterTelp = findViewById(R.id.imgBacaMeterTelp);
        imgBacaMeterTelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                if (edtTelp.length() < 7) {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Nomor telepon salah!", "", Snackbar.LENGTH_LONG);
                } else {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + edtTelp.getText().toString())));
                }
            }
        });

        databaseHandler = new DatabaseHandler(this);

        TextView txtHubungiTelp = findViewById(R.id.txtHubungiTelp);
        txtHubungiTelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                final EditText edtNewTelp = new EditText(BacaMeter.this);
                edtNewTelp.setInputType(InputType.TYPE_CLASS_PHONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(BacaMeter.this);
                builder.setTitle("Nomor Telepon Baru");
                builder.setView(edtNewTelp);
                builder.setPositiveButton("Tambahkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Functions(getApplicationContext()).Getar();

                        if (edtNewTelp.getText().toString().length() > 5) {
                            progressDialog.show();

                            edtTelp.setText(edtNewTelp.getText());

                            // update ke database
                            // update type: 1 -> phone, 2 -> photorumah, 3 -> koordinat
                            databaseHandler.addUpdate(new TableUpdate(edtNopel.getText().toString(), "1", edtNewTelp.getText().toString(), "0"));

                            // get initial
                            String init = "";
                            List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
                            if (tablePetugasList.size() > 0) {
                                for (TablePetugas tablePetugas : tablePetugasList) {
                                    init = tablePetugas.getInisial();
                                }
                            }

                            // get token
                            String api_token = ((GlobalVars) BacaMeter.this.getApplication()).getApiToken();

                            // upload ke server
                            // start async
                            RequestQueue queue = Volley.newRequestQueue(BacaMeter.this);
                            String url = ambilAPISetting() + "/?action=update&init=" + init + "&type=1&custid=" + edtNopel.getText() + "&value=" + edtNewTelp.getText() + "&token=" + api_token;

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.e("UPDATE", response);
                                    if (response.equals("ok")) {
                                        // update status di database
                                        databaseHandler.updateUpdate(edtNopel.getText().toString(), "1", "1");

                                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update nomor telepon berhasil dikirim ke Server!", "", 3000);
                                    } else {
                                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update nomor telepon berhasil disimpan, tapi gagal dikirim ke Server \nBuka menu Upload ntuk mengirim ulang!", "", 5000);
                                    }

                                    progressDialog.dismiss();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update nomor telepon berhasil disimpan, tapi gagal dikirim ke Server \nBuka menu Upload ntuk mengirim ulang!", "", 5000);
                                    progressDialog.dismiss();
                                }
                            });

                            queue.add(stringRequest);
                        } else {
                            new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Data belum lengkap!", "", 3000);
                        }
                    }
                });
                builder.show();
            }
        });

        TextView txtUpdateKoordinat = findViewById(R.id.txtUpdateKoordinat);
        txtUpdateKoordinat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                bacameterCarMap.setVisibility(View.VISIBLE);
            }
        });

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progressDialog = new ProgressDialog(this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        bacameterCarMap = findViewById(R.id.bacameterCarMap);
        bacameterCarMap.setVisibility(View.INVISIBLE);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        bacametermapView = findViewById(R.id.bacametermapView);
        bacametermapView.onCreate(mapViewBundle);
        bacametermapView.getMapAsync(this);

        findViewById(R.id.btnUpdateLatLng).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                progressDialog.show();

                // update ke database
                // update type: 1 -> phone, 2 -> photorumah, 3 -> koordinat
                databaseHandler.addUpdate(new TableUpdate(edtNopel.getText().toString(), "3", String.valueOf(newLatLng), "0"));

                // get initial
                String init = "";
                List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
                if (tablePetugasList.size() > 0) {
                    for (TablePetugas tablePetugas : tablePetugasList) {
                        init = tablePetugas.getInisial();
                    }
                }

                // get token
                String api_token = ((GlobalVars) BacaMeter.this.getApplication()).getApiToken();

                // upload ke server
                RequestQueue queue = Volley.newRequestQueue(BacaMeter.this);
                String url = ambilAPISetting() + "/?action=update&init=" + init + "&type=3&custid=" + edtNopel.getText() + "&value=" + newLatLng.latitude + "," + newLatLng.longitude + "&token=" + api_token;

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("UPDATE", response);
                        if (response.equals("ok")) {
                            // update status di database
                            databaseHandler.updateUpdate(edtNopel.getText().toString(), "3", "1");

                            new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update koordinat berhasil dikirim ke Server!", "", 3000);
                        } else {
                            new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update koordinat berhasil disimpan, tapi gagal dikirim ke Server \nBuka menu Upload ntuk mengirim ulang!", "", 5000);
                        }

                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter), "Update koordinat berhasil disimpan, tapi gagal dikirim ke Server \nBuka menu Upload ntuk mengirim ulang!", "", 5000);
                        progressDialog.dismiss();
                    }
                });

                queue.add(stringRequest);

                bacameterCarMap.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            imgRumah.setImageBitmap(imageBitmap);

            // PROSES SAVE & UPLOAD FOTO RUMAH
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
//        editText.setBackgroundColor(Color.TRANSPARENT);
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
        findViewById(R.id.rootBacameter).getGlobalVisibleRect(finalBounds, globalOffset);
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
        set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left))
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
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
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

    private void getHousePic() {
        String api_token = ((GlobalVars) this.getApplication()).getApiToken();
        String serverRequest = ambilAPISetting() + "/?action=housepic&custid=" + curNopel + "&token=" + api_token;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                if (response.length() > 0) {
                    try {
                        byte[] decodedString = Base64.decode(response.getBytes(), Base64.DEFAULT);
                        imgRumah.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);

    }

    private String ambilAPISetting() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        String tempApiSetting = "";
        List<TableSetting> tableSettingList = databaseHandler.bukaSetting();
        if (tableSettingList.size() > 0) {
            for (TableSetting tableSetting : tableSettingList) {
                tempApiSetting = tableSetting.getURLAPI();
            }
        }
        return tempApiSetting;
    }

    @Override
    public void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        bacametermapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bacametermapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bacametermapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bacametermapView.onStop();
    }

    @Override
    protected void onPause() {
        bacametermapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        bacametermapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        bacametermapView.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    // Add a marker in Tirta Pakuan
                    LatLng lokasiAwal = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Anda").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_people)));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 23));
                }
            }
        });

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setMinZoomPreference(12);
        LatLng ny = new LatLng(40.7143528, -74.0059731);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ny));

        newLatLng = new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
        final TextView txtNewLat = findViewById(R.id.txtNewLat);
        final TextView txtNewLng = findViewById(R.id.txtNewLng);
        txtNewLat.setText(String.valueOf(googleMap.getCameraPosition().target.latitude));
        txtNewLng.setText(String.valueOf(googleMap.getCameraPosition().target.longitude));

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                newLatLng = new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
                txtNewLat.setText(String.valueOf(googleMap.getCameraPosition().target.latitude));
                txtNewLng.setText(String.valueOf(googleMap.getCameraPosition().target.longitude));
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (bacameterCarMap.getVisibility() == View.VISIBLE) {
            // Set Koordinat here
            bacameterCarMap.setVisibility(View.INVISIBLE);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

}
