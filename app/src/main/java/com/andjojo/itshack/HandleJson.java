package com.andjojo.itshack;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HandleJson {
    public static String getTransportType(JSONArray trips, int tripNum){
        List<GeoPoint> geoPoints = new ArrayList<>();
        JSONArray legs=null;
        String transportType=null;
        int color = Color.BLACK;
        try {
            JSONObject jsonArray = trips.getJSONObject(0);
            legs = jsonArray.getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonArray = legs.getJSONObject(tripNum);
            transportType = jsonArray.getString("transportType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transportType;
    }
    public static JSONArray getTrip(JSONObject myjson){
        JSONArray trips=null;
        try {
            trips = myjson.getJSONArray("trips");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trips;
    }
    public static int getNumberOfLegs(JSONArray trips){
        List<GeoPoint> geoPoints = new ArrayList<>();
        JSONArray legs=null;
        String transportType=null;
        int i=0;
        try {
            JSONObject jsonArray = trips.getJSONObject(0);
            legs = jsonArray.getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        i=legs.length();
        return i;
    }
    public static int getColorFromTrip(JSONArray trips,int tripNum){
        List<GeoPoint> geoPoints = new ArrayList<>();
        JSONArray legs=null;
        String transportType=HandleJson.getTransportType(trips,tripNum);
        int color = Color.BLACK;
        if (transportType==null){
            color=Color.BLACK;
        }
        else if (transportType.equals("HIGH_SPEED_TRAIN")||transportType.equals("INTERCITY_TRAIN")){
            color=Color.WHITE;
        }
        else if (transportType.equals("REGIONAL_TRAIN")){
            color=Color.RED;
        }

        return color;
    }
    public static List<GeoPoint> getPolyline(JSONArray trips,int tripNum){
        List<GeoPoint> geoPoints = new ArrayList<>();
        JSONArray legs=null;
        JSONArray coordinates=null;
        String transportType=null;
        JSONObject origin=null;
        JSONObject destination=null;
        try {
            JSONObject jsonArray = trips.getJSONObject(0);
            legs = jsonArray.getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonArray = legs.getJSONObject(tripNum);
            coordinates = jsonArray.getJSONArray("polyline");
            JSONObject jsonObject = jsonArray.getJSONObject("destination");
            destination = jsonObject.getJSONObject("geo");
            jsonObject = jsonArray.getJSONObject("origin");
            origin = jsonObject.getJSONObject("geo");
            transportType = jsonArray.getString("transportType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (transportType.equals("WALK")){
            double lon = 0;
            double lat =0;
            double lon1 = 0;
            double lat1 =0;
            try {
                lon1 = origin.getDouble("longitude");
                lat1 = origin.getDouble("latitude");
                lon = destination.getDouble("longitude");
                lat = destination.getDouble("latitude");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            GeoPoint point = new GeoPoint(lat1, lon1);
            geoPoints.add(point);
            point = new GeoPoint(lat, lon);
            geoPoints.add(point);
        }
        else {
            for (int i = 0; i < coordinates.length(); i++) {
                try {
                    JSONObject jsonObject = coordinates.getJSONObject(i);
                    double lon = jsonObject.getDouble("longitude");
                    double lat = jsonObject.getDouble("latitude");
                    GeoPoint point = new GeoPoint(lat, lon);
                    geoPoints.add(point);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return geoPoints;
    }
    public static String getJourneyNum(JSONArray trips, int tripNum){
        List<GeoPoint> geoPoints = new ArrayList<>();
        JSONArray legs=null;
        String journeyNum=null;
        int color = Color.BLACK;
        try {
            JSONObject jsonArray = trips.getJSONObject(0);
            legs = jsonArray.getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonArray = legs.getJSONObject(tripNum);
            String type = jsonArray.getString("transportType");
            journeyNum = jsonArray.getString("journeyID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return journeyNum;// "9965690f-725e-351b-950b-e9243f706587";
    }

    public static JSONArray getSegments(JSONObject myjson){
        JSONArray segments=null;
        try {
            JSONObject jsonArray= myjson.getJSONObject("journey");
            segments = jsonArray.getJSONArray("segments");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return segments;
    }
    public static int getDepartureTime(JSONArray segments, int segNum){
        int time=0;
        try {
            JSONObject jsonObject= segments.getJSONObject(segNum);
            JSONObject departure = jsonObject.getJSONObject("departure");
            String timeActual = departure.getString("timeActual");
            Date date = new Date();
            String s =timeActual.substring(0,4);
            date.setYear(Integer.valueOf(timeActual.substring(0,4))-1900);
            date.setMonth(Integer.valueOf(timeActual.substring(5,7))-1);
            date.setDate(Integer.valueOf(timeActual.substring(8,10)));
            date.setHours(Integer.valueOf(timeActual.substring(11,13)));
            date.setMinutes(Integer.valueOf(timeActual.substring(14,16)));
            Date currentDate = new Date(System.currentTimeMillis());
            time = (int)(date.getTime()-currentDate.getTime())/1000;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return time;
    }
    public static int getArrivalTime(JSONArray segments, int segNum){
        int time=0;
        try {
            JSONObject jsonObject= segments.getJSONObject(segNum);
            JSONObject departure = jsonObject.getJSONObject("arrival");
            String timeActual = departure.getString("timeActual");
            Date date = new Date();
            String s =timeActual.substring(0,4);
            date.setYear(Integer.valueOf(timeActual.substring(0,4))-1900);
            date.setMonth(Integer.valueOf(timeActual.substring(5,7))-1);
            date.setDate(Integer.valueOf(timeActual.substring(8,10)));
            date.setHours(Integer.valueOf(timeActual.substring(11,13)));
            date.setMinutes(Integer.valueOf(timeActual.substring(14,16)));
            Date currentDate = new Date(System.currentTimeMillis());
            time = (int)(date.getTime()-currentDate.getTime())/1000;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return time;
    }
    public static int getTrack(JSONArray segments, int segNum){
        int track=0;
        try {
            JSONObject jsonObject= segments.getJSONObject(segNum);
            JSONObject departure = jsonObject.getJSONObject("departure");
            track = departure.getInt("trackActual");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return track;
    }
    public static String getTrain(JSONArray segments, int segNum){
        String trainID="";
        try {
            JSONObject jsonObject= segments.getJSONObject(segNum);
            JSONObject departure = jsonObject.getJSONObject("departure");
            JSONObject train = departure.getJSONObject("train");
            trainID = train.getString("trainID");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trainID;
    }

}
