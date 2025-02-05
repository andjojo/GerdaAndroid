package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;
import com.andjojo.itshack.WebAPI.HandlePHPResult;

import org.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;

public class OverviewActivity extends AppCompatActivity {

    OverviewCanvas overviewCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        OverviewCanvas overviewCanvas = (OverviewCanvas) findViewById(R.id.overview);
        overviewCanvas.setStations(GerdaVars.getStations());
        overviewCanvas.setSteps(GerdaVars.getSteps());
        overviewCanvas.draw();
        Button button = (Button) findViewById(R.id.button);
        Button buttonShare = (Button) findViewById(R.id.button3);
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
                    onGo(v);
                    return true;
                }
                return false;
            }
        });
        buttonShare.setOnTouchListener(new View.OnTouchListener() {
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
                    onShare(v);
                    return true;
                }
                return false;
            }
        });

    }
    public void onGo(View v){
        URL url = null;
        try {
            url = new URL(GerdaVars.getURL()+"check_update/user_id="+GerdaVars.getUserId()+",track_id="+GerdaVars.getTrackId());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
        //TIME TO TRAVEL!
    }
    public void onShare(View v){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reise Teilen");
        String shareMessage= "Verfolge mich auf meiner Reise:\n\n";
        shareMessage = shareMessage + "http://18.232.116.237:3434/?user_id="+ GerdaVars.getUserId()+"&track_id="+GerdaVars.trackId;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Wie teilen?"));
    }

    public HandlePHPResult handlePHPResult=(s, url)->{
        //dialog.dismiss();
        //TODO: Still missing JSON Handling
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    };
}
