package com.andjojo.itshack.WebAPI;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadFilesTask extends AsyncTask<String, Void, String> {
    URL url;
    String s;
    HandlePHPResult handlePHPResult;
    public  DownloadFilesTask(URL url, HandlePHPResult handlePHPResult) {
        this.url=url;
        this.handlePHPResult=handlePHPResult;
    }

    protected String doInBackground(String... params) {
        String responseString = null;

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httppost = new HttpGet(url.toString());
            httppost.setHeader("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
            httppost.setHeader("Authorization", "Basic YXBpdXNlcjpnZWhlaW0xMjM0");
            //httppost.setHeader("accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("id", "12345"));
                nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
                //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));



                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                s = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block

            } catch (IOException e) {
                // TODO Auto-generated catch block

            }
            //code to do the HTTP request
        }finally {
        }
        return s;
    }


    @Override
    protected void onPostExecute(String result) {
        if (s!=null) {
            try {
                handlePHPResult.handle(s,url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
        }
    }
}
