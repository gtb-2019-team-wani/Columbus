package com.example.columbus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,ActivityCompat.OnRequestPermissionsResultCallback,LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 0;
    private LocationManager myLocationManager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("test", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        ImageView btn_left = findViewById(R.id.left_button);
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), EmblemActivity.class);
                startActivity(intent);
            }
        });

        ImageView btn_right = findViewById(R.id.right_button);
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), SettingActivity.class);
                startActivity(intent);
            }
        });
    }



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    //マップ描画
    public void onMapReady(GoogleMap googleMap) {
        Log.i("test", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("なんかできないよ", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("原因不明だよ", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        //permissionチェック
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = getProvider();
            Location lastLocation = myLocationManager.getLastKnownLocation(provider);
            if(lastLocation != null) {
                setLocation(lastLocation);
                drawRoute();
            }
            mMap.setMyLocationEnabled(true);
            myLocationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);
        } else {
            setDefaultLocation();
            confirmPermission();
        }
    }



    //permission許可されてるかとってくる
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            //許可されてる
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                myLocationManager.requestLocationUpdates(getProvider(), 0, 0, (LocationListener) this);
            }
            //許可されてない
            else {
            }
        }
    }

    //ピンをぶっさす位置変更
    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
        try {
            myLocationManager.removeUpdates((LocationListener) this);
        } catch(SecurityException e) {
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            myLocationManager.removeUpdates((LocationListener) this);
        } catch(SecurityException e) {
        }
    }

    private String getProvider() {
        Criteria criteria = new Criteria();
        return myLocationManager.getBestProvider(criteria, true);
    }

    //現在地とっていいか確認
    private void confirmPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_location_rationale_title)
                    .setMessage(R.string.permission_location_rationale)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    //初期位置
    private void setDefaultLocation() {
        LatLng tokyo = new LatLng(35.681298, 139.766247);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 18));
    }

    //新しい位置を格納
    private void setLocation(Location location) {
        LatLng myLocation = new LatLng(35.6550,139.6998);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }

    private void drawRoute(){
        Polyline options = mMap.addPolyline((new PolylineOptions())
          .add(
                  new LatLng(35.6563229,139.6994060),
                  new LatLng(35.6563228,139.6994060),
                  new LatLng(35.6563228,139.6994059),
                  new LatLng(35.6550,139.6998)
          ).color(Color.rgb(255,225,0))
        );
    }
}
