package com.vega.gade3.student17611612.ashmapapp.ObjectClasses;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocationClass {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timeStamp;
    private String userID;

    private Marker mapMarker;

    public UserLocationClass(GeoPoint geoPoint, Date timeStamp, String userID /*Marker mapMarker*/) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.userID = userID;
        //this.mapMarker = mapMarker;
    }

    public UserLocationClass() {

    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

/*    public Marker getMapMarker() {
        return mapMarker;
    }

    public void setMapMarker(Marker mapMarker) {
        this.mapMarker = mapMarker;
    }*/

    @Override
    public String toString() {
        return "UserLocationClass{" +
                "geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", userID='" + userID + '\'' +
                '}';
    }
}
