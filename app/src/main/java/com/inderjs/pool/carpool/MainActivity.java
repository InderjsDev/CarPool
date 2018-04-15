package com.inderjs.pool.carpool;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.inderjs.pool.carpool.R.id.map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText mDesET, mCurrentEt;
    private Button mGoBtn, mRideBtn, mCancelBtn;
    private ImageView mSettings;
    private TextView mRideText;
    private CardView mFindCard;

    private ArrayList<LatLng> points; //added
    Polyline line; //added

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // user auth state is changed - user is null
            // launch login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        mDesET = (EditText) findViewById(R.id.des_et);
        mCurrentEt = (EditText) findViewById(R.id.current_et);
        mGoBtn = (Button) findViewById(R.id.go_btn);
        mRideBtn = (Button) findViewById(R.id.rideBtn);
        mCancelBtn = (Button) findViewById(R.id.cancelBtn);
        mSettings = (ImageView)findViewById(R.id.setting);
        mFindCard = (CardView)findViewById(R.id.findRideCard);
        mRideText = (TextView)findViewById(R.id.rideText);


        mFindCard.setVisibility(View.GONE);


        points = new ArrayList<LatLng>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);


        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFindCard.setVisibility(View.GONE);
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent setting = new Intent(MainActivity.this, SettingsActivity.class);

                startActivity(setting);
            }
        });


        mRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent ride = new Intent(MainActivity.this, RideActivity.class);

                startActivity(ride);
            }
        });

        mGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Geocoder.isPresent()) {
                    try {

                        String desLocation = "";

                        String curLocation = "";

                        if (!mDesET.getText().toString().equals("")) {

                            desLocation = mDesET.getText().toString();
                        }

                        if (!mCurrentEt.getText().toString().equals("")) {

                            curLocation = mCurrentEt.getText().toString();
                        }

                        mMap.clear();


                        LatLng origin = null;
                        LatLng destination = null ;

                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> addresses = gc.getFromLocationName(desLocation, 5); // get the found Address Objects

                        List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                        for (Address a : addresses) {
                            if (a.hasLatitude() && a.hasLongitude()) {
                                ll.add(new LatLng(a.getLatitude(), a.getLongitude()));


                                destination = new LatLng(a.getLatitude(), a.getLongitude());

                            }
                        }


                        List<Address> addresses1 = gc.getFromLocationName(curLocation, 5); // get the found Address Objects

                        List<LatLng> l2 = new ArrayList<LatLng>(addresses1.size()); // A list to save the coordinates if they are available
                        for (Address a : addresses1) {
                            if (a.hasLatitude() && a.hasLongitude()) {
                                l2.add(new LatLng(a.getLatitude(), a.getLongitude()));

                                origin = new LatLng(a.getLatitude(), a.getLongitude());

                            }
                        }



                        DrawRouteMaps.getInstance(MainActivity.this)
                                .draw(origin, destination, mMap);
                        DrawMarker.getInstance(MainActivity.this).draw(mMap, origin, R.drawable.markera, "Origin Location");
                        DrawMarker.getInstance(MainActivity.this).draw(mMap, destination, R.drawable.markerb, "Destination Location");

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(origin)
                                .include(destination).build();
                        Point displaySize = new Point();
                        getWindowManager().getDefaultDisplay().getSize(displaySize);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));



                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                mFindCard.setVisibility(View.VISIBLE);
                                mRideBtn.setText("Wait");
                                mRideBtn.setEnabled(false);

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        mRideText.setText("Ride Available 2 min away");
                                        mRideBtn.setText("Ride Now!");
                                        mRideBtn.setEnabled(true);

                                    }
                                }, 3000);

                            }
                        }, 3000);


                    } catch (IOException e) {
                        // handle the exception
                    }
                }

            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng myLoc = new LatLng(28.5943, 76.9086);
        mMap.addMarker(new MarkerOptions().position(myLoc).title("Cbpgec"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
        mMap.animateCamera(zoom);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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



}
