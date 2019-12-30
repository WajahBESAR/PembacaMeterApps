package com.wajahbesar.pembacameter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.util.List;

public class BacaMeter extends AppCompatActivity {

    private String curNopel;
    private ImageView imgRumah;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Animator currentAnimator;
    private int shortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_meter);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        EditText edtNopel = findViewById(R.id.edtBacaMeterNopel);
        EditText edtNomet = findViewById(R.id.edtBacaMeterNomet);
        EditText edtNama = findViewById(R.id.edtBacaMeterNama);
        EditText edtAlamat = findViewById(R.id.edtBacaMeterAlamat);
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
                @SuppressLint("InflateParams")
                final View popup_gantiphotorumah_view = layoutInflater.inflate(R.layout.popup_gantiphotorumah, null);

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

            getHousePic();
        }

        Button btnBacaMeterBaca = findViewById(R.id.btnBacaMeterBaca);
        btnBacaMeterBaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                Intent intent = new Intent(BacaMeter.this, BacaCamera.class);
                intent.putExtra("extNopel", curNopel);

                startActivity(intent);
            }
        });

        ImageView imgBacaMeterTelp = findViewById(R.id.imgBacaMeterTelp);
        imgBacaMeterTelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                if (edtTelp.length() < 7){
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootBacameter),"Nomor telepon salah!", "", Snackbar.LENGTH_LONG);
                } else {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + edtTelp.getText().toString())));
                }
            }
        });

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
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
            public void onErrorResponse(VolleyError error) {}
        });
        queue.add(stringRequest);

    }

    private String ambilAPISetting() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
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
