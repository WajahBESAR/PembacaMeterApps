package com.wajahbesar.pembacameter;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Utilities.Functions;
import com.wajahbesar.pembacameter.Utilities.UploadAdapter;

import java.util.ArrayList;
import java.util.List;

public class Upload extends AppCompatActivity {

    public String datetime_full;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);

        List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
        for(TablePetugas tablePetugas: tablePetugasList) {
            datetime_full = tablePetugas.getTanggal(); // 2019-09-24 xxxxxx
        }

        findViewById(R.id.btnUploadBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                finish();
            }
        });

        RecyclerView rvUpload = findViewById(R.id.rvUpload);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvUpload.setLayoutManager(layoutManager);

        final ArrayList<TablePelanggan> pelangganArrayList = (ArrayList<TablePelanggan>) databaseHandler.searchPelanggan("dibaca", "1");
        final UploadAdapter mAdapter = new UploadAdapter(this, pelangganArrayList, this);
        rvUpload.setAdapter(mAdapter);

        findViewById(R.id.btnUploadSemua).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                if (pelangganArrayList.size() > 0) {
                    // proses upload
                    //..
                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootUpload), "Tidak ada ada!", "", 5000);
                }
            }
        });
    }

    public String getDatetime_full() {
        return datetime_full;
    }

}
