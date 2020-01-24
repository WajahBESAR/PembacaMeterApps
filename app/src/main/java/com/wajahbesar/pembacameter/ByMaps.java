package com.wajahbesar.pembacameter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePelanggan;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ByMaps extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Location currentLocation;

    private int imageType;
    private ImageView imgMapType;

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_maps);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        LinearLayout layoutBottom = findViewById(R.id.layoutBottom);
        layoutBottom.setVisibility(View.INVISIBLE);

        ImageView btnByMapsBack = findViewById(R.id.btnByMapsBack);
        btnByMapsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();
                finish();
            }
        });

        databaseHandler = new DatabaseHandler(this);

        imageType = 0;
        imgMapType = findViewById(R.id.imgMapType);
        imgMapType.setImageDrawable(getDrawable(R.drawable.icon_map_satellite));
        imgMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                if(imageType == 0) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    imgMapType.setImageDrawable(getDrawable(R.drawable.icon_map_default));
                    imageType = 1;
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    imgMapType.setImageDrawable(getDrawable(R.drawable.icon_map_satellite));
                    imageType = 0;
                }
            }
        });

        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    // Add a marker in Tirta Pakuan
                    LatLng lokasiAwal = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Anda").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_people)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 15));
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Set default place, sebelum get current location
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.620237, 106.815656), 15));

        // GET CURRENT LOCATION
//        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
//        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null){
//                    currentLocation = location;
//                    // Add a marker in Tirta Pakuan
//                    LatLng lokasiAwal = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Anda").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_people)));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 15));
//                }
//            }
//        });

        // POPULATE DATA PELANGGAN WITH COORDINATES
        ArrayList<TablePelanggan> tablePelangganArrayList = (ArrayList<TablePelanggan>) databaseHandler.selectPelanggan();
        for (int i = 0; i < tablePelangganArrayList.size(); i ++) {
            if ((tablePelangganArrayList.get(i).getLatitude().length() > 3) && (tablePelangganArrayList.get(i).getLongitude().length() > 3)){
                LatLng posision = new LatLng(Double.parseDouble(tablePelangganArrayList.get(i).getLatitude()), Double.parseDouble(tablePelangganArrayList.get(i).getLongitude()));
                String nopel = tablePelangganArrayList.get(i).getNopel();
                String nama = tablePelangganArrayList.get(i).getNama();
                String alamat = tablePelangganArrayList.get(i).getAlamat();
                String dibaca = tablePelangganArrayList.get(i).getDibaca();
                BitmapDescriptor bitmapDescriptor;
                if (dibaca.equals("0")) {
                    bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_red);
                } else {
                    bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
                }
                mMap.addMarker(new MarkerOptions()
                        .position(posision)
                        .title(nopel)
                        .snippet(nama + "\n" + alamat)
                        .icon(bitmapDescriptor));
            }
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(ByMaps.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(ByMaps.this);
                title.setTextColor(Color.RED);
                title.setGravity(Gravity.CENTER);
                title.setTextSize(16);
                title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(ByMaps.this);
                snippet.setGravity(Gravity.CENTER);
                snippet.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                snippet.setTextSize(14);
                snippet.setTextColor(Color.BLACK);
                snippet.setText(marker.getSnippet());

                Location pelangganLocation = new Location("pelanggan");
                pelangganLocation.setLatitude(marker.getPosition().latitude);
                pelangganLocation.setLongitude(marker.getPosition().longitude);
                TextView jarak = new TextView(ByMaps.this);
                jarak.setGravity(Gravity.CENTER);
                jarak.setTextColor(Color.RED);
                jarak.setText(getJarak(currentLocation, pelangganLocation));

                TextView link = new TextView(ByMaps.this);
                link.setGravity(Gravity.CENTER);
                link.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                link.setTextColor(Color.BLUE);
                link.setTextSize(12);
                link.setText("Klik disini untuk membuka informasi pelanggan!");

                info.addView(title);
                info.addView(snippet);
                info.addView(jarak);
                info.addView(link);

                return info;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                new Functions(getApplicationContext()).Getar();

                marker.showInfoWindow();
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                new Functions(getApplicationContext()).Getar();

                String tempNopel = "", tempNama = "", tempAlamat = "", tempGoltar = "", tempTelp = "", tempMetnum = "", tempLat = "", tempLon = "";

                List<TablePelanggan> tablePelangganList = databaseHandler.searchPelanggan("nopel", marker.getTitle());
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
                    extras.putString("parent", "3");

                    Intent intent = new Intent(ByMaps.this, BacaMeter.class);
                    intent.putExtras(extras);

                    startActivity(intent);

                } else {
                    new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootByMaps), "Pelanggan tidak ditemukan!", "", -1);
                }

            }
        });
    }

    public String getJarak(Location awal, Location akhir) {
        if ((awal != null) && (akhir != null)) {
            return "Perkiraan jarak: " + awal.distanceTo(akhir) + " meter";
        } else {
            return "Perkiraan jarak: Tidak diketahui";
        }
    }

    @Override
    public void onLocationChanged(Location location){
        if (location != null){
            currentLocation = location;
            // Add a marker in Tirta Pakuan
            LatLng lokasiAwal = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Anda"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 15));
        }
    }

    @Override
    public void onProviderDisabled(String arg0){
        // Do something here if you would like to know when the provider is disabled by the user
        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootByMaps), "GPS mati!", "", 3000);
    }

    @Override
    public void onProviderEnabled(String arg0){
        // Do something here if you would like to know when the provider is enabled by the user
        new Functions(getApplicationContext()).showMessage(findViewById(R.id.rootByMaps), "GPS hidup!", "", 3000);
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2){
        // Do something here if you would like to know when the provider status changes
    }
}
