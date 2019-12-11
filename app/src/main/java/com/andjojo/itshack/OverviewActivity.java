package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    public HandlePHPResult handlePHPResult=(s, url)->{
        //dialog.dismiss();
        //TODO: Still missing JSON Handling
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    };
}
