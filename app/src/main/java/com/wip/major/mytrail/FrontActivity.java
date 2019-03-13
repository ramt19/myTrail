package com.wip.major.mytrail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class FrontActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private DatabaseReference firebaseDatabase;
    private SharedPreferences pref;
    private static final String TAG = "Front Activity";
    private Intent intent;
    private SensorManager sensorManager;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation;
    private String lat;
    private String lon;
    private DetectShake mShaker;
    private LocationSMS sms;
    private boolean track = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ToggleButton mTrack = (ToggleButton) findViewById(R.id.track);
        intent = new Intent(FrontActivity.this, RealTimeTracking.class);
        pref = getApplicationContext().getSharedPreferences("session", MODE_PRIVATE);

        String email = pref.getString("email", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.sms);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (checkContacts()) {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        Toast.makeText(getApplicationContext(), "Enable GPS", LENGTH_SHORT).show();

                    sms = new LocationSMS(getApplicationContext(), lat, lon);
                    sms.sendSMS(!track);
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        boolean t = checkContacts();
        if (email != null) {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("Users");
            firebaseDatabase.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> list = (HashMap<String, String>) dataSnapshot.getValue();
                    SharedPreferences.Editor editor = pref.edit();
                    for (String key : list.keySet()
                            ) {
                        editor.putString("uid", key);
                        Log.i(TAG, key);
                    }
                    editor.commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Log.i(TAG, "email is null");
        }
        mTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    if (checkContacts()) {
                        startService(intent);
                        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            Toast.makeText(getApplicationContext(), "Enable GPS", LENGTH_SHORT).show();

                        sms = new LocationSMS(getApplicationContext(), lat, lon);
                        sms.sendSMS(track);
                    }


                } else {
                    new FusedLocationProviderClient(getApplicationContext()).removeLocationUpdates(new LocationCallback());
                    stopService(intent);
                }
            }
        });

        final Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        mShaker = new DetectShake(this);
        mShaker.setOnShakeListener(new DetectShake.OnShakeListener () {
            public void onShake()
            {
                vibe.vibrate(100);
                new AlertDialog.Builder(FrontActivity.this)
                        .setPositiveButton(android.R.string.ok, null)
                        .setMessage("Location Send!")
                        .show();
                LocationSMS sms = new LocationSMS(getApplicationContext(),lat, lon);
                sms.sendSMS(!track);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.front, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            Intent inn = new Intent(this, UpdateContacts.class);
            startActivity(inn);
            // Handle the camera action
        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("email");
            editor.commit();
            FirebaseAuth.getInstance().signOut();
            Intent intn = new Intent(this, LoginActivity.class);
            stopService(intent);
            startActivity(intn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public boolean checkContacts(){
        pref = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);
        String cont = pref.getString("contacts","null");

        if(cont.equals("false")){
            AlertDialog.Builder builder = new AlertDialog.Builder(FrontActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("Create contacts to send Location");
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        return true;
    }
}
