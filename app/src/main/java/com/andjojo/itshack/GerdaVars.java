package com.andjojo.itshack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GerdaVars {

    private static String URL="http://3.84.55.152:5001/api/";
    private static String userId="oma_4";
    private static String startAdress="Hegnach_Aldingerstraße";
    private static String destinationAdress="Hamburg";
    private static String startTime="2019_11_11T11:00:00";
    public static String stations[];
    public static String steps[];
    public static JSONArray route;
    public static String trackId="12345";
    public  static Boolean debug=false;

    public static String getURL(){
        return URL;
    }

    public static String getUserId(){
        return userId;
    }

    public static void setUserId(String Id){
        userId=Id;
    }

    public static String getStartAdress(){
        return startAdress;
    }

    public static String getDestinationAdress(){
        return destinationAdress;
    }

    public static void setStartAdress(String Adress){
        startAdress=Adress;
    }

    public static void setDestinationAdress(String Adress){
        destinationAdress=Adress;
    }

    public static String getStartTime(){
        return startTime;
    }

    public static void setStations(String[] stationString){
        stations=stationString;
    }

    public static void setSteps(String[] stepsString){
        steps=stepsString;
    }

    public static String[] getSteps(){
        return steps;
    }

    public static String[] getStations(){
       return stations;
    }

    public static Boolean isDebug(){
        return debug;
    }

    public static void setDebug(Boolean isdebug){
        debug = isdebug;
    }

    public static void setTrackId(String id){
       trackId = id;
    }

    public static String getTrackId(){
        return trackId;
    }

    public static void setRoute(JSONArray jsonRoute) throws JSONException {
        route = jsonRoute;
        String stations[] = new String[jsonRoute.length()+1];
        String steps[] = new String[jsonRoute.length()];

        for (int i=0;i<jsonRoute.length();i++){
            JSONObject jsonObject = jsonRoute.getJSONObject(i);
            stations[i] = jsonObject.getString("dep_name");
            steps[i] = jsonObject.getString("transport_type");
        }


        JSONObject jsonObject = jsonRoute.getJSONObject(jsonRoute.length()-1);
        stations[jsonRoute.length()] = jsonObject.getString("arv_name");
        setTrackId(jsonObject.getString("track_id"));


        setStations(stations);
        setSteps(steps);
    }

}
