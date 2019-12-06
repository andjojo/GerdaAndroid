package com.andjojo.itshack;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import com.andjojo.itshack.WebAPI.DownloadFilesTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

    class CustomRecognitionListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";
        private MainActivity activity;
        private String interactionID;
        private ImageButton btn;


        public CustomRecognitionListener(MainActivity activity, String interactionID, ImageButton btn){
            this.activity=activity;
            this.interactionID=interactionID;
            this.btn = btn;
        }

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
            btn.setBackgroundResource(R.drawable.layout_bg_yellow);
        }

        public void onError(int error) {
            Log.e(TAG, "error " + error);
        }

        public void onResults(Bundle results) {
            Log.e(TAG, results.toString());
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            text += matches.get(0);
            text = text.replace(" ","_");
            activity.addUserSpeechBubble(text);
            URL url = null;
            try {
                url = new URL("http://3.84.55.152:5001/api/gerda_interaction/user_id="+GerdaVars.getUserId()+",track_id="+GerdaVars.getTrackId()+",current_step="+activity.stepNumber+",interaction_id="+interactionID+",text="+text);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            new DownloadFilesTask(url, activity.handlePHPResult).execute("");
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
}
