package com.example.columbus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,ActivityCompat.OnRequestPermissionsResultCallback,LocationListener {

    public static final int MY_LOCATION_REQUEST_CODE = 0;
    private LocationManager myLocationManager;
    private GoogleMap mMap;
    public static final int PREFERENCE_INIT = 0;
    public static final int PREFERENCE_BOOTED = 1;
    public static final int PREFERENCE_PARMISSION = 2;
    public static final int PREFERENCE_USENG = 3;
    public static final int MENU_SELECT_CLEAR = 0;

    //データ保存
    private void setState(int state) {
        // SharedPreferences設定を保存
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("InitState", state).commit();

    }
    //データ読み出し
    private int getState() {
        // 読み込み
        int state;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        state = sp.getInt("InitState", PREFERENCE_INIT);

        return state;
    }

    //ダイアログ表示
    @Override
    public void onResume(){
        super.onResume();
    }

    ////////////////////////////////////////////////////////////////
    //初回のデータを削除するところ
    //メニュー作成
    public boolean onCreateOptionsMenu(Menu menu){
        //Clearボタンの追加
        menu.add(0, MENU_SELECT_CLEAR, 0, "Clear")
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        return true;
    }

    //メニュー実行時の処
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SELECT_CLEAR:
                //状態を忘れる
                setState(PREFERENCE_INIT);
                return true;
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        //左のボタンを押したときの処理
        ImageView btn_left = findViewById(R.id.left_button);
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), EmblemActivity.class);
                startActivity(intent);

            }
        });

        //右のボタンを押したときの処理
        ImageView btn_right = findViewById(R.id.right_button);
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), SettingActivity.class);
                startActivity(intent);

            }
        });
            /* if (getState() == PREFERENCE_INIT) {

                setState(PREFERENCE_BOOTED);
                Intent intent = new Intent(getApplication(), ThankActivity.class);
                startActivity(intent);
            } */
        //Intent intent = new Intent(getApplication(), ThankActivity.class);
        //startActivity(intent);
    }



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

                //helpに飛ばす
                //setStatehelp
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
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
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
        LatLng myLocation = new LatLng(35.656309,139.699444);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }

    private void drawRoute(){
        ArrayList<LatLng> positions = new ArrayList<>();
        //Line表示する緯度経度の設定
        //最後の地点と現在地は同じにする
        positions.add(new LatLng(35.658032,139.701295));
        positions.add(new LatLng(35.657295,139.701124));
        positions.add(new LatLng(35.656688,139.699310));
        positions.add(new LatLng(35.656309	,139.699444));

        Polyline options = mMap.addPolyline((new PolylineOptions())
          .addAll(positions).color(Color.rgb(241,142,142)).width(40)
        );

/*        float[] result = new float[positions.size()];
        for(int i = 0; i < positions.size(); i++){
            Location.distanceBetween(positions.get(i).latitude,positions.get(i).longitude,positions.get(i+1).latitude,positions.get(i+1).longitude,result);
        }*/
    }
}
