package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        //TIME TO TRAVEL!
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
