package com.andjojo.itshack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Step {



    private Float[] lats;
    private Float[] lons;
    private String departureTime,arrivalTime;
    private JSONObject jsonObject;


    public Step(JSONObject jsonObject) throws JSONException {
        this.jsonObject = jsonObject;
        lats = new Float[2];
        lons = new Float[2];
        lats[0]= Float.valueOf(jsonObject.getString("step_dep_lat"));
        lons[0]= Float.valueOf(jsonObject.getString("step_dep_lon"));
        lats[1]= Float.valueOf(jsonObject.getString("step_arv_lat"));
        lons[1]= Float.valueOf(jsonObject.getString("step_arv_lon"));

    }

    public Float[] getLats(){
        return lats;
    }
    public Float[] getLons(){
        return lons;
    }

    public String getDepartureTime(){
        return departureTime;
    }

    public String getArrivalTime(){
        return arrivalTime;
    }


    /*public static void setRoute(JSONArray jsonRoute) throws JSONException {
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
        trackId = jsonObject.getString("track_id");


        setStations(stations);
        setSteps(steps);
    }*/

}
