package com.andjojo.itshack;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

    class CustomRecognitionListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";
        private MainActivity activity;

        public CustomRecognitionListener(MainActivity activity){
            this.activity=activity;
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
        }

        public void onError(int error) {
            Log.e(TAG, "error " + error);

        }

        public void onResults(Bundle results) {
            Log.e(TAG, results.toString());
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            for (String result : matches)
                text += result + "\n";
            activity.addUserSpeechBubble(text);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
}
