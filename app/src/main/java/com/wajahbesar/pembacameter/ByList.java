package com.wajahbesar.pembacameter;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Utilities.Functions;
import com.wajahbesar.pembacameter.Utilities.PelangganAdapter;

import java.util.ArrayList;
import java.util.List;

public class ByList extends AppCompatActivity {

    public String datetime_full;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_list);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);

        List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
        for(TablePetugas tablePetugas: tablePetugasList) {
            datetime_full = tablePetugas.getTanggal(); // 2019-09-24 xxxxxx
        }

        ImageView btnByListBack = findViewById(R.id.btnByListBack);
        btnByListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                finish();
            }
        });

        RecyclerView rvPelanggan = findViewById(R.id.rvPelanggan);
        rvPelanggan.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvPelanggan.setLayoutManager(layoutManager);

        ArrayList<TablePelanggan> pelangganArrayList = (ArrayList<TablePelanggan>) databaseHandler.selectPelanggan();
        final PelangganAdapter mAdapter = new PelangganAdapter(this, pelangganArrayList, this);
        rvPelanggan.setAdapter(mAdapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new Functions(getApplicationContext()).Getar();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Functions(getApplicationContext()).Getar();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public String getDatetime_full() {
        return datetime_full;
    }
}
