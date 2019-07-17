package com.example.columbus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 0;
    private LocationManager myLocationManager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    //マップ描画
    public void onMapReady(GoogleMap googleMap) {
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
            }
            mMap.setMyLocationEnabled(true);
            Toast.makeText(this, "Provider=" + provider, Toast.LENGTH_SHORT).show();
            myLocationManager.requestLocationUpdates(provider, 0, 0, this);
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
                myLocationManager.requestLocationUpdates(getProvider(), 0, 0, this);
            }
            //許可されてない
            else {
                Toast.makeText(this, getString(R.string.permission_location_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //ピンをぶっさす位置変更
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LocationChanged実行" , Toast.LENGTH_SHORT).show();
        setLocation(location);
        try {
            myLocationManager.removeUpdates(this);
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
            myLocationManager.removeUpdates(this);
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
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
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
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }
}