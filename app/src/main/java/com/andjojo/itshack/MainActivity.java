package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;
import com.andjojo.itshack.WebAPI.HandlePHPResult;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    MapView map = null;
    TextToSpeech textToSpeechSystem;
    LocationListener locationListener;
    LocationManager locationManager;
    Location currentLoc;
    ViewGroup frend;
    private static int USER_QUESTION = 0;
    private static int USER_ANSWER = 1;
    TextView textViewZeit;
    TextView textViewExtra;
    ScrollView scrollView;
    IMapController mapController;
    RideCanvas rideCanvas;
    Step currentStep;
    int stepNumber=-1;
    GeoPointListener geoPointListener;
    String currentInteractionId = "";
    Marker myMarker;
    MainActivity self;
    boolean debugLoc = false;
    String blockedInteractions="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

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

        ImageView button = (ImageView) findViewById(R.id.imageView3);
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
                    onListen(v);
                    return true;
                }
                return false;
            }
        });

        frend = (ViewGroup) findViewById(R.id.scrollayout);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        float[] myColorMatrix = {
                0f, 0, 0, 0, 255,        //red
                0, 0f, 0, 0, 255,//green
                0, 0, 0f, 0, 255,//blue
                0, 0, 0, 1.0f, 0 //alpha
        };
        ColorFilter myColorFilter = new ColorMatrixColorFilter(myColorMatrix);
        //map.getMapOverlay().setColorFilter(myColorFilter);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(16);
        myMarker = new Marker(map);
        //GeoPoint startPoint = new GeoPoint(53.551085, 9.993682);
        //mapController.setCenter(startPoint);

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);


        textViewZeit = (TextView) findViewById(R.id.textViewZeit);
        textViewExtra = (TextView) findViewById(R.id.textViewExtra);
        scrollView = (ScrollView) findViewById(R.id.scrollView);


        nextStep();
        startUpdateTimer();

        addGerdaSpeechBubble("Hallo. Ich bin Gerda deine Begleitung auf der heutigen Fahrt. Da ich ein Rotkehlchen kannst du von meinem tollen Orientierungssinn profitieren und wirst ganz sicher ankommen. Ich hoffe du freust dich schon auf unsere Reise. Wenn du Fragen hast dann drücke den Knopf mit dem Mikrophon und schieß los!");


        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location loc) {
                if (!debugLoc)changeLocationAction(loc.getLatitude(),loc.getLongitude());
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "Manifest.permission.READ_SMS") ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, "Manifest.permission.READ_SMS")) {

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

        rideCanvas = (RideCanvas) findViewById(R.id.ride);
        rideCanvas.draw();

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

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onListen(View v) {

        listen(USER_QUESTION);
    }

    public void listen(int mode) {
        /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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
        }*/
        // Intent to listen to user vocal input and return the result to the same activity.
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Use a language model based on free-form speech recognition.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getApplicationContext().getPackageName());
        ImageView btn = (ImageView) findViewById(R.id.imageView3);
        final int paddingBottom = btn.getPaddingBottom(), paddingLeft = btn.getPaddingLeft();
        final int paddingRight = btn.getPaddingRight(), paddingTop = btn.getPaddingTop();
        btn.setBackgroundResource(R.drawable.layout_bg_yellow_blue);
        btn.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        // Add custom listeners.
        CustomRecognitionListener listener = new CustomRecognitionListener(this,currentInteractionId,btn);
        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        sr.setRecognitionListener(listener);
        sr.startListening(intent);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK && data != null) {

                }
                break;

            }

        }
    }

    public void addGerdaSpeechBubble(String text) {
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.speech_bubble_gerda, frend, false);
        frend.addView(view);
        ViewGroup vg = (ViewGroup) (frend.getChildAt(frend.getChildCount() - 1));
        TextView tv = (TextView) vg.getChildAt(0);
        tv.setText(text);
            textToSpeechSystem = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        //Set<Voice> voiceSet = textToSpeechSystem.getVoices();
                        //Voice voice=null;
                        Set<String> a = new HashSet<>();
                        a.add("male");
                        Voice v = new Voice("de-ger-x-sfg#male_2-local", new Locale("de", "DE"), 500, 200, true, a);
                        List<TextToSpeech.EngineInfo> e = textToSpeechSystem.getEngines();
                        textToSpeechSystem.setSpeechRate(0.9f);
                        if(!GerdaVars.isDebug())textToSpeechSystem.speak(text, TextToSpeech.QUEUE_ADD, null);
                    }
                }

            });
        Handler handler = new Handler();
        int delay = 10; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, delay);

    }

    public void addUserSpeechBubble(String text) {
        final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.speech_bubble_user, frend, false);
        frend.addView(view);
        ViewGroup vg = (ViewGroup) (frend.getChildAt(frend.getChildCount() - 1));
        TextView tv = (TextView) vg.getChildAt(0);
        tv.setText(text);
        Handler handler = new Handler();
        int delay = 10; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, delay);
    }

    public void onFake(View v){
        if (GerdaVars.isDebug()){
            changeLocationAction(currentStep.getNextStationLat(),currentStep.getNextStationLon());
            debugLoc = true;
        }
    }

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public static double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
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

    public HandlePHPResult handlePHPResult = (s, url) -> {
        if (GerdaVars.isDebug()){
            addGerdaSpeechBubble(url.toString()+"\n \n"+s);
            if (s.contains("DOCTYPE")){
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("crash", url.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(),"Copied to Clipboard", Toast.LENGTH_SHORT).show();
            }
        }

        if (url.toString().contains("get_next_step")) {
            blockedInteractions = "";
            currentInteractionId = "";
            JSONObject jsonObject = new JSONObject(s);
            currentStep = new Step(jsonObject);
                if (!currentStep.getTransportType().equals("Umstieg")) {
                    newPolyline(currentStep.getLats(), currentStep.getLons());
                    for (int i = 0; i < currentStep.getStationLats().length; i++) {
                        GeoPoint geoPoint = new GeoPoint(currentStep.getStationLats()[i], currentStep.getStationLons()[i]);
                        if (i == 0) mapController.setCenter(geoPoint);
                        newMarker(geoPoint);
                        rideCanvas.setStep(currentStep);
                        rideCanvas.invalidate();
                    }
                    if (currentStep.getTransportType().equals("Laufen")) {
                        this.addGerdaSpeechBubble(currentStep.getCurrentInstruction(0));
                        geoPointListener = new GeoPointListener(this, currentStep);
                    } else {
                        geoPointListener = null;
                    }


                }
                else {
                    //TODO: ADD code for Umstieg
                    nextStep();
                }
        }
        else if (url.toString().contains("check_state")){
            JSONObject jsonObject = new JSONObject(s);
            Boolean isInteraction = jsonObject.getBoolean("is_interaction");
            if (!blockedInteractions.contains(jsonObject.getString("interaction_id"))) {
                currentInteractionId = jsonObject.getString("interaction_id");
                blockedInteractions = blockedInteractions+jsonObject.getString("interaction_id");
                addGerdaSpeechBubble(jsonObject.getString("interaction_text"));
                if (isInteraction) {
                    Handler handler = new Handler();
                    int delay = 5000; //milliseconds

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            listen(USER_ANSWER);
                        }
                    }, delay);
                }
            }
        }
        else if (url.toString().contains("gerda_interaction")){
            JSONObject jsonObject = new JSONObject(s);

            currentInteractionId = jsonObject.getString("interaction_id");
            addGerdaSpeechBubble(jsonObject.getString("text"));

            if (!currentInteractionId.equals("none")){
                Handler handler = new Handler();
                int delay = 5000; //milliseconds

                if (jsonObject.getString("interaction_id").equals("arrived_arv_station")) {
                    nextStep();
                }
                if (jsonObject.getBoolean("is_interaction")) {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            listen(USER_ANSWER);
                        }
                    }, delay);
                }
            }
        }
    };

    public void newPolyline(Float[] lats, Float[] lons) {

        List<GeoPoint> geoPoints = new ArrayList<>();

        for (int i = 0; i < lats.length; i++) {
            GeoPoint geoPoint = new GeoPoint(lats[i], lons[i]);
            if (i == 0) mapController.setCenter(geoPoint);
            geoPoints.add(geoPoint);
        }
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setColor(Color.rgb(34,68,89));
        map.getOverlayManager().add(line);
    }

    public void changeLocationAction(double lat, double lon){
        GeoPoint point = new GeoPoint(lat,lon);
        myMarker.setPosition(point);
        myMarker.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        myMarker.setTextLabelForegroundColor(
                Color.RED
        );
        myMarker.setTextLabelFontSize(40);
        myMarker.setIcon(getDrawable(R.drawable.smile));
        myMarker.setImage(getDrawable(R.drawable.smile));
        //m.setTextIcon("text");
        myMarker.setAnchor(0.19f, 0.33f);
        map.getOverlays().add(myMarker);
        map.invalidate();
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        mapController.setCenter(geoPoint);
        rideCanvas.setUserPos(currentStep.getPercentageOnRoute((float) lat, (float) lon, self));
        if(geoPointListener!=null)geoPointListener.listen(lat, lon);
        updateLocation(lat, lon);
    }

    public void newMarker(GeoPoint point) {
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

    public void startUpdateTimer(){
        Handler handler = new Handler();
        int delay = 10000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void updateLocation(double lat, double lon){
        URL url = null;
        try {
            url = new URL(GerdaVars.getURL()+"check_state/user_id="+GerdaVars.getUserId()+",track_id="+GerdaVars.getTrackId()+",lat="+lat+",lon="+lon+",current_step="+stepNumber);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url, handlePHPResult).execute("");
    }

    public void nextStep(){
        URL url = null;
        try {
            url = new URL(GerdaVars.getURL()+"get_next_step/user_id="+GerdaVars.getUserId()+",track_id="+GerdaVars.getTrackId()+",current_step="+stepNumber);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url, handlePHPResult).execute("");
        stepNumber++;
    }

}