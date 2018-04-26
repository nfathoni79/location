package com.example.asus.checkinlokasi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.asus.checkinlokasi.util.ShowAlert;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements LocationView,LocationServiceView, View.OnClickListener, MyReceiver.PeriodicCheckLocation {

    private EditText etLocationName, etNote, etKontributor;
    private Button btnSave, btnSetLocation;
    private TextView tvLongitudeLatitude;
    private LocationPresenter locationPresenter;
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private Intent intent;
    private Timer timer;

    private MyReceiver mBroadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etLocationName = findViewById(R.id.et_location_name);
        etNote = findViewById(R.id.et_note);
        etKontributor = findViewById(R.id.et_kontributor);
        btnSave = findViewById(R.id.btn_save);
        btnSetLocation = findViewById(R.id.btn_set_location);
        tvLongitudeLatitude = findViewById(R.id.tv_longitude_latitude);
        initView();
        initPresenter();
        initService();
        registerReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
        if (intent != null){
            stopService(intent);
        }
        timer.cancel();

    }

    public void registerReceiver() {
        mBroadcast = new MyReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(MyReceiver.TAG);
        registerReceiver(mBroadcast, filter);
    }

    private void unregisterReceiver() {
        try {
            if (mBroadcast != null) {
                unregisterReceiver(mBroadcast);
            }
        } catch (Exception e) {
            Log.i("", "broadcastReceiver is already unregistered");
            mBroadcast = null;
        }

    }

    private void initService() {

        intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    private void initView() {
        btnSetLocation.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    private void initPresenter() {
        locationPresenter = new LocationPresenter(MainActivity.this, MainActivity.this);
        locationPresenter.getLocation();
    }

    @Override
    public void onSuccessGetLocation(Location addresses) {
        if(locationPresenter == null){
            locationPresenter = new LocationPresenter(this, this);
            locationPresenter.getLocation();
        }
        tvLongitudeLatitude.setText(Double.toString(addresses.getLongitude()) +", "+ Double.toString(addresses.getLatitude()) );
    }

    @Override
    public void onDisabledGPS(String please_enable_gps_and_internet) {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Terjadi kesalahan. Apakah anda ingin mengulang?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
//                if(ShowAlert.dialog != null && ShowAlert.dialog.isShowing()){
//                    ShowAlert.closeProgresDialog();
//                }
//                ShowAlert.showProgresDialog(TesMinatActivity.this);
//                tesMinatPresenter.showTesMinatQuestion();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert = builder.create();
        alert.show();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_set_location){
            locationPresenter.getLocation();
        }
        if(v.getId() == R.id.btn_save){
            String locationName = etLocationName.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String contributor = etKontributor.getText().toString().trim();
            if(locationName.isEmpty()){
                etLocationName.setError(getString(R.string.text_cannot_empty));
                etLocationName.requestFocus();
            }else if(note.isEmpty()){
                etNote.setError(getString(R.string.text_cannot_empty));
                etNote.requestFocus();
            }else if(contributor.isEmpty()){
                etKontributor.setError(getString(R.string.text_cannot_empty));
                etKontributor.requestFocus();
            }else {

            }
        }
    }

    @Override
    public void handleFromReceiver(String location) {
        if(location != null){
            tvLongitudeLatitude.setText(location);
        }

    }

    @Override
    public void onDisabledGPSFromService(String internet_and_gps_not_available) {
        ShowAlert.showToast(this, internet_and_gps_not_available);
    }

    @Override
    public void onSuccessGetLocationFromService(Location location) {
        if(location != null){
            tvLongitudeLatitude.setText(Double.toString(location.getLongitude()) +", "+ Double.toString(location.getLatitude()) );
        }
    }
}
