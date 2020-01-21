package com.andjojo.itshack;

public class GeoPointListener {

    MainActivity activity;
    double targetlat,targetlon;
    int maneuvernumber=0;
    Step step;
    float epsilon=0.05f;

    public GeoPointListener(MainActivity activity,Step step){
        this.activity = activity;
        this.step = step;
        targetlat = step.getStationLats()[maneuvernumber];
        targetlon = step.getStationLons()[maneuvernumber];
    }

    public void listen(double lat, double lon){
        if (GerdaVars.isDebug())activity.addGerdaSpeechBubble(distanceInKmBetweenEarthCoordinates(lat,lon,targetlat,targetlon)+"",false);
        if (distanceInKmBetweenEarthCoordinates(lat,lon,targetlat,targetlon)<epsilon){
            maneuvernumber++;
            if(maneuvernumber<step.getStationLats().length) {
                activity.addGerdaSpeechBubble(step.getCurrentInstruction(maneuvernumber),false);
                targetlat = step.getStationLats()[maneuvernumber];
                targetlon = step.getStationLons()[maneuvernumber];
            }
            //else activity.nextStep();

        }
    }

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public static double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
