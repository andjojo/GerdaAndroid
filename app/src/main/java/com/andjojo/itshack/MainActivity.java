package com.andjojo.itshack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;
import com.andjojo.itshack.WebAPI.HandlePHPResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MapView map = null;
    TextToSpeech textToSpeechSystem;
    LocationListener locationListener;
    LocationManager locationManager;
    Location currentLoc;
    JSONArray currentTrip;
    ViewGroup frend;
    int currentTripNum =0;
    private static int USER_QUESTION=0;
    private static int REACHED_STATION_ANSWER=1;
    private static int BOARDED_TRAIN_ANSWER=2;
    TextView textViewZeit;
    TextView textViewExtra;
    ScrollView scrollView;
    boolean onBoard=false;
    long nextEventinMillis;
    String lat_start="53.551085";
    String lon_start="9.993682";
    String lat_dest="48.784084";
    String lon_dest="9.181635";
    double lat_target,lon_target;
    double distance=0;
    String starttime="2019-11-02T17%3A19%3A23Z";
    String resturl = "fromLatitude="+lat_start+"&fromLongitude="+lon_start+"&provider=INNO&routingProvider=HERE&startTime="+starttime+"&toLatitude="+lat_dest+"&toLongitude="+lon_dest;

    int listenmode=0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        frend =(ViewGroup)  findViewById(R.id.scrollayout);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        float[] myColorMatrix ={
                0f,0,0,0,255,        //red
                0,0f,0,0,255,//green
                0,0,0f,0,255,//blue
                0,0,0,1.0f,0 //alpha
        };
        ColorFilter myColorFilter = new ColorMatrixColorFilter(myColorMatrix);
        //map.getMapOverlay().setColorFilter(myColorFilter);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(16);
        GeoPoint startPoint = new GeoPoint(53.551085, 9.993682);
        mapController.setCenter(startPoint);
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        //https://openrouteservice.org/


        textViewZeit=(TextView) findViewById(R.id.textViewZeit);
        textViewExtra=(TextView) findViewById(R.id.textViewExtra);
        scrollView=(ScrollView) findViewById(R.id.scrollView);



        Marker m = new Marker(map);
        m.setPosition(startPoint);
        m.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        m.setTextLabelForegroundColor(
                Color.RED
        );
        m.setTextLabelFontSize(40);
        m.setIcon(getDrawable(R.drawable.marker));
        m.setImage(getDrawable(R.drawable.marker));
        //m.setTextIcon("text");
        m.setAnchor(0.19f, 0.33f);
        map.getOverlays().add(m);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                currentLoc = loc;
                GeoPoint geoPoint = new GeoPoint(loc.getLatitude(),loc.getLongitude());
                mapController.setZoom(16);
                mapController.setCenter(geoPoint);
                int min=0;
                if (!onBoard&&!HandleJson.getTransportType(currentTrip,currentTripNum).equals("WALK"))min=1;
                if(false)reachedLocation();
                URL url = null;
                try {
                    url = new URL("http://10.1.141.165:5000/get_update/"+(currentTripNum-min)+",oma_erna,"+currentLoc.getLatitude()+","+currentLoc.getLongitude());
                    //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                textViewZeit.setText((int)((nextEventinMillis-System.currentTimeMillis())/1000/60)+" MIN");
                new DownloadFilesTask(url,handlePHPResult).execute("");
                distance=distanceInKmBetweenEarthCoordinates(currentLoc.getLatitude(),currentLoc.getLongitude(),lat_target,lon_target);
                int dis = (int) (distance*100);
                if (distance>1)textViewExtra.setText(((float)dis)/100+" KM");
                else textViewExtra.setText(((float)dis)+" M");
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
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {

// Permission is not granted
// Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,"Manifest.permission.READ_SMS") ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,"Manifest.permission.READ_SMS")) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{"Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS"},
                        1);

                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        else {
            // Permission has already been granted
        }



        URL url = null;
        try {
            url = new URL("https://innoapi-k8s01-dev-fcd.reisenden.info/2.7/routing/trip?"+resturl);
            //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
        url = null;
        try {
            url = new URL("http://10.1.141.165:5000/new_trip/"+lat_start+","+lon_start+","+lat_dest+","+lon_dest+","+starttime);
            //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");

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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

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

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }









    public void onListen(View v){

        listen(USER_QUESTION);
    }
    public void listen(int mode){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "JETZT SPRECHEN");
        try {
            startActivityForResult(intent, mode);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry your device not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK && data!=null) {
                    ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addUserSpeechBubble(result.get(0).toString());
                    String text = result.get(0).toString().replace(" ","_");


                    URL url = null;

                    try {
                        url = new URL("http://10.1.141.165:5000/dialog/"+text+","+currentTripNum);
                        //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    new DownloadFilesTask(url,handlePHPResult).execute("");

                }
                break;
            }
            case 1:{
                if (data!=null) {
                    ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addUserSpeechBubble(result.get(0).toString());
                    String text = result.get(0).toString().toLowerCase();
                    if (text.contains("ja")) {
                        currentTripNum++;

                        whattodo();
                        //TO DO: HIER SWITCHED DIE APP IN DEN NÄCHSTEN MODUS
                    } else {
                        addGerdaSpeechBubble("Sag mir dann doch kurz bescheid, wenn du da bist");
                    }
                }
                else {
                    addGerdaSpeechBubble("Sag mir dann doch kurz bescheid, wenn du da bist");
                }
                break;


            }
            case 2:{
                if (data!=null) {
                    ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    addUserSpeechBubble(result.get(0).toString());
                    String text = result.get(0).toString().toLowerCase();
                    if (text.contains("ja")) {
                        whattodo();
                        onBoard=true;
                    } else {
                        addGerdaSpeechBubble("Oh nein, dann berechne ich deine Route jetzt neu");
                    }
                }
                else {

                }


            }

        }
    }

    public void addGerdaSpeechBubble(String text){
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.speech_bubble_gerda, frend, false);
        frend.addView(view);
        ViewGroup vg = (ViewGroup) (frend.getChildAt(frend.getChildCount()-1));
        TextView tv = (TextView)vg.getChildAt(0);
        tv.setText(text);
        textToSpeechSystem = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechSystem.speak(text, TextToSpeech.QUEUE_ADD, null);
                }
            }
        });
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    public void addUserSpeechBubble(String text){
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.speech_bubble_user, frend, false);
        frend.addView(view);
        ViewGroup vg = (ViewGroup) (frend.getChildAt(frend.getChildCount()-1));
        TextView tv = (TextView)vg.getChildAt(0);
        tv.setText(text);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    public double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    public HandlePHPResult handlePHPResult=(s, url)->{
        if (url.toString().contains("journeyID")) {
            JSONObject myjson = null;
            try {
                myjson = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String type = HandleJson.getTransportType(currentTrip, currentTripNum);
            List<GeoPoint> geoPoints = HandleJson.getPolyline(currentTrip,currentTripNum);
            lat_target=geoPoints.get(geoPoints.size()-1).getLatitude();
            lon_target=geoPoints.get(geoPoints.size()-1).getLongitude();
            if (!type.equals("WALK")) {
                if (!onBoard) {
                    JSONArray segments = HandleJson.getSegments(myjson);
                    int time = HandleJson.getDepartureTime(segments, 0);
                    nextEventinMillis = System.currentTimeMillis()+time*1000;
                    textViewZeit.setText(time / 60 + " MIN");
                    textViewExtra.setText("GLEIS " + HandleJson.getTrack(segments, 0));
                    addGerdaSpeechBubble("Toll gemacht! Als nächstes nimmst du den " + HandleJson.getTrain(segments, 0) + " in " + time / 60 + " Minuten von Gleis " + HandleJson.getTrack(segments, 0));
                }
                else{
                    JSONArray segments = HandleJson.getSegments(myjson);
                    int time = HandleJson.getArrivalTime(segments, segments.length()-1);
                    nextEventinMillis = System.currentTimeMillis()+time*1000;
                    textViewZeit.setText(time / 60 + " MIN");
                    textViewExtra.setText("PÜNKTLICH");
                    addGerdaSpeechBubble("Super! Dann wünsche ich eine gute Fahrt. Du kannst jetzt für " + time / 60/60 + " Stunden entspannen. Ich informiere dich wenn etwas Unvorhergesehenes passieren sollte.");
                    makeBoardingRequest();

                }
            }
            else{
                if (onBoard==true){
                    makeLeavingRequest();
                }
                onBoard=false;
                addGerdaSpeechBubble("Folge der Karte zum nächsten Ziel.");
                if(!myjson.toString().equals("{\"journey\":null}")) {
                    JSONArray segments = HandleJson.getSegments(myjson);
                    int time = HandleJson.getDepartureTime(segments, 0);
                    nextEventinMillis = System.currentTimeMillis() + time * 1000;
                    textViewZeit.setText(time / 60 + " MIN");
                    textViewExtra.setText("LAUFEN");
                }
                }
            }
        else if (url.toString().contains("innoapi")||(url.toString().contains("get_update")&&!s.equals("0,0"))) {
            currentTripNum=0;
            map.getOverlayManager().clear();
            JSONObject myjson = null;
            try {
                myjson = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray trip = HandleJson.getTrip(myjson);
            currentTrip = trip;
            for (int i = 0; i < HandleJson.getNumberOfLegs(trip); i++) {
                List<GeoPoint> geoPoints = HandleJson.getPolyline(trip, i);
                Polyline line = new Polyline();   //see note below!
                line.setPoints(geoPoints);
                String journeyNum = HandleJson.getJourneyNum(trip,i);
                line.setOnClickListener(new Polyline.OnClickListener() {
                    @Override
                    public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                        Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
                line.setColor(HandleJson.getColorFromTrip(trip, i));
                IMapController mapController = map.getController();
                map.getOverlayManager().add(line);
                newMarker(geoPoints.get(0));
                newMarker(geoPoints.get(geoPoints.size() - 1));

            }
            whattodo();

        }
        else if(url.toString().contains("dialog")){
            addGerdaSpeechBubble(s);
        }
    };
    public void makeLeavingRequest(){
        URL url = null;
        try {
            url = new URL("http://10.1.141.165:5000/detach_user_from_journy/oma_erna,"+HandleJson.getJourneyNum(currentTrip,currentTripNum));
            //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
    }
    public void makeBoardingRequest(){
        URL url = null;
        try {
            url = new URL("http://10.1.141.165:5000/add_user_to_journy/oma_erna,"+HandleJson.getJourneyNum(currentTrip,currentTripNum));
            //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
    }

    public void newMarker(GeoPoint point){
        Marker m = new Marker(map);
        m.setPosition(point);
        m.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        m.setTextLabelForegroundColor(
                Color.RED
        );
        m.setTextLabelFontSize(40);
        m.setIcon(getDrawable(R.drawable.marker));
        m.setImage(getDrawable(R.drawable.marker));
        //m.setTextIcon("text");
        m.setAnchor(0.19f, 0.33f);
        map.getOverlays().add(m);
        map.invalidate();
    }

    public void reachedLocation(){
        addGerdaSpeechBubble("Bist du an der nächsten Station angekommen?");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listen(REACHED_STATION_ANSWER);
            }
        }, 3000);
    }
    public void boardedTrain(){
        addGerdaSpeechBubble("Bist du im Zug?");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listen(BOARDED_TRAIN_ANSWER);
            }
        }, 2000);

    }
    public void onFakeReach(View v){
        reachedLocation();
    }
    public void onFakeBoarding(View v){
        if(!onBoard)boardedTrain();
        else{
            addGerdaSpeechBubble("Deine Bahn hat leider Verspätung und der Anschluss kann nicht erreicht werden. Hier sind deine neuen Routeninfos ab Frankfurt.");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL("http://10.1.141.165:5000/get_update/"+currentTripNum+",oma_erna,"+currentLoc.getLatitude()+","+currentLoc.getLongitude());
                        //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    new DownloadFilesTask(url,handlePHPResult).execute("");
                }
            }, 4000);
        }
    }
    public void whattodo(){

        if (currentTripNum<HandleJson.getNumberOfLegs(currentTrip)) {
            URL url = null;
            int i=0;
            if (HandleJson.getTransportType(currentTrip,currentTripNum).equals("WALK"))i++;
            try {
                url = new URL("https://innoapi-k8s01-dev-fcd.reisenden.info/2.7/journey/byid?journeyID="+HandleJson.getJourneyNum(currentTrip,currentTripNum+i)+"&provider=INNO");
                //url = new URL("https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248dda1eabd45b145d09618ac19a99f42c3&start=8.681495,49.41461&end=9.993682,53.551085");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new DownloadFilesTask(url,handlePHPResult).execute("");
        }
        else{
            addGerdaSpeechBubble("Du bist angekommen! Dein Kontakt wurde automatisch informiert!");
            //SmsManager smsManager = SmsManager.getDefault();
            //smsManager.sendTextMessage("004917622818770", null, "Hallo, Ich bin angekommen", null, null);

        }
    }
}