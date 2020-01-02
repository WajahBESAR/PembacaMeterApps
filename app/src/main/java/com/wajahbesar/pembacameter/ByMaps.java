package com.wajahbesar.pembacameter;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class ByMaps extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_maps);

        // TRANSPARENT NOTIFBAR
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    // Add a marker in Tirta Pakuan
                    LatLng lokasiAwal = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Anda"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAwal, 15));
                }
            }
        });

        mMap = googleMap;

        // Set default place, sebelum get current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.620237, 106.815656), 15));
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
    }

    @Override
    public void onProviderEnabled(String arg0){
        // Do something here if you would like to know when the provider is enabled by the user
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2){
        // Do something here if you would like to know when the provider status changes
    }
}
