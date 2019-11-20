package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;
import com.andjojo.itshack.WebAPI.HandlePHPResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class TextActivity extends AppCompatActivity {

    ProgressDialog dialog;
    EditText startText,destinationText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        startText=(EditText)findViewById(R.id.editText2);
        destinationText=(EditText)findViewById(R.id.editText);
        requestRecordAudioPermission();

    }
    public void getNewRoute(View v){
        GerdaVars.setStartAdress(startText.getText().toString().replace(" ","_"));
        GerdaVars.setDestinationAdress(destinationText.getText().toString().replace(" ","_"));
        URL url = null;
        try {
            url = new URL(GerdaVars.getURL()+"route/user_id="+GerdaVars.getUserId()+",dep_name="+GerdaVars.getStartAdress()+",arr_name="+GerdaVars.getDestinationAdress()+",date_time="+GerdaVars.getStartTime());
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