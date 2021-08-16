package com.vega.gade3.student17611612.ashmapapp.ObjectClasses;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class SavedLocationsClass {
    private String LocationName;
    private GeoPoint geoPoint;

    public SavedLocationsClass(String locationName, GeoPoint geoPoint) {
        this.LocationName = locationName;
        this.geoPoint = geoPoint;
    }

    public SavedLocationsClass(){

    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }


    public void SendToDatabase(String UserID){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        final DocumentReference docRef = mDatabase.collection("SavedLocations").document(UserID);
        docRef.collection("Locations").document(this.LocationName).set(SavedLocationsClass.this);
    }

    public void RemoveFromDatabase(String UserID){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        final DocumentReference docRef = mDatabase.collection("SavedLocations").document(UserID);
        docRef.collection("Locations").document(this.LocationName).delete();
    }
}
