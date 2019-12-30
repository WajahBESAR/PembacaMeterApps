package com.wajahbesar.pembacameter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.util.List;
import java.util.Objects;

public class ByNopel extends AppCompatActivity {

    private DatabaseHandler databaseHandler;

    private TextView txtInput;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_nopel);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        databaseHandler = new DatabaseHandler(this);

        txtInput = findViewById(R.id.txtInput);
        txtInput.setText("");

        Button keyButton0 = findViewById(R.id.keyButton0);
        Button keyButton1 = findViewById(R.id.keyButton1);
        Button keyButton2 = findViewById(R.id.keyButton2);
        Button keyButton3 = findViewById(R.id.keyButton3);
        Button keyButton4 = findViewById(R.id.keyButton4);
        Button keyButton5 = findViewById(R.id.keyButton5);
        Button keyButton6 = findViewById(R.id.keyButton6);
        Button keyButton7 = findViewById(R.id.keyButton7);
        Button keyButton8 = findViewById(R.id.keyButton8);
        Button keyButton9 = findViewById(R.id.keyButton9);
        Button keyButtonBack = findViewById(R.id.keyButtonBack);
        Button keyButtonClear = findViewById(R.id.keyButtonClear);

        Button btnNometer = findViewById(R.id.btnNoMeter);
        Button btnNoPelanggan = findViewById(R.id.btnNoPelanggan);

        progressDialog = new ProgressDialog(ByNopel.this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        keyButton0.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "0");
            }
        });

        keyButton1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "1");
            }
        });

        keyButton2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "2");
            }
        });

        keyButton3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "3");
            }
        });

        keyButton4.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "4");
            }
        });

        keyButton5.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "5");
            }
        });

        keyButton6.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "6");
            }
        });

        keyButton7.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "7");
            }
        });

        keyButton8.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "8");
            }
        });

        keyButton9.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText(txtInput.getText().toString() + "9");
            }
        });

        keyButtonClear.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                txtInput.setText("");
            }
        });

        keyButtonBack.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                String string = txtInput.getText().toString();
                new Functions(getApplicationContext()).Getar();
                if(txtInput.getText().length() > 1) {
                    txtInput.setText(string.substring(0, string.length() - 1));
                } else {
                    txtInput.setText("");
                }
            }
        });

        btnNoPelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                String input = txtInput.getText().toString();
                if(input.length() == 8){
                    cariPelanggan("nopel", txtInput.getText().toString());
                    //Log.e("COUNT", String.valueOf(databaseHandler.countPelanggan("0")));
                } else{
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootKeyboard), "Pelanggan tidak ditemukan!", "", -1);
                }
            }
        });

        btnNometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                String input = txtInput.getText().toString();
                if(input.length() > 4 && input.length() < 15){
                    progressDialog.show();
                    cariPelanggan("metnum", txtInput.getText().toString());
                    progressDialog.dismiss();
                } else{
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootKeyboard), "Pelanggan tidak ditemukan!", "", -1);
                }
            }
        });
    }

    private void cariPelanggan(String field, String data) {
        String tempNopel = "", tempNama = "", tempAlamat = "", tempGoltar = "", tempTelp = "", tempMetnum = "", tempLat = "", tempLon = "";

        List<TablePelanggan> tablePelangganList = databaseHandler.searchPelanggan(field, data);
//        Log.e("PELANGGAN", String.valueOf(tablePelangganList.size()));
        if(tablePelangganList.size() > 0) {
            for (TablePelanggan tablePelanggan : tablePelangganList) {
                tempNopel = tablePelanggan.getNopel();
                tempNama = tablePelanggan.getNama();
                tempAlamat = tablePelanggan.getAlamat();
                tempGoltar = tablePelanggan.getGoltar();
                tempTelp = tablePelanggan.getTelepon();
                tempMetnum = tablePelanggan.getMetnum();
                tempLat = tablePelanggan.getLatitude();
                tempLon = tablePelanggan.getLongitude();
            }

            Bundle extras = new Bundle();
            extras.putString("extNopel", tempNopel);
            extras.putString("extNama", tempNama);
            extras.putString("extAlamat", tempAlamat);
            extras.putString("extGoltar", tempGoltar);
            extras.putString("extTelp", tempTelp);
            extras.putString("extMetnum", tempMetnum);
            extras.putString("extLat", tempLat);
            extras.putString("extLon", tempLon);

            Intent intent = new Intent(this, BacaMeter.class);
            intent.putExtras(extras);

            startActivity(intent);

        } else {
            new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootKeyboard), "Pelanggan tidak ditemukan!", "", -1);
        }
    }

}
