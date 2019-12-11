package com.andjojo.itshack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Step {


    private Float[] lats;
    private Float[] lons;
    private String[] StationNames;
    private Float[] StationPercentage;
    private Float[] StationLats;
    private Float[] StationLons;
    private String departureTime,arrivalTime;
    private JSONObject jsonObject;
    private String transportType;
    private String stepManeuver;
    private String[] maneuvers;
    private double nextStationLat;
    private double nextStationLon;
    private int stationnum = 0;


    public Step(JSONObject jsonObject) throws JSONException {
        this.jsonObject = jsonObject;
        //lats = new Float[2];
        //lons = new Float[2];
        String polyline = jsonObject.getString("step_polyline");
        String firstsplit[] = polyline.split(" ");
        if (!polyline.equals("None")) {
            lats = new Float[firstsplit.length];
            lons = new Float[firstsplit.length];
            for (int i = 0; i < firstsplit.length; i++) {
                String secondsplit[] = firstsplit[i].split(",");
                lats[i] = Float.valueOf(secondsplit[0]);
                lons[i] = Float.valueOf(secondsplit[1]);
            }
        }


        transportType = jsonObject.getString("transport_type");
        if (!transportType.equals("Laufen")&&!transportType.equals("Umstieg")) {
            String allStationsOnStep = jsonObject.getString("all_stations_on_step");
            String firststationsplit[] = allStationsOnStep.split(";");
            StationLats = new Float[firststationsplit.length];
            StationLons = new Float[firststationsplit.length];
            StationNames = new String[firststationsplit.length];
            StationPercentage = new Float[firststationsplit.length];

            for (int i = 0; i < firststationsplit.length; i++) {
                String secondsplit[] = firststationsplit[i].split(",");
                StationNames[i] = secondsplit[0];
                StationLats[i] = Float.valueOf(secondsplit[1]);
                StationLons[i] = Float.valueOf(secondsplit[2]);
                StationPercentage[i] = getPercentageOnRoute(StationLats[i], StationLons[i],null);
                if (i == 0){
                    nextStationLat = StationLats[0];
                    nextStationLon = StationLons[0];
                }
            }
        }else{
            stepManeuver = jsonObject.getString("step_maneuver");
            if (!stepManeuver.equals("None")) {
                JSONArray jsonArray = new JSONArray(stepManeuver);
                maneuvers = new String[jsonArray.length()];
                StationLats = new Float[jsonArray.length()];
                StationLons = new Float[jsonArray.length()];
                StationNames = new String[jsonArray.length()];
                StationPercentage = new Float[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonManeuver = jsonArray.getJSONObject(i);
                    maneuvers[i] = jsonManeuver.getString("instruction");
                    String graph = jsonManeuver.getString("graph");
                    String points[] = graph.split(" ");
                    String secondsplit[] = points[points.length - 1].split(",");
                    StationLats[i] = Float.valueOf(secondsplit[0]);
                    StationLons[i] = Float.valueOf(secondsplit[1]);
                    if (i == 0){
                        nextStationLat = StationLats[0];
                        nextStationLon = StationLons[0];
                    }
                }
            }
        }



        /*lats[0]= Float.valueOf(jsonObject.getString("step_dep_lat"));
        lons[0]= Float.valueOf(jsonObject.getString("step_dep_lon"));
        lats[1]= Float.valueOf(jsonObject.getString("step_arv_lat"));
        lons[1]= Float.valueOf(jsonObject.getString("step_arv_lon"));*/

    }
    public String getTransportType(){
        return transportType;
    }

    public Float[] getLats(){
        return lats;
    }
    public Float[] getLons(){
        return lons;
    }

    public String getCurrentInstruction(int i){
        return maneuvers[i];
    }

    public Float[] getStationLats(){
        return StationLats;
    }
    public Float[] getStationLons(){
        return StationLons;
    }
    public String[] getStationNames(){
        return StationNames;
    }
    public Float[] getStationPercentage(){
        return StationPercentage;
    }

    public String getDepartureTime(){
        return departureTime;
    }

    public String getArrivalTime(){
        return arrivalTime;
    }

   public Float getPercentageOnRoute(Float lat,Float lon,MainActivity activity){
        int shortespoint=0;
        double distance=300;
        if (transportType!="Umstieg") {
            for (int i = 0; i < lats.length; i++) {
                double temp_distance = MainActivity.distanceInKmBetweenEarthCoordinates(lat, lon, lats[i], lons[i]);
                if (temp_distance < distance) {
                    shortespoint = i;
                    distance = temp_distance;
                }
            }
            if (activity != null) {
                if (!transportType.equals("Laufen")) {
                    nextStationLat = StationLats[StationLats.length - 1];
                    nextStationLon = StationLons[StationLons.length - 1];
                }else{
                    if (StationLats.length>stationnum+1) stationnum++;
                    nextStationLat = StationLats[stationnum];
                    nextStationLon = StationLons[stationnum];
                }
                //if (MainActivity.distanceInKmBetweenEarthCoordinates(lat, lon, lats[lats.length - 1], lons[lats.length - 1]) < 0.1)
                    //activity.nextStep();
            }
            return Float.valueOf(shortespoint) / (lats.length - 1);
        }
        else return 1f;
   }

    public double getNextStationLat() {
        return nextStationLat;
    }

    public double getNextStationLon() {
        return nextStationLon;
    }
}
