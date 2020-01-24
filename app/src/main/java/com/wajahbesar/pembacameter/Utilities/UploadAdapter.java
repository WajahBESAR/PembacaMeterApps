package com.wajahbesar.pembacameter.Utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.R;
import com.wajahbesar.pembacameter.Upload;

import java.io.File;
import java.util.ArrayList;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.PelangganViewHolder> implements Filterable {

//    private DatabaseHandler databaseHandler;
    private ArrayList<TablePelanggan> pelangganList;
    private Context context;
    private Activity byUpload;

    public UploadAdapter(Context context, ArrayList<TablePelanggan> pelangganList, Activity activity) {
        this.context = context;
        this.pelangganList = pelangganList;
        byUpload = activity;
//        databaseHandler = new DatabaseHandler(context);
    }

    @NonNull
    @Override
    public PelangganViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_upload, parent, false);
        return new PelangganViewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    class PelangganViewHolder extends RecyclerView.ViewHolder{
        private ImageView photo;
        private TextView nopel, nama, alamat;
//        private RelativeLayout relLayout;

        PelangganViewHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.imgPhoto);
            nopel = itemView.findViewById(R.id.txtTitle);
            nama = itemView.findViewById(R.id.txtNama);
            alamat = itemView.findViewById(R.id.txtAlamat);
//            relLayout = itemView.findViewById(R.id.rlUpload);
        }
    }

    private Bitmap getImage (String Nopel) {
        String datetime_full = ((Upload) byUpload).getDatetime_full();
        String Bulan = datetime_full.substring(5, 7);
        String Tahun = datetime_full.substring(0, 4);
        String fileImage = Nopel.substring(0, 4) + "-" + Nopel.substring(4) + "_" + Bulan + "_" + Tahun + ".JPG";
        File direktori = new File(Environment.getExternalStorageDirectory().getPath() + "/PembacaMeter/Photo/");
        File photometer = new File(direktori, fileImage);
        if(photometer.exists()) {
            return BitmapFactory.decodeFile(photometer.getAbsolutePath());
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_report_image);
        }
    }

    @Override
    public void onBindViewHolder(PelangganViewHolder holder, final int position) {
        String n = pelangganList.get(position).getNopel();
        holder.photo.setImageBitmap(getImage(n));
        holder.nopel.setText(pelangganList.get(position).getNopel());
        holder.nama.setText(pelangganList.get(position).getNama());
        holder.alamat.setText(pelangganList.get(position).getAlamat());

//        holder.relLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Functions(context.getApplicationContext()).Getar();
//
//                String tempNopel = "", tempNama = "", tempAlamat = "", tempGoltar = "", tempTelp = "", tempMetnum = "", tempLat = "", tempLon = "";
//
//                List<TablePelanggan> tablePelangganList = databaseHandler.searchPelanggan("nopel", pelangganList.get(position).getNopel());
//                if(tablePelangganList.size() > 0) {
//                    for (TablePelanggan tablePelanggan : tablePelangganList) {
//                        tempNopel = tablePelanggan.getNopel();
//                        tempNama = tablePelanggan.getNama();
//                        tempAlamat = tablePelanggan.getAlamat();
//                        tempGoltar = tablePelanggan.getGoltar();
//                        tempTelp = tablePelanggan.getTelepon();
//                        tempMetnum = tablePelanggan.getMetnum();
//                        tempLat = tablePelanggan.getLatitude();
//                        tempLon = tablePelanggan.getLongitude();
//                    }
//
//                    Bundle extras = new Bundle();
//                    extras.putString("extNopel", tempNopel);
//                    extras.putString("extNama", tempNama);
//                    extras.putString("extAlamat", tempAlamat);
//                    extras.putString("extGoltar", tempGoltar);
//                    extras.putString("extTelp", tempTelp);
//                    extras.putString("extMetnum", tempMetnum);
//                    extras.putString("extLat", tempLat);
//                    extras.putString("extLon", tempLon);
//                    extras.putString("parent", "2");
//
//                    Intent intent = new Intent(context, BacaMeter.class);
//                    intent.putExtras(extras);
//
//                    context.startActivity(intent);
//
//                } else {
//                    new Functions(context.getApplicationContext()).showMessage(view, "Pelanggan tidak ditemukan!", "", -1);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return (pelangganList != null) ? pelangganList.size() : 0;
    }

}
