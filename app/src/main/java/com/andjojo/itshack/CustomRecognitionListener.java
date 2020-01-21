package com.andjojo.itshack;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

    class CustomRecognitionListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";
        private MainActivity activity;
        private String interactionID;
        private ImageView btn;
        private SpeechCanvas sc;
        private boolean listen = true;


        public CustomRecognitionListener(MainActivity activity, String interactionID, ImageView btn){
            this.activity=activity;
            this.interactionID=interactionID;
            this.btn = btn;
            sc = (SpeechCanvas) activity.findViewById(R.id.speech);
        }

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Toast.makeText(activity.getApplicationContext(),rmsdB+"",Toast.LENGTH_SHORT);
            Log.d(TAG, "onRmsChanged");
            sc.onRmsChanged(rmsdB);
            if (listen){
                sc.setVisibility(View.VISIBLE);
                btn.setImageAlpha(0);
            }
            else{
                sc.setVisibility(View.INVISIBLE);
                btn.setImageAlpha(255);
            }

        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
            final int paddingBottom = btn.getPaddingBottom(), paddingLeft = btn.getPaddingLeft();
            final int paddingRight = btn.getPaddingRight(), paddingTop = btn.getPaddingTop();
            btn.setBackgroundResource(R.drawable.layout_bg_yellow);
            btn.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            sc.onRmsChanged(0.0f);
            listen = false;
        }

        public void onError(int error) {
            Log.e(TAG, "error " + error);
            final int paddingBottom = btn.getPaddingBottom(), paddingLeft = btn.getPaddingLeft();
            final int paddingRight = btn.getPaddingRight(), paddingTop = btn.getPaddingTop();
            btn.setBackgroundResource(R.drawable.layout_bg_yellow);
            btn.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            sc.onRmsChanged(0.0f);
            listen = false;
        }

        public void onResults(Bundle results) {
            Log.e(TAG, results.toString());
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            text += matches.get(0);
            text = text.replace(" ","_");
            activity.addUserSpeechBubble(text);
            if (text.contains("debug")||text.contains("Debug")){
                GerdaVars.debug = !GerdaVars.debug;
                activity.addGerdaSpeechBubble("Debug Mode: "+GerdaVars.debug.toString(),false);
            }
            else {
                URL url = null;
                try {
                    url = new URL(GerdaVars.getURL() + "gerda_interaction/user_id=" + GerdaVars.getUserId() + ",track_id=" + GerdaVars.getTrackId() + ",current_step=" + activity.stepNumber + ",interaction_id=" + interactionID + ",text=" + text);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                new DownloadFilesTask(url, activity.handlePHPResult).execute("");
            }
            sc.onRmsChanged(0.0f);
            listen = false;
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
}
