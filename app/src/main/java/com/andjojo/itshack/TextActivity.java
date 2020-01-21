package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class TextActivity extends AppCompatActivity {

    ProgressDialog dialog;
    EditText startText,destinationText;
    Switch locSwitch;
    LocationListener locationListener;
    LocationManager locationManager;
    ImageView imstart,imdest;
    TextView stateText;
    String currentloc;
    Button button;
    int mode = 1;
    boolean quer=true;
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        startText=(EditText)findViewById(R.id.editText2);
        stateText=(TextView) findViewById(R.id.textView2);
        imstart = (ImageView) findViewById(R.id.imageView2);
        imdest = (ImageView) findViewById(R.id.imageView5);
        destinationText=(EditText)findViewById(R.id.editText);
        locSwitch = (Switch) findViewById(R.id.switch2);
        requestRecordAudioPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPref = getApplicationContext().getSharedPreferences("internal", Context.MODE_PRIVATE);
        String track = sharedPref.getString("trackid", "null");
        if (!track.equals("null")){
            quer = true;
            GerdaVars.setStartAdress(startText.getText().toString().replace(" ", "_"));
            GerdaVars.setDestinationAdress(destinationText.getText().toString().replace(" ", "_"));
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            GerdaVars.setUserId(android_id);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd---HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());


            GerdaVars.setStartTime(currentDateandTime.replace("---", "T"));
            String var = "0";
            if (locSwitch.isChecked()) var = "1";
            URL url = null;
            try {
                url = new URL(GerdaVars.getURL() + "route/user_id=" + GerdaVars.getUserId() + ",dep_name=" + GerdaVars.getStartAdress() + ",arr_name=" + GerdaVars.getDestinationAdress() + ",date_time=" + GerdaVars.getStartTime() + ",dep_is_lat_lon=" + var);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new DownloadFilesTask(url, handlePHPResult).execute("");
            dialog = ProgressDialog.show(this, "",
                    "Route wird geladen...", true);
        }else{
            quer = false;
        }

        button = (Button) findViewById(R.id.button2);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    v.animate()
                            .scaleXBy(-0.04f)
                            .scaleYBy(-0.04f)
                            .setDuration(200)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {


                                }
                            });
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){

                    v.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(200);
                    getNewRoute(v);
                    return true;
                }
                return false;
            }
        });
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
    public void onBackPressed(){
        Animation animation = new TranslateAnimation(-1500, 0,0, 0);
        animation.setDuration(1000);
        startText.startAnimation(animation);
        imstart.startAnimation(animation);
        locSwitch.startAnimation(animation);
        imstart.setVisibility(View.VISIBLE);
        startText.setVisibility(View.VISIBLE);
        locSwitch.setVisibility(View.VISIBLE);
        stateText.setText("Von");
        stateText.startAnimation(animation);

        destinationText.setVisibility(View.INVISIBLE);
        imdest.setVisibility(View.INVISIBLE);
        Animation animation2 = new TranslateAnimation(0, 1500,0, 0);
        animation2.setDuration(1000);
        animation2.setFillAfter(true);
        destinationText.startAnimation(animation2);
        imdest.startAnimation(animation2);

        mode=1;
    }
    public void getNewRoute(View v){
        if (mode == 1){
            Animation animation = new TranslateAnimation(0, -1500,0, 0);
            animation.setDuration(1000);
            startText.startAnimation(animation);
            imstart.startAnimation(animation);
            locSwitch.startAnimation(animation);
            imstart.setVisibility(View.INVISIBLE);
            locSwitch.setVisibility(View.INVISIBLE);
            startText.setVisibility(View.INVISIBLE);
            stateText.setText("Nach");
            button.setText("Weiter");

            destinationText.setVisibility(View.VISIBLE);
            imdest.setVisibility(View.VISIBLE);
            Animation animation2 = new TranslateAnimation(1500, 0,0, 0);
            animation2.setDuration(1000);
            animation2.setFillAfter(true);
            destinationText.startAnimation(animation2);
            imdest.startAnimation(animation2);
            stateText.startAnimation(animation2);
            mode=3;
        }
        else if (mode==3) {
            GerdaVars.setStartAdress(startText.getText().toString().replace(" ", "_"));
            GerdaVars.setDestinationAdress(destinationText.getText().toString().replace(" ", "_"));
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            GerdaVars.setUserId(android_id);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd---HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());


            GerdaVars.setStartTime(currentDateandTime.replace("---", "T"));
            String var = "0";
            if (locSwitch.isChecked()) var = "1";
            URL url = null;
            try {
                url = new URL(GerdaVars.getURL() + "route/user_id=" + GerdaVars.getUserId() + ",dep_name=" + GerdaVars.getStartAdress() + ",arr_name=" + GerdaVars.getDestinationAdress() + ",date_time=" + GerdaVars.getStartTime() + ",dep_is_lat_lon=" + var);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new DownloadFilesTask(url, handlePHPResult).execute("");
            dialog = ProgressDialog.show(this, "",
                    "Route wird geladen...", true);
        }
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }




    public HandlePHPResult handlePHPResult=(s,url)->{
        dialog.dismiss();
        JSONArray jsonRoute = new JSONArray(s);
        if (quer) {
            GerdaVars.setQuereinstiegRoute(jsonRoute,sharedPref);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            GerdaVars.setRoute(jsonRoute,sharedPref);
            Intent intent = new Intent(this, OverviewActivity.class);
            startActivity(intent);
        }

    };

}