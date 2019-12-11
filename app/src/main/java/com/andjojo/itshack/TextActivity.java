package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;
import com.andjojo.itshack.WebAPI.HandlePHPResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class TextActivity extends AppCompatActivity {

    ProgressDialog dialog;
    EditText startText,destinationText;
    Switch debugSwitch,locSwitch;
    LocationListener locationListener;
    LocationManager locationManager;
    String currentloc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        startText=(EditText)findViewById(R.id.editText2);
        destinationText=(EditText)findViewById(R.id.editText);
        debugSwitch = (Switch) findViewById(R.id.switch1);
        locSwitch = (Switch) findViewById(R.id.switch2);
        requestRecordAudioPermission();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location loc) {
                currentloc = loc.getLatitude()+"_"+loc.getLongitude();
                if (locSwitch.isChecked())startText.setText(currentloc);
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
        };
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void getNewRoute(View v){
        GerdaVars.setStartAdress(startText.getText().toString().replace(" ","_"));
        GerdaVars.setDestinationAdress(destinationText.getText().toString().replace(" ","_"));
        GerdaVars.setUserId("oma_erna_"+System.currentTimeMillis());
        GerdaVars.setDebug(debugSwitch.isChecked());
        String var = "0";
        if (locSwitch.isChecked()) var = "1";
        URL url = null;
        try {
            url = new URL(GerdaVars.getURL()+"route/user_id="+GerdaVars.getUserId()+",dep_name="+GerdaVars.getStartAdress()+",arr_name="+GerdaVars.getDestinationAdress()+",date_time="+GerdaVars.getStartTime()+",dep_is_lat_lon="+var);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
        dialog = ProgressDialog.show(this, "",
                "Route wird geladen...", true);
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }




    public HandlePHPResult handlePHPResult=(s,url)->{
        dialog.dismiss();
        JSONArray jsonRoute = new JSONArray(s);
        GerdaVars.setRoute(jsonRoute);
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
    };

}