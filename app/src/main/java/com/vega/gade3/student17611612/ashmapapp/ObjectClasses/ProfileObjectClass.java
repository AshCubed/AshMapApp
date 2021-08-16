package com.vega.gade3.student17611612.ashmapapp.ObjectClasses;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ProfileObjectClass implements Serializable {
    public String name;
    public String favLandmark;
    public boolean isMetric;
    public boolean isImperial;
    public Map<String, Boolean> stars = new HashMap<>();

    public ProfileObjectClass(){

    }

    public ProfileObjectClass(String NAME, String favLandmark, Boolean METRIC, Boolean IMPERIAL){
        this.name = NAME;
        this.favLandmark = favLandmark;
        this.isMetric = METRIC;
        this.isImperial = IMPERIAL;
    }


    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("Name", name);
        result.put("FavLandMark", favLandmark);
        result.put("Metric", isMetric);
        result.put("Imperial", isImperial);
        return result;
    }



}
