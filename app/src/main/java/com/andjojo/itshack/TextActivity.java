package com.andjojo.itshack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class TextActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

    }
    public void getRout(View v){
        URL url = null;
        try {
            url = new URL("http://10.1.141.165:5000/get_update/walk,50_324_39234,30_234_3941");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFilesTask(url,handlePHPResult).execute("");
    }

    public void openMap(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public HandlePHPResult handlePHPResult=(s,url)->{





    };

}